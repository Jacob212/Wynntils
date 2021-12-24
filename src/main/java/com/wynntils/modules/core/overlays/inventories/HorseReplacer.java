/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.overlays.inventories;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.wynntils.core.events.custom.GuiOverlapEvent;
import com.wynntils.core.framework.FrameworkManager;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.screen.inventory.HorseInventoryScreen;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.HorseInventoryContainer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import java.io.IOException;
import java.util.List;

public class HorseReplacer extends HorseInventoryScreen  {

    IInventory lowerInv; 
    HorseInventoryContainer upperInv;

    public HorseReplacer(PlayerInventory playerInv, HorseInventoryContainer horseInv, AbstractHorseEntity horse) {
        super(horseInv, playerInv, horse);

        this.lowerInv = playerInv; this.upperInv = horseInv;
    }

    public HorseInventoryContainer getUpperInv() {
        return upperInv;
    }

    public IInventory getLowerInv() {
        return lowerInv;
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.render(matrix, mouseX, mouseY, partialTicks);
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.HorseOverlap.DrawScreen(this, mouseX, mouseY, partialTicks));
    }

    @Override
    public void slotClicked(Slot slotIn, int slotId, int mouseButton, ClickType type) {
        if (!FrameworkManager.getEventBus().post(new GuiOverlapEvent.HorseOverlap.HandleMouseClick(this, slotIn, slotId, mouseButton, type)))
            super.slotClicked(slotIn, slotId, mouseButton, type);
    }

    @Override
    public void renderLabels(MatrixStack stack, int mouseX, int mouseY) {
        super.renderLabels(stack, mouseX, mouseY);
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.HorseOverlap.DrawGuiContainerForegroundLayer(this, mouseX, mouseY));
    }

    @Override
    protected void renderBg(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(stack, partialTicks, mouseX, mouseY);
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.HorseOverlap.DrawGuiContainerBackgroundLayer(this, mouseX, mouseY));
    }

    @Override
    public boolean keyPressed(int typedChar, int keyCode, int j) {
        if (!FrameworkManager.getEventBus().post(new GuiOverlapEvent.HorseOverlap.KeyTyped(this, (char) typedChar, keyCode)))
            return super.keyPressed(typedChar, keyCode, j);
        return false;
    }

    @Override
    public void renderTooltip(MatrixStack stack, int x, int y) {
        if (FrameworkManager.getEventBus().post(new GuiOverlapEvent.HorseOverlap.HoveredToolTip.Pre(this, x, y))) return;

        super.renderTooltip(stack, x, y);
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.HorseOverlap.HoveredToolTip.Post(this, x, y));
    }

    @Override
    public void renderTooltip(MatrixStack matrix, ItemStack stack, int x, int y) {
        super.renderTooltip(matrix, stack, x, y);
    }

    @Override
    public void onClose() {
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.HorseOverlap.GuiClosed(this));
        super.onClose();
    }

    public List<Widget> getButtonList() {
        return buttons;
    }

}
