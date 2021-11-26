/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.overlays.ui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
//import net.minecraft.client.gui.screen.GuiErrorScreen;
//import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;
//TODO find if this class is still needed, if needed find replacement for imports above ^^
import net.minecraft.client.gui.overlay.DebugOverlayGui;

import java.util.Map;

//public class ModConflictScreen extends CustomModLoadingErrorDisplayException {
//
//    public Map<String, String> conflictMods;
//
//    public ModConflictScreen(Map<String, String> conflictMods) {
//        this.conflictMods = conflictMods;
//    }
//
//
//    @Override
//    public void init(GuiErrorScreen errorScreen, FontRenderer font) {
//
//    }
//
//    @Override
//    public void render(MatrixStack matrix, GuiErrorScreen errorScreen, FontRenderer font, int mouseRelX, int mouseRelY, float tickTime) {
//        errorScreen.drawDefaultBackground();
//
//        int offset = (errorScreen.height/2) - (40 + 10*conflictMods.size());
//        errorScreen.drawCenteredString(font, "Wynntils has detected the following mods:", errorScreen.width/2, offset, 0xFFFFFF);
//        offset+=10;
//        for (String x : conflictMods.keySet()) {
//            offset+=10;
//            errorScreen.drawCenteredString(font, "§f§l" + x + "§f§o (" + conflictMods.get(x) + ")", errorScreen.width/2, offset, 0xFFFFFF);
//        }
//        offset+=20;
//        errorScreen.drawCenteredString(font, "§fThese mods are known to cause §f§lmassive conflicts §fwith Wynntils", errorScreen.width/2, offset, 0xFFFFFF);
//        offset+=10;
//        errorScreen.drawCenteredString(font, "§cPlease remove these mods and restart your game to play", errorScreen.width/2, offset, 0xFFFFFF);
//    }
//
//}
