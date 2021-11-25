/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.chat.overlays.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.wynntils.McIf;
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
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.GuiLabel;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.input.Mouse;

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
    
    @Override
    protected void actionPerformed(Button button) throws IOException {
        if (button == addTab) {
            McIf.mc().setScreen(new TabGUI(-2));
        } else if (button instanceof ChatButton) {
            ChatButton chatButton = (ChatButton) button;
            if (chatButton.getLanguage() != null) {
                ChatOverlay.getChat().setCurrentLanguage(chatButton.getLanguage());
                this.languageButtons.values().forEach(ChatButton::unselect);
                chatButton.setSelected(true);
            }
        }
    }

    @Override
    public boolean keyPressed(int typedChar, int keyCode, int j) {
        if (input.getValue().isEmpty() && keyCode == GLFW.GLFW_KEY_TAB) {
            ChatOverlay.getChat().switchTabs(Utils.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) || Utils.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT) ? -1 : +1);
            tabButtons.values().stream().forEach(ChatButton::unselect);
            tabButtons.get(ChatOverlay.getChat().getCurrentTab()).setSelected(true);
        }
        boolean backspace = typedChar == '\u0008';
        Pair<String, Character> output = ChatOverlay.getChat().getCurrentLanguage().replace(this.input.getValue(), typedChar);
        if (output.b != '\u0008' && backspace) {
            keyCode = 0; // no key code
        }
        if (!output.a.equals(input.getValue())) {
            this.input.setValue(output.a);
        }
        typedChar = output.b;

        return super.keyPressed(typedChar, keyCode, j);
    }

    @Override
    public void init() {
        super.init();
        int buttonId = 0;
        int tabX = 0;
        for (ChatTab tab : TabManager.getAvailableTabs()) {
            this.tabButtons.put(tab, addButton(new ChatButton(buttonId++, 20 + tabX++ * 40, this.height - 45, 37, 13, getDisplayName(tab), ChatOverlay.getChat().getCurrentTab() == tab, tab)));
        }
        addTab = addButton(new ChatButton(buttonId++, 2, this.height - 45, 15, 13, TextFormatting.GOLD + "+", false));
        int x = 0;
        languageButtons.put(WynncraftLanguage.GAVELLIAN, addButton(new ChatButton(buttonId++, this.width - ++x * 12, this.height - 14, 10, 12, "G", false, WynncraftLanguage.GAVELLIAN)));
        languageButtons.put(WynncraftLanguage.WYNNIC, addButton(new ChatButton(buttonId++, this.width - ++x * 12, this.height - 14, 10, 12, "W", false, WynncraftLanguage.WYNNIC)));
        languageButtons.put(WynncraftLanguage.NORMAL, addButton(new ChatButton(buttonId++, this.width - ++x * 12, this.height - 14, 10, 12, "N", false, WynncraftLanguage.NORMAL)));
        if (ChatConfig.INSTANCE.useBrackets) {
            languageButtons.values().forEach((button) -> button.visible = false);
        } else {
            this.input.setWidth(this.width - x * 12 - 4);
        }
        languageButtons.get(ChatOverlay.getChat().getCurrentLanguage()).setSelected(true);
    }

    @Override
    public void tick() {
        super.tick();
        for (Map.Entry<ChatTab, ChatButton> tabButton : tabButtons.entrySet()) {
            tabButton.getValue().setMessage(getDisplayName(tabButton.getKey()));
        }
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        if (!ChatConfig.INSTANCE.useBrackets) {
            drawRect(2, this.height - 14, this.input.getWidth() + 2, this.height - 2, Integer.MIN_VALUE);
            this.input.render(matrix, mouseX, mouseY, partialTicks);
            ITextComponent itextcomponent = McIf.mc().gui.getChatGUI().getMessage(Mouse.getX(), Mouse.getY());

            for (int i = 0; i < this.buttons.size(); ++i) {
                ((Button) this.buttons.get(i)).renderButton(matrix, mouseX, mouseY, partialTicks);
            }

            for (int j = 0; j < this.labelList.size(); ++j) {
                ((GuiLabel) this.labelList.get(j)).drawLabel(McIf.mc(), mouseX, mouseY);
            }

            if (itextcomponent != null && itextcomponent.getStyle().getHoverEvent() != null) {
                this.handleComponentHover(itextcomponent, mouseX, mouseY);
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

        public ChatButton(int id, int x, int y, int width, int height, String text, boolean selected) {
            super(id, x, y, width, height, text);
            this.selected = selected;
        }

        public ChatButton(int id, int x, int y, int width, int height, String text, boolean selected, ChatTab tab) {
            this(id, x, y, width, height, text, selected);
            this.tab = tab;
        }

        public ChatButton(int id, int x, int y, int width, int height, String text, boolean selected, WynncraftLanguage language) {
            this(id, x, y, width, height, text, selected);
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
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
            if (this.visible) {
                this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
                ScreenRenderer.beginGL(this.x, this.y);
                if (this.hovered) {
                    renderer.drawRect(ChatGUI.selected, 0, 0, this.width, this.height);
                } else {
                    renderer.drawRect(ChatGUI.unselected, 0, 0, this.width, this.height);
                }

                renderer.drawString(McIf.toText(this.getMessage()), this.width / 2.0f + 1, 3, this.selected ? CommonColors.GREEN : CommonColors.WHITE, SmartFontRenderer.TextAlignment.MIDDLE, SmartFontRenderer.TextShadow.NONE);
                ScreenRenderer.endGL();
            }
        }
    }

}
