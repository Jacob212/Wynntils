/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.chat.overlays.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.wynntils.McIf;
//import com.wynntils.ModCore;
import com.wynntils.core.framework.rendering.ScreenRenderer;
import com.wynntils.core.framework.rendering.SmartFontRenderer;
import com.wynntils.core.framework.rendering.colors.CommonColors;
import com.wynntils.core.framework.rendering.colors.CustomColor;
import com.wynntils.core.utils.Utils;
import com.wynntils.core.utils.objects.Pair;
import com.wynntils.modules.chat.configs.ChatConfig;
import com.wynntils.modules.chat.instances.ChatTab;
import com.wynntils.modules.chat.language.WynncraftLanguage;
import com.wynntils.modules.chat.managers.TabManager;
import com.wynntils.modules.chat.overlays.ChatOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;
//import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChatGUI extends ChatScreen {

    private static final ScreenRenderer renderer = new ScreenRenderer();

    // colors
    private static final CustomColor selected = new CustomColor(0, 0, 0, 0.7f);
    private static final CustomColor unselected = new CustomColor(0, 0, 0, 0.4f);

    private Map<ChatTab, ChatButton> tabButtons = new HashMap<>();
    private ChatButton addTab = null;
    private Map<WynncraftLanguage, ChatButton> languageButtons = new HashMap<>();

    public ChatGUI() {
        super("");

    }

    public ChatGUI(String defaultInputText) {
        super(defaultInputText);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        for (Map.Entry<ChatTab, ChatButton> tabButton : tabButtons.entrySet()) {
            if (tabButton.getValue().isMouseOver(mouseX, mouseY)) {
                if (mouseButton == 1) {
                    McIf.mc().setScreen(new TabGUI(TabManager.getAvailableTabs().indexOf(tabButton.getKey())));
                } else {
                    ChatOverlay.getChat().setCurrentTab(TabManager.getAvailableTabs().indexOf(tabButton.getKey()));
                    tabButtons.values().stream().forEach(ChatButton::unselect);
                    tabButton.getValue().setSelected(true);
                }
                McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
            }
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }    
    
//    @Override
//    protected void actionPerformed(Button button) throws IOException {
//        if (button == addTab) {
//            McIf.mc().setScreen(new TabGUI(-2));
//        } else if (button instanceof ChatButton) {
//            ChatButton chatButton = (ChatButton) button;
//            if (chatButton.getLanguage() != null) {
//                ChatOverlay.getChat().setCurrentLanguage(chatButton.getLanguage());
//                this.languageButtons.values().forEach(ChatButton::unselect);
//                chatButton.setSelected(true);
//            }
//        }
//    }

    @Override
    public boolean keyPressed(int typedChar, int keyCode, int j) {
        if (input.getValue().isEmpty() && keyCode == GLFW.GLFW_KEY_TAB) {
            ChatOverlay.getChat().switchTabs(Utils.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) || Utils.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT) ? -1 : +1);
            tabButtons.values().stream().forEach(ChatButton::unselect);
            tabButtons.get(ChatOverlay.getChat().getCurrentTab()).setSelected(true);
        }
        boolean backspace = typedChar == '\u0008';
        Pair<String, Character> output = ChatOverlay.getChat().getCurrentLanguage().replace(this.input.getValue(), (char) typedChar);
        if (output.b != '\u0008' && backspace) {
            keyCode = 0; // no key code
        }
        if (!output.a.equals(input.getValue())) {
            this.input.setValue(output.a);
        }
        typedChar = output.b;

        return super.keyPressed(typedChar, keyCode, j);
    }
    
    public boolean charTyped(char typedChar, int keyCode) {
        return this.input.charTyped(typedChar, keyCode);
     }
    
    public void languageButtonPress(Button button) {
    	ChatButton chatButton = (ChatButton) button;
    	ChatOverlay.getChat().setCurrentLanguage(chatButton.getLanguage());
    	this.languageButtons.values().forEach(ChatButton::unselect);
    	chatButton.setSelected(true);
    }
    
    public void tabButtonsPress(Button button) {
    	tabButtons.values().stream().forEach(ChatButton::unselect);
    	tabButtons.get(ChatOverlay.getChat().getCurrentTab()).setSelected(true);
    }

    @Override
    public void init() {
        super.init();
        int tabX = 0;
        for (ChatTab tab : TabManager.getAvailableTabs()) {
            this.tabButtons.put(tab, addButton(new ChatButton(20 + tabX++ * 40, this.height - 45, 37, 13
            		, new StringTextComponent(getDisplayName(tab)), button -> {
//            			ChatOverlay.getChat().setCurrentTab(TabManager.getAvailableTabs().indexOf(tab));
//            			McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
            		}, ChatOverlay.getChat().getCurrentTab() == tab, tab)));//TODO find out what is meant to be in lambda function
        }
        addTab = addButton(new ChatButton(2, this.height - 45, 15, 13, new StringTextComponent(TextFormatting.GOLD + "+"), button -> {
        	McIf.mc().setScreen(new TabGUI(-2));
        }, false));
        int x = 0;
        languageButtons.put(WynncraftLanguage.GAVELLIAN, addButton(new ChatButton(this.width - ++x * 12, this.height - 14, 10, 12
        		, new StringTextComponent("G"), button -> {languageButtonPress(button);}, false, WynncraftLanguage.GAVELLIAN)));
        
        languageButtons.put(WynncraftLanguage.WYNNIC, addButton(new ChatButton(this.width - ++x * 12, this.height - 14, 10, 12
        		, new StringTextComponent("W"), button -> {languageButtonPress(button);}, false, WynncraftLanguage.WYNNIC)));
        
        languageButtons.put(WynncraftLanguage.NORMAL, addButton(new ChatButton(this.width - ++x * 12, this.height - 14, 10, 12
        		, new StringTextComponent("N"), button -> {languageButtonPress(button);}, false, WynncraftLanguage.NORMAL)));
        
        if (ChatConfig.INSTANCE.useBrackets) {
            languageButtons.values().forEach((button) -> button.visible = false);
        } else {
            this.input.setWidth(this.width - x * 12 - 4);
        }
        languageButtons.get(ChatOverlay.getChat().getCurrentLanguage()).setSelected(true);    
    }

    //removing this for now, as it seems to not really do anything.....
//    @Override
//    public void tick() {
//        super.tick();
//        for (Map.Entry<ChatTab, ChatButton> tabButton : tabButtons.entrySet()) {
//            tabButton.getValue().setMessage(new StringTextComponent(getDisplayName(tabButton.getKey())));
//        }
//    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        if (!ChatConfig.INSTANCE.useBrackets) {
            fill(matrix, 2, this.height - 14, this.input.getWidth() + 2, this.height - 2, Integer.MIN_VALUE);
            this.input.render(matrix, mouseX, mouseY, partialTicks);
            Style textStyle = McIf.mc().gui.getChat().getClickedComponentStyleAt(mouseX, mouseY);

            for (int i = 0; i < this.buttons.size(); ++i) {
                ((Button) this.buttons.get(i)).render(matrix, mouseX, mouseY, partialTicks);
            }

            if (textStyle != null && textStyle.getHoverEvent() != null) {
                this.renderComponentHoverEffect(matrix, textStyle, mouseX, mouseY);
            }
        } else {
            super.render(matrix, mouseX, mouseY, partialTicks);
        }
    }

    private String getDisplayName(ChatTab tab) {
        if (tab.hasMentions()) {
            return TextFormatting.RED + tab.getName();
        } else if (tab.hasNewMessages()) {
            return TextFormatting.YELLOW + tab.getName();
        } else {
            return tab.getName();
        }
    }

    private static class ChatButton extends Button {
        private ChatTab tab = null;
        private boolean selected = false;
        private WynncraftLanguage language = null;

        public ChatButton(int x, int y, int width, int height, ITextComponent text, Button.IPressable onPress, boolean selected) {
            super(x, y, width, height, text, onPress);
            this.selected = selected;
        }

        public ChatButton(int x, int y, int width, int height, ITextComponent text, Button.IPressable onPress, boolean selected, ChatTab tab) {
            this(x, y, width, height, text, onPress, selected);
            this.tab = tab;
        }

        public ChatButton(int x, int y, int width, int height, ITextComponent text, Button.IPressable onPress, boolean selected, WynncraftLanguage language) {
            this(x, y, width, height, text, onPress, selected);
            this.language = language;
        }

        public void unselect() {
            this.selected = false;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public WynncraftLanguage getLanguage() {
            return language;
        }

        @Override
        public void renderButton(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        	if (this.isHovered()) {
        		fill(matrix, this.x, this.y, this.x + this.width, this.y + this.height, ChatGUI.selected.toInt());
        	} else {
        		fill(matrix, this.x, this.y, this.x + this.width, this.y + this.height, ChatGUI.unselected.toInt());
        	}
        	drawCenteredString(matrix, McIf.mc().font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, this.selected ? CommonColors.GREEN.toInt() : CommonColors.WHITE.toInt());
        }
    }

}
