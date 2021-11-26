/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.instances.inventory;

import com.wynntils.McIf;
import com.wynntils.core.events.custom.PacketEvent;
import com.wynntils.core.framework.FrameworkManager;
import com.wynntils.core.utils.objects.Pair;
import com.wynntils.modules.core.enums.InventoryResult;
import com.wynntils.modules.core.interfaces.IInventoryOpenAction;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CConfirmTransactionPacket;
import net.minecraft.network.play.server.SConfirmTransactionPacket;
import net.minecraft.network.play.server.SOpenWindowPacket;
import net.minecraft.network.play.server.SWindowItemsPacket;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Used for fake opening inventories that are opened by inventory
 * Just create the instance (title, clickItemPosition)
 * you can receive the inventory by setting up onReceiveItems
 * call #.isOpen when you are ready to isOpen the inventory and
 * call #.close whenever you finish using the inventory, otherwise
 * everything will bug and catch fire.
 */
public class FakeInventory {

    Pattern expectedWindowTitle;
    IInventoryOpenAction openAction;

    private Consumer<FakeInventory> onReceiveItems = null;
    private BiConsumer<FakeInventory, InventoryResult> onClose = null;

    private int windowId = -1;
    private short transaction = 0;
    private String windowTitle = "";
    private NonNullList<ItemStack> inventory = null;

    private boolean isOpen = false;
    private long lastAction = 0;
    private boolean expectingResponse = false;
    private long limitTime = 10000;

    public FakeInventory(Pattern expectedWindowTitle, IInventoryOpenAction openAction) {
        this.expectedWindowTitle = expectedWindowTitle;
        this.openAction = openAction;
    }

    public FakeInventory onReceiveItems(Consumer<FakeInventory> onReceiveItems) {
        this.onReceiveItems = onReceiveItems;

        return this;
    }

    public FakeInventory onClose(BiConsumer<FakeInventory, InventoryResult> onClose) {
        this.onClose = onClose;

        return this;
    }

    public FakeInventory setLimitTime(long limitTime) {
        this.limitTime = limitTime;

        return this;
    }

    public void open() {
        if (isOpen) return;

        lastAction = McIf.getSystemTime();
        expectingResponse = true;

        FrameworkManager.getEventBus().register(this);

        openAction.onOpen(this, () -> close(InventoryResult.CLOSED_PREMATURELY));
    }

    public void close() {
        close(InventoryResult.CLOSED_SUCCESSFULLY);
    }

    public void closeUnsuccessfully() {
        close(InventoryResult.CLOSED_UNSUCCESSFULLY);
    }

    private void close(InventoryResult result) {
        if (!isOpen) return;

        FrameworkManager.getEventBus().unregister(this);
        isOpen = false;

        if (windowId != -1) McIf.mc().getConnection().send(new CCloseWindowPacket(windowId));
        windowId = -1;

        if(onClose != null) McIf.mc().submit(() -> onClose.accept(this, result));
    }

    public void clickItem(int slot, int mouseButton, ClickType type) {
        if (!isOpen) return;

        lastAction = McIf.getSystemTime();
        expectingResponse = true;

        transaction++;
        McIf.mc().getConnection().send(new CClickWindowPacket(windowId, slot, mouseButton, type, inventory.get(slot), transaction));
    }

    public Pair<Integer, ItemStack> findItem(String name, BiPredicate<String, String> filterType) {
        if (!isOpen) return null;

        for(int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.get(slot);
            if (stack.isEmpty() || !stack.hasCustomHoverName()) continue;

            String displayName = McIf.getTextWithoutFormattingCodes(stack.getDisplayName());
            if (filterType.test(displayName, name)) {
                return new Pair<>(slot, stack);
            }
        }

        return null;
    }

    public List<Pair<Integer, ItemStack>> findItems(List<String> names, BiPredicate<String, String> filterType) {
        return findItems(names, Collections.nCopies(names.size(), filterType));
    }

    public List<Pair<Integer, ItemStack>> findItems(List<String> names, List<? extends BiPredicate<String, String>> filterTypes) {
        if (!isOpen) return null;

        int found = 0;
        List<Pair<Integer, ItemStack>> result = new ArrayList<>(Collections.nCopies(names.size(), null));

        for (int slot = 0, len = inventory.size(); slot < len && found != names.size(); ++slot) {
            ItemStack stack = inventory.get(slot);
            if (stack.isEmpty() || !stack.hasCustomHoverName()) continue;

            String displayName = McIf.getTextWithoutFormattingCodes(stack.getDisplayName());
            for (int i = 0; i < names.size(); ++i) {
                if (result.get(i) != null) continue;

                if (filterTypes.get(i).test(displayName, names.get(i))) {
                    result.set(i, new Pair<>(slot, stack));
                }
            }
        }

        return result;
    }

    public List<ItemStack> getInventory() {
        return inventory;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public String getWindowTitle() {
        return windowTitle;
    }

    @Override
    public FakeInventory clone() {
        return new FakeInventory(expectedWindowTitle, openAction);
    }

    // detects if the server is not responding
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (!isOpen || e.phase != TickEvent.Phase.END) return;
        if (!expectingResponse) return;
        if (McIf.getSystemTime() - lastAction < limitTime) return;

        close(InventoryResult.CLOSED_PREMATURELY);
    }

    // detects the GUI open, and gatters information
    @SubscribeEvent
    public void onInventoryReceive(PacketEvent<SOpenWindowPacket> e) {
    	//FIXME changed container info
//        if (!e.getPacket().getContainerId().equalsIgnoreCase("minecraft:container") || !e.getPacket().hasSlots()) {
//            close(InventoryResult.CLOSED_OVERLAP);
//            return;
//        }

        if (!expectedWindowTitle.matcher(McIf.getTextWithoutFormattingCodes(McIf.getUnformattedText(e.getPacket().getTitle()))).matches()) {
            close(InventoryResult.CLOSED_OVERLAP);
            return;
        }

        isOpen = true;
        expectingResponse = false;
        lastAction = McIf.getSystemTime();

        windowId = e.getPacket().getContainerId();
        windowTitle = McIf.getUnformattedText(e.getPacket().getTitle());
        inventory = NonNullList.create();

        e.setCanceled(true);
    }

    // detects item receiving
    @SubscribeEvent
    public void onItemsReceive(PacketEvent<SWindowItemsPacket> e) {
        if (windowId != e.getPacket().getContainerId()) {
            close(InventoryResult.CLOSED_OVERLAP);
            return;
        }

        inventory.clear();
        inventory.addAll(e.getPacket().getItems());

        expectingResponse = false;
        lastAction = McIf.getSystemTime();

        if (onReceiveItems != null) McIf.mc().submit(() -> onReceiveItems.accept(this));

        e.setCanceled(true);
    }

    // confirm all server transactions
    @SubscribeEvent
    public void confirmAllTransactions(PacketEvent.Incoming<SConfirmTransactionPacket> e) {
        if (windowId != e.getPacket().getContainerId()) {
            close(InventoryResult.CLOSED_OVERLAP);
            return;
        }

        McIf.mc().getConnection().send(new CConfirmTransactionPacket(windowId, e.getPacket().getUid(), true));
        e.setCanceled(true);
    }

    // interrupt if execute command
    @SubscribeEvent
    public void cancelCommands(ClientChatEvent e) {
        if (!e.getMessage().startsWith("/class") || !e.getMessage().startsWith("/classes")) return;

        close(InventoryResult.CLOSED_ACTION);
    }

    // interrupt if world is loaded
    @SubscribeEvent
    public void closeOnWorldLoad(WorldEvent.Load e) {
        if (!isOpen) return;

        close(InventoryResult.CLOSED_ACTION);
    }

}
