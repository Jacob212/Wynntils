/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.modules.chat.managers;

import com.wynntils.McIf;
import com.wynntils.core.framework.instances.PlayerInfo;
import com.wynntils.core.framework.instances.data.CharacterData;
import com.wynntils.core.framework.instances.data.InventoryData;
import com.wynntils.core.utils.Utils;
import com.wynntils.core.utils.helpers.TextAction;
import com.wynntils.core.utils.objects.Location;
import com.wynntils.modules.chat.ChatModule;
import com.wynntils.modules.chat.configs.ChatConfig;
import com.wynntils.modules.chat.overlays.ChatOverlay;
import com.wynntils.modules.core.managers.CompassManager;
import com.wynntils.modules.map.overlays.ui.MainWorldMapUI;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.item.Item;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

public class HeldItemChatManager {

    private static final int MESSAGE_ID = 1711716819;  // HeldItemChatManager.class.getName().hashCode()
    private static final int DISPLAY_TIME = 1000;  // ms to show message

    private static boolean hasMessage = false;
    private static long startedHolding = Long.MIN_VALUE;
    private static int lastHolding = -1;

    public static void onTick() {
        showMessage(getMessage());
    }

    public static void reset() {
        deleteMessage();
        startedHolding = Long.MIN_VALUE;
        lastHolding = -1;
    }

    private static ITextComponent getMessage() {
        if (
            !ChatConfig.INSTANCE.heldItemChat ||
            McIf.player() == null || McIf.world() == null ||
            McIf.player().inventory.items.get(6).getItem() != Items.COMPASS ||
            McIf.player().inventory.items.get(7).getItem() != Items.WRITTEN_BOOK ||
            McIf.player().inventory.items.get(8).getItem() != Items.NETHER_STAR &&
            McIf.player().inventory.items.get(8).getItem() != Item.byBlock(Blocks.SNOW) ||
            !PlayerInfo.get(CharacterData.class).isLoaded()
        ) {
            reset();
            return null;
        }

        if (lastHolding != McIf.player().inventory.selected) {
            lastHolding = McIf.player().inventory.selected;
            startedHolding = System.currentTimeMillis();
            return null;
        }

        if (System.currentTimeMillis() < startedHolding + DISPLAY_TIME) return null;

        switch (McIf.player().inventory.selected) {
            case 6: return getCompassMessage();
            // case 7: return getQuestBookMessage();
            case 8: return getSoulPointsMessage();
            default: return null;
        }
    }

    private static class OnOpenMapAtCompassClick implements Runnable {
        @Override
        public void run() {
            Location compass = CompassManager.getCompassLocation();
            if (compass == null) {
                Utils.setScreen(new MainWorldMapUI());
                return;
            }

            Utils.setScreen(new MainWorldMapUI((float) compass.clone().getX(), (float) compass.getZ()));
        }
    }

    private static ITextComponent getCompassMessage() {
        ITextComponent base = new StringTextComponent("Compass Beacon");
        ITextComponent text = base;

        text.getStyle().withColor(TextFormatting.RED);
        text.getStyle().withBold(true);

        text = add(text, new StringTextComponent(" "));
        text.getStyle().withColor(TextFormatting.WHITE);
        text.getStyle().withBold(false);

        Location compass = CompassManager.getCompassLocation();
        if (compass != null) {
            ITextComponent clearBeacon = add(text, new StringTextComponent("[Clear beacon]"));
            clearBeacon.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Click to clear the compass beacon")));
            clearBeacon.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/compass clear"));

            text = add(text, new StringTextComponent(" "));
        }

        add(text, getCancelComponent());
        text = add(text, new StringTextComponent("\n"));
        text.getStyle().withColor(TextFormatting.WHITE);

        if (compass == null) {
            ITextComponent suggestCompass = add(text, new StringTextComponent("Compass beacon not set"));
            suggestCompass.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Do /compass or middle click on map to set compass")));
            suggestCompass.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/compass "));
            suggestCompass.getStyle().withColor(TextFormatting.GRAY);
            suggestCompass.getStyle().withItalic(true);
            return base;
        }

        double compassX = compass.getX();
        double compassZ = compass.getZ();
        double playerX = McIf.player().getX();
        double playerZ = McIf.player().getZ();

        int distance = MathHelper.floor(MathHelper.sqrt((compassX - playerX) * (compassX - playerX) + (compassZ - playerZ) * (compassZ - playerZ)));

        ITextComponent showOnMap = add(text, new StringTextComponent(String.format(
            TextFormatting.DARK_AQUA + "%d, %d" + TextFormatting.RESET + ": %dm away",
            MathHelper.floor(compassX), MathHelper.floor(compassZ), distance
        )));

        text.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Click to show on main map")));
        text.getStyle().withClickEvent(TextAction.getStaticEvent(OnOpenMapAtCompassClick.class));

        return base;
    }

    private static ITextComponent getSoulPointsMessage() {
        InventoryData info = PlayerInfo.get(InventoryData.class);
        int maxSoulPoints = info.getMaxSoulPoints();
        int currentSoulPoints = info.getSoulPoints();
        int time = info.getTicksToNextSoulPoint();

        if (maxSoulPoints == -1 || currentSoulPoints == -1 || time == -1) return null;

        ITextComponent base = new StringTextComponent("Soul points");
        ITextComponent text = base;

        text.getStyle().withColor(TextFormatting.AQUA);
        text.getStyle().withBold(true);

        text = add(text, new StringTextComponent(" "));
        text.getStyle().withColor(TextFormatting.WHITE);
        text.getStyle().withBold(false);

        add(text, getCancelComponent());
        text = add(text, new StringTextComponent("\n"));

        text = add(text, new StringTextComponent(String.format(
            "§6§l%d§6/§l%d§r ",
            currentSoulPoints, maxSoulPoints
        )));

        if (currentSoulPoints >= maxSoulPoints) {
            add(text, new StringTextComponent("§e[§lFULL§e]"));
        } else {
            int seconds = (time / 20) % 60;
            add(text, new StringTextComponent(String.format(
                "§e[§l%s§e:§l%s§e]", time / (20 * 60), String.format("%02d", seconds)
            )));
        }

        return base;
    }

    private static class OnCancelClick implements Runnable {
        private static class OnUnhideClick implements Runnable {
            @Override
            public void run() {
                ChatConfig.INSTANCE.heldItemChat = true;
                ChatConfig.INSTANCE.saveSettings(ChatModule.getModule());
            }
        }

        @Override
        public void run() {
            ChatConfig.INSTANCE.heldItemChat = false;
            ChatConfig.INSTANCE.saveSettings(ChatModule.getModule());

            ITextComponent message = new StringTextComponent("Enable §bMod options > Chat > Held Item Chat Messages§r to undo (or click this)");
            McIf.sendMessage(TextAction.withStaticEvent(message, OnUnhideClick.class));
        }
    }

    private static ITextComponent getCancelComponent() {
        ITextComponent msg = new StringTextComponent("[x]");
        msg.getStyle().withColor(TextFormatting.DARK_RED);
        msg.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Do not show this text")));

        return TextAction.withStaticEvent(msg, OnCancelClick.class);
    }

    private static void deleteMessage() {
        if (!hasMessage) return;

        hasMessage = false;
        ChatOverlay.getChat().deleteChatLine(MESSAGE_ID);
    }

    private static void showMessage(ITextComponent msg) {
        if (msg == null) {
            deleteMessage();
            return;
        }

        hasMessage = true;
        boolean oldClickableCoords = ChatConfig.INSTANCE.clickableCoordinates;
        ChatConfig.INSTANCE.clickableCoordinates = false;
        try {
            ChatOverlay.getChat().printUnloggedChatMessage(msg, MESSAGE_ID);
        } finally {
            ChatConfig.INSTANCE.clickableCoordinates = oldClickableCoords;
        }
    }

    private static ITextComponent add(ITextComponent to, ITextComponent what) {
        to.getSiblings().add(what);
        return what;
    }

}
