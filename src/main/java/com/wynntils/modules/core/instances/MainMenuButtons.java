/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.instances;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.wynntils.McIf;
import com.wynntils.Reference;
import com.wynntils.core.framework.rendering.textures.Textures;
import com.wynntils.core.utils.ServerUtils;
import com.wynntils.modules.core.config.CoreDBConfig;
import com.wynntils.modules.core.overlays.UpdateOverlay;
//import com.wynntils.modules.core.overlays.ui.UpdateAvailableScreen;
import com.wynntils.modules.utilities.instances.ServerIcon;
//import com.wynntils.webapi.WebManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import com.wynntils.transition.GlStateManager;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.ResourceLocation;
//import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.List;

public class MainMenuButtons {

    private static ServerList serverList = null;
    private static final int WYNNCRAFT_BUTTON_ID = 3790627;

    private static WynncraftButton lastButton = null;

    private static boolean alreadyLoaded = false;

    //TODO Not needed anymore
//    public static void addButtons(MainMenuScreen to, List<Button> buttons, boolean resize) {
////        if (!CoreDBConfig.INSTANCE.addMainMenuButton) return;
//
//        if (lastButton == null || !resize) {
//            ServerData s = getWynncraftServerData();
////            FMLClientHandler.instance().setupServerList();
//
////            lastButton = new WynncraftButton(s, WYNNCRAFT_BUTTON_ID, to.width / 2 + 104, to.height / 4 + 48 + 24, (button) -> {
////            	actionPerformed(to, button, buttons);
////                 });
////            WebManager.checkForUpdates();
//            UpdateOverlay.reset();
//
//            buttons.add(lastButton);
//
//            // little pling when finished loading
//            if (!alreadyLoaded) {
//                McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.NOTE_BLOCK_PLING, 1f));
//                alreadyLoaded = true;
//            }
//            
//        }
//
//        lastButton.x = to.width / 2 + 104; lastButton.y = to.height / 4 + 48 + 24;
//        buttons.add(lastButton);
//    }

    public static void actionPerformed(Screen gui, Button button) {
    	clickedWynncraftButton(((WynncraftButton) button).serverIcon.getServer(), gui);
    }

    //FIXME auto updater not working
    private static void clickedWynncraftButton(ServerData server, Screen backGui) {
//        if (hasUpdate()) {
//            McIf.mc().setScreen(new UpdateAvailableScreen(server));
//        } else {
//            WebManager.skipJoinUpdate();
            ServerUtils.connect(backGui, server);
//        }
    }
//
//    private static boolean hasUpdate() {
//        return !Reference.developmentEnvironment && WebManager.getUpdate() != null && WebManager.getUpdate().hasUpdate();
//    }

    private static ServerData getWynncraftServerData() {
        return ServerUtils.getWynncraftServerData(serverList = new ServerList(McIf.mc()), true);
    }

    public static class WynncraftButton extends Button {

        private ServerIcon serverIcon;

        public WynncraftButton(ServerData server, int x, int y, Button.IPressable onPress) {
        	super(x, y, 20, 20, new StringTextComponent("Join Wynncraft"), onPress);

            serverIcon = new ServerIcon(server, true);
            serverIcon.onDone(r -> serverList.save());
        }

        //TODO needs ServerIcon fixing first
//        @Override
//        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
//            if (!visible) return;
//
//            super.renderButton(matrices, mouseX, mouseY, partialTicks);
//
//            ServerIcon.ping();
//            ResourceLocation icon = ServerIcon.getServerIcon();
//            if (icon == null) icon = ServerIcon.UNKNOWN_SERVER;
//            McIf.mc().getTextureManager().bind(icon);
//
//            boolean hasUpdate = hasUpdate();
//            boolean hasUpdate = false;
//
//            GlStateManager.pushMatrix();
//
//            GlStateManager.translate(x + 2, y + 2, 0);
//            GlStateManager.scale(0.5f, 0.5f, 0);
//            GlStateManager.enableBlend();
//            blit(matrices, 0, 0, 0.0F, 0.0F, 32, 32, 32, 32);
//            if (!hasUpdate) {
//                GlStateManager.disableBlend();
//            }
//
//            GlStateManager.popMatrix();
//
//            if (hasUpdate) {
//                Textures.UIs.main_menu.bind();
//                // When not provided with the texture size vanilla automatically assumes both the height and width are 256
//                blit(matrices, x, y, 0, 0, 20, 20);
//            }
//
//            GlStateManager.disableBlend();
//        }

    }
//TODO Dont think this is needed anymore
//    public static class FakeGui extends Screen {
//        FakeGui() {
//            doAction();
//        }
//
//        @Override
//        public void init() {
//            doAction();
//        }
//
//        private static void doAction() {
//            clickedWynncraftButton(getWynncraftServerData(), null);
//        }
//    }

}
