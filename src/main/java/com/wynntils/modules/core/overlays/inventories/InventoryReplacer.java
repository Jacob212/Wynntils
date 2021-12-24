/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.overlays.inventories;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.wynntils.core.events.custom.GuiOverlapEvent;
import com.wynntils.core.framework.FrameworkManager;
import com.wynntils.modules.questbook.enums.QuestBookPages;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import com.wynntils.transition.GlStateManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import java.io.IOException;
import java.util.List;


public class InventoryReplacer extends InventoryScreen {

    PlayerEntity player;

    public InventoryReplacer(PlayerEntity player) {
        super(player);

        this.player = player;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.render(matrix, mouseX, mouseY, partialTicks);

        FrameworkManager.getEventBus().post(new GuiOverlapEvent.InventoryOverlap.DrawScreen(this, mouseX, mouseY, partialTicks));
    }

    @Override
    public void slotClicked(Slot slotIn, int slotId, int mouseButton, ClickType type) {
        if (!FrameworkManager.getEventBus().post(new GuiOverlapEvent.InventoryOverlap.HandleMouseClick(this, slotIn, slotId, mouseButton, type)))
            super.slotClicked(slotIn, slotId, mouseButton, type);
    }

    @Override
    public void renderLabels(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderLabels(matrix, mouseX, mouseY);

        FrameworkManager.getEventBus().post(new GuiOverlapEvent.InventoryOverlap.DrawGuiContainerForegroundLayer(this, mouseX, mouseY));
    }

    @Override
    protected void renderBg(MatrixStack matrix, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(matrix, partialTicks, mouseX, mouseY);
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.InventoryOverlap.DrawGuiContainerBackgroundLayer(this, mouseX, mouseY));
    }

    @Override
    public boolean keyPressed(int typedChar, int keyCode, int j) {
        if (!FrameworkManager.getEventBus().post(new GuiOverlapEvent.InventoryOverlap.KeyTyped(this, (char) typedChar, keyCode)))
            return super.keyPressed(typedChar, keyCode, j);
        return false;
    }
//TODO actionPerformed has been removed, buttons don't work like this anymore
//    @Override
//    public void actionPerformed(Button guiButton) throws IOException {
//        if (guiButton.id == 10) {
//            QuestBookPages.MAIN.getPage().open(true);
//            return;
//        }
//
//        super.actionPerformed(guiButton);
//    }

    @Override
    public void renderTooltip(MatrixStack matrix, int x, int y) {
        if (FrameworkManager.getEventBus().post(new GuiOverlapEvent.InventoryOverlap.HoveredToolTip.Pre(this, x, y))) return;

        super.renderTooltip(matrix, x, y);
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.InventoryOverlap.HoveredToolTip.Post(this, x, y));
    }

    @Override
    public void renderTooltip(MatrixStack matrix, ItemStack stack, int x, int y) {
        GlStateManager.translate(0, 0, -300d);
        super.renderTooltip(matrix, stack, x, y);
    }

    @Override
    public void onClose() {
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.InventoryOverlap.GuiClosed(this));
        super.onClose();
    }

    public List<Widget> getButtonList() {
        return buttons;
    }
}
