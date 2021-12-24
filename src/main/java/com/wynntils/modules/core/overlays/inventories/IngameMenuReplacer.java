/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.overlays.inventories;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.wynntils.core.events.custom.GuiOverlapEvent;
import com.wynntils.core.framework.FrameworkManager;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.client.gui.screen.IngameMenuScreen;

import java.io.IOException;
import java.util.List;

public class IngameMenuReplacer extends IngameMenuScreen {

	//TODO might not be right
    public IngameMenuReplacer(boolean p_i51519_1_) {
		super(p_i51519_1_);
		// TODO Auto-generated constructor stub
	}

	@Override
    public void init() {
        super.init();

        FrameworkManager.getEventBus().post(new GuiOverlapEvent.IngameMenuOverlap.InitGui(this, buttons));
    }

	//actionPerformed is no longer a thing
	//TODO find a new way to post the event when a button is pressed
//    @Override
//    public void actionPerformed(Button btn) throws IOException {
//        if (FrameworkManager.getEventBus().post(new GuiOverlapEvent.IngameMenuOverlap.ActionPerformed(this, btn))) {
//            return;
//        }
//        super.actionPerformed(btn);
//    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.render(matrix, mouseX, mouseY, partialTicks);

        FrameworkManager.getEventBus().post(new GuiOverlapEvent.IngameMenuOverlap.DrawScreen(this, mouseX, mouseY, partialTicks));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        boolean result = super.mouseClicked(mouseX, mouseY, mouseButton);

        FrameworkManager.getEventBus().post(new GuiOverlapEvent.IngameMenuOverlap.MouseClicked(this, (int) mouseX, (int) mouseY, mouseButton));
        return result;
    }

    @Override
    public void renderTooltip(MatrixStack stack, ITextComponent text, int x, int y) {
        super.renderTooltip(stack, text, x, y);
    }

    public List<Widget> getButtonList() {
        return buttons;
    }

}
