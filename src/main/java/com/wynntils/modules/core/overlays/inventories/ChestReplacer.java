/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.overlays.inventories;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.wynntils.core.events.custom.GuiOverlapEvent;
import com.wynntils.core.framework.FrameworkManager;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import java.io.IOException;
import java.util.List;

public class ChestReplacer extends ChestScreen {
    
	IInventory lowerInv;
	IInventory upperInv;

    public ChestReplacer(ChestContainer upperInv, IInventory lowerInv, ITextComponent title) {
        super(upperInv, (PlayerInventory) lowerInv, title);

        this.lowerInv = lowerInv;
        this.upperInv = upperInv.getContainer();
    }

    public IInventory getLowerInv() {
        return lowerInv;
    }

    public IInventory getUpperInv() {
        return upperInv;
    }

    @Override
    public void init() {
        super.init();
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.ChestOverlap.InitGui(this, this.buttons));
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        if (FrameworkManager.getEventBus().post(new GuiOverlapEvent.ChestOverlap.DrawScreen.Pre(this, mouseX, mouseY, partialTicks))) {
            return;
        }

        super.render(matrix, mouseX, mouseY, partialTicks);
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.ChestOverlap.DrawScreen.Post(this, mouseX, mouseY, partialTicks));
    }

    @Override
    public void slotClicked(Slot slotIn, int slotId, int mouseButton, ClickType type) {
        if (!FrameworkManager.getEventBus().post(new GuiOverlapEvent.ChestOverlap.HandleMouseClick(this, slotIn, slotId, mouseButton, type)))
            super.slotClicked(slotIn, slotId, mouseButton, type);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double d1, double d2) {
        // FIXME: the event class needs fields that match the new reality
        if (!FrameworkManager.getEventBus().post(new GuiOverlapEvent.ChestOverlap.MouseClickMove(this, (int) mouseX, (int) mouseY, mouseButton, (long) d1)))
            return super.mouseDragged(mouseX, mouseY, mouseButton, d1, d2);
        return false;
    }
    //TODO handleMouseInput has been removed,
    //find out if HandleMouseInput event is used anywhere.
//    @Override
//    public void handleMouseInput() throws IOException {
//        if (!FrameworkManager.getEventBus().post(new GuiOverlapEvent.ChestOverlap.HandleMouseInput(this)))
//            super.handleMouseInput();
//    }

    @Override
    public void renderLabels(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderLabels(matrix, mouseX, mouseY);
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.ChestOverlap.DrawGuiContainerForegroundLayer(this, mouseX, mouseY));
    }

    @Override
    protected void renderBg(MatrixStack matrix, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(matrix, partialTicks, mouseX, mouseY);
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.ChestOverlap.DrawGuiContainerBackgroundLayer(this, mouseX, mouseY));
    }

    @Override
    public boolean keyPressed(int typedChar, int keyCode, int j)  {
        if (!FrameworkManager.getEventBus().post(new GuiOverlapEvent.ChestOverlap.KeyTyped(this, (char) typedChar, keyCode)))
            return super.keyPressed(typedChar, keyCode, j);
        return false;
    }

    @Override
    public void renderTooltip(MatrixStack stack, int x, int y) {
        if (FrameworkManager.getEventBus().post(new GuiOverlapEvent.ChestOverlap.HoveredToolTip.Pre(this, x, y))) return;

        super.renderTooltip(stack, x, y);
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.ChestOverlap.HoveredToolTip.Post(this, x, y));
    }
    
    @Override
    public void renderTooltip(MatrixStack matrix, ItemStack stack, int x, int y) {
        super.renderTooltip(matrix, stack, x, y);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)  {
        if (FrameworkManager.getEventBus().post(new GuiOverlapEvent.ChestOverlap.MouseClicked(this, mouseX, mouseY, mouseButton))) return true;
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onClose() {
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.ChestOverlap.GuiClosed(this));
        super.onClose();
    }

    public List<Widget> getButtonList() {
        return this.buttons;
    }
}
