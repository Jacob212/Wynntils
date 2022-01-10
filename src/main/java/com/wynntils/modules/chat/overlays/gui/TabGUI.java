/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.chat.overlays.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.wynntils.McIf;
import com.wynntils.core.framework.rendering.colors.CommonColors;
import com.wynntils.modules.chat.instances.ChatTab;
import com.wynntils.modules.chat.managers.TabManager;
import com.wynntils.modules.chat.overlays.ChatOverlay;
import net.minecraft.client.gui.widget.*;
import static net.minecraft.util.text.TextFormatting.*;

import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.util.text.StringTextComponent;

import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.regex.Pattern;


public class TabGUI extends Screen {

    int id;
    ChatTab tab;
    boolean simple = true;

    public TabGUI(int id) {
    	super(new StringTextComponent("Tab Menu"));
        this.id = id;

        if (id != -2)
            tab = TabManager.getTabById(id);
    }
    List<CheckboxButton> simpleRegexSettings = new ArrayList<>();

    // ui things
    Button saveButton;
    Button deleteButton;
    Button advancedButton;
    Button closeButton;
    CheckboxButton lowPriority;
    CheckboxButton allRegex;
    CheckboxButton localRegex;
    CheckboxButton guildRegex;
    CheckboxButton partyRegex;
    CheckboxButton shoutsRegex;
    CheckboxButton pmRegex;
    TextFieldWidget nameTextField;
    TextFieldWidget regexTextField;
    TextFieldWidget autoCommandField;
    TextFieldWidget orderNbField;

    @Override
    public void init() {
        buttons.clear();
        simpleRegexSettings.clear();

        int x = width / 2; int y = height / 2;

        // General
        buttons.add(saveButton = new Button(x - 90, y + 40, 40, 20, new StringTextComponent(GREEN + "Save"), (button) -> {
        	if (id == -2) {
        		TabManager.registerNewTab(new ChatTab(nameTextField.getValue(), regexTextField.getValue(), regexSettingsCreator(), autoCommandField.getValue(), lowPriority.selected(), orderNbField.getValue().matches("[0-9]+") ? Integer.parseInt(orderNbField.getValue()) : 0));
        	} else {
        		TabManager.updateTab(id, nameTextField.getValue(), regexTextField.getValue(), regexSettingsCreator(), autoCommandField.getValue(), lowPriority.selected(), orderNbField.getValue().matches("[0-9]+") ? Integer.parseInt(orderNbField.getValue()) : 0);
        	}
        	McIf.mc().setScreen(new ChatGUI());
        }));//id=0
        buttons.add(deleteButton = new Button(x - 45, y + 40, 40, 20, new StringTextComponent(DARK_RED + "Delete"), (button) -> {
        	McIf.mc().setScreen(new ConfirmScreen((result) -> {
        		if (result) {
        			int c = TabManager.deleteTab(id);
        			if (ChatOverlay.getChat().getCurrentTabId() == id) ChatOverlay.getChat().setCurrentTab(c);
        			McIf.mc().setScreen(new ChatGUI());
        		} else {
        			McIf.mc().setScreen(this);
        		}
        	}, new StringTextComponent(""), new StringTextComponent(WHITE + (BOLD + "Do you really want to delete this chat tab?")+ RED + "This action is irreversible!")));
        }));//id=1
        buttons.add(closeButton = new Button(x + 50, y + 40, 40, 20, new StringTextComponent(WHITE + "Close")
        		, button -> {McIf.mc().setScreen(new ChatGUI());}));//id=2

        buttons.add(advancedButton = new Button(x - 65, y - 60, 130, 20, new StringTextComponent("Show Advanced Settings"), (button) -> {
        	if (button.getMessage().equals(new StringTextComponent("Show Advanced Settings"))) {
        		button.setMessage(new StringTextComponent("Hide Advanced Settings"));
        		simple = false;
        	} else {
        		button.setMessage(new StringTextComponent("Show Advanced Settings"));
        		simple = true;
        	}
        	regexTextField.setVisible(!simple);
        	simpleRegexSettings.forEach(b -> b.visible = simple);
        }));//id=3

        deleteButton.active = (id != -2) && TabManager.getAvailableTabs().size() > 1;

        buttons.add(nameTextField = new TextFieldWidget(McIf.mc().font, x - 110, y - 90, 80, 20, new StringTextComponent("")));//id=3
        nameTextField.setVisible(true);
        nameTextField.active = true;
        nameTextField.setMaxLength(10);

        buttons.add(autoCommandField = new TextFieldWidget(McIf.mc().font, x - 12, y - 90, 80, 20, new StringTextComponent("")));//id=3
        autoCommandField.setVisible(true);
        autoCommandField.active = true;
        autoCommandField.setMaxLength(10);

        buttons.add(orderNbField = new TextFieldWidget(McIf.mc().font, x + 85, y - 90, 25, 20, new StringTextComponent("")));//id=3
        orderNbField.setVisible(true);
        orderNbField.active = true;
        orderNbField.setMaxLength(2);

        //TODO CheckBox default size was 11x11, maybe dont know really
        buttons.add(lowPriority = new CheckboxButton(x - 100, y + 22, 15, 15, new StringTextComponent("Low Priority"), true));//id=3

        // Simple
        simpleRegexSettings.add(allRegex = new CheckboxButton(x - 100, y - 25, 15, 15, new StringTextComponent("All"), false));//id=10
        simpleRegexSettings.add(localRegex = new CheckboxButton(x - 50, y - 25, 15, 15, new StringTextComponent("Local"), false));//id=11
        simpleRegexSettings.add(guildRegex = new CheckboxButton(x, y - 25, 15, 15, new StringTextComponent("Guild"), false));//id=12
        simpleRegexSettings.add(partyRegex = new CheckboxButton(x + 50, y - 25, 15, 15, new StringTextComponent("Party"), false));//id=13
        simpleRegexSettings.add(shoutsRegex = new CheckboxButton(x - 100, y - 10, 15, 15, new StringTextComponent("Shouts"), false));//id=14
        simpleRegexSettings.add(pmRegex = new CheckboxButton(x - 50, y - 10, 15, 15, new StringTextComponent("PMs"), false));//id=15
        buttons.addAll(simpleRegexSettings);
        applyRegexSettings();
        // Advanced
        buttons.add(regexTextField = new TextFieldWidget(McIf.mc().font, x - 100, y - 20, 200, 20, new StringTextComponent("")));
        regexTextField.setVisible(false);
        regexTextField.active = true;
        regexTextField.setMaxLength(400);

        if (tab != null) {
            nameTextField.setValue(tab.getName());
            regexTextField.setValue(tab.getRegex().replace("§", "&"));
            if (lowPriority.selected() != tab.isLowPriority()) {
            	lowPriority.onPress();//TODO This should work
            }
            autoCommandField.setValue(tab.getAutoCommand());
            orderNbField.setValue(Integer.toString(tab.getOrderNb()));
            checkIfRegexIsValid();
        }

//        Keyboard.enableRepeatEvents(true);
    }

//    @Override
//    public void onClose() {
//        Keyboard.enableRepeatEvents(false);
//    }

//    protected void actionPerformed(Button button) throws IOException {
////        super.actionPerformed(button);
//
//        if (button == closeButton) McIf.mc().setScreen(new ChatGUI());
//        else if (button == saveButton) {
//            if (id == -2) {
//                TabManager.registerNewTab(new ChatTab(nameTextField.getValue(), regexTextField.getValue(), regexSettingsCreator(), autoCommandField.getValue(), lowPriority.isChecked(), orderNbField.getValue().matches("[0-9]+") ? Integer.parseInt(orderNbField.getValue()) : 0));
//            } else {
//                TabManager.updateTab(id, nameTextField.getValue(), regexTextField.getValue(), regexSettingsCreator(), autoCommandField.getValue(), lowPriority.isChecked(), orderNbField.getValue().matches("[0-9]+") ? Integer.parseInt(orderNbField.getValue()) : 0);
//            }
//            McIf.mc().setScreen(new ChatGUI());
//        } else if (button == deleteButton) {
//            McIf.mc().setScreen(new ConfirmScreen((result, cc) -> {
//                if (result) {
//                    int c = TabManager.deleteTab(id);
//                    if (ChatOverlay.getChat().getCurrentTabId() == id) ChatOverlay.getChat().setCurrentTab(c);
//                    McIf.mc().setScreen(new ChatGUI());
//                } else {
//                    McIf.mc().setScreen(this);
//                }
//            }, WHITE + (BOLD + "Do you really want to delete this chat tab?"), RED + "This action is irreversible!", 0));
//        } else if (button == advancedButton) {
//            boolean simple;
//            if (button.getMessage().equals("Show Advanced Settings")) {
//                button.setMessage("Hide Advanced Settings");
//                simple = false;
//            } else {
//                button.setMessage("Show Advanced Settings");
//                simple = true;
//            }
//            regexTextField.setVisible(!simple);
//            regexLabel.visible = !simple;
//            simpleSettings.visible = simple;
//            simpleRegexSettings.forEach(b -> b.visible = simple);
//        } else if (button == allRegex) {
//            simpleRegexSettings.forEach(b -> b.setIsChecked(((CheckboxButton) button).isChecked()));
//        }
//        if (button.id >= 10 && button.id <= 16) {
//            regexTextField.setValue(regexCreator());
//            checkIfRegexIsValid();
//        }
//    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrix);

        super.render(matrix, mouseX, mouseY, partialTicks);
        
        //labels
        int x = width / 2; int y = height / 2;        
        if (simple) {
        	drawString(matrix, McIf.mc().font, "Message types " + RED + "*", x - 100, y - 35, CommonColors.WHITE.toInt());
        } else {
        	drawString(matrix, McIf.mc().font, "Regex " + RED + "*", x - 100, y - 35, CommonColors.WHITE.toInt());
        }
        drawString(matrix, McIf.mc().font, "Name " + RED + "*", x - 110, y - 105, CommonColors.WHITE.toInt());
        drawString(matrix, McIf.mc().font, "Auto Command", x - 12, y - 105, CommonColors.WHITE.toInt());
        drawString(matrix, McIf.mc().font, "Order #", x + 85, y - 105, CommonColors.WHITE.toInt());

        if (mouseX >= nameTextField.x && mouseX < nameTextField.x + nameTextField.getWidth() && mouseY >= nameTextField.y && mouseY < nameTextField.y + nameTextField.getHeight())
        	renderComponentTooltip(matrix, Arrays.asList(new StringTextComponent(GREEN + (BOLD +"Name")), new StringTextComponent(GRAY + "This is how your tab"), new StringTextComponent(GRAY + "will be named"), new StringTextComponent(""), new StringTextComponent(RED + "Required")), mouseX, mouseY);

        if (regexTextField.isVisible() && mouseX >= regexTextField.x && mouseX < regexTextField.x + regexTextField.getWidth() && mouseY >= regexTextField.y && mouseY < regexTextField.y + regexTextField.getHeight())
            renderComponentTooltip(matrix, Arrays.asList(new StringTextComponent(GREEN + (BOLD + "RegEx")), new StringTextComponent(GRAY + "This will parse the chat"), new StringTextComponent(" "), new StringTextComponent(GREEN + "You can learn RegEx at"), new StringTextComponent(GOLD + "https://regexr.com/"), new StringTextComponent(""), new StringTextComponent(RED + "Required")), mouseX, mouseY);

        if (mouseX >= autoCommandField.x && mouseX < autoCommandField.x + autoCommandField.getWidth() && mouseY >= autoCommandField.y && mouseY < autoCommandField.y + autoCommandField.getHeight())
        	renderComponentTooltip(matrix, Arrays.asList(new StringTextComponent(GREEN + (BOLD + "Auto Command")), new StringTextComponent(GRAY + "This will automatically"), new StringTextComponent(GRAY + "put this command before"), new StringTextComponent(GRAY + "any message."), new StringTextComponent(""), new StringTextComponent(RED + "Optional")), mouseX, mouseY);

        if (mouseX >= orderNbField.x && mouseX < orderNbField.x + orderNbField.getWidth() && mouseY >= orderNbField.y && mouseY < orderNbField.y + orderNbField.getHeight())
        	renderComponentTooltip(matrix, Arrays.asList(new StringTextComponent(GREEN + (BOLD + "Order number")), new StringTextComponent(GRAY + "This determines the"), new StringTextComponent(GRAY + "arrangement of the"), new StringTextComponent(GRAY + "chat tabs."), new StringTextComponent(DARK_GRAY + "(lowest to highest)"), new StringTextComponent(RED + "Optional")), mouseX, mouseY);

        if (mouseX >= lowPriority.x && mouseX < lowPriority.x + lowPriority.getWidth() && mouseY >= lowPriority.y && mouseY < lowPriority.y + lowPriority.getHeight())
            renderComponentTooltip(matrix, Arrays.asList(new StringTextComponent(GREEN + (BOLD + "Low priority")), new StringTextComponent(GRAY + "If selected, messages"), new StringTextComponent(GRAY + "will attempt to match"), new StringTextComponent(GRAY + "with other tabs first."), new StringTextComponent(""), new StringTextComponent(GRAY + "This will also duplicate"), new StringTextComponent(GRAY + "messages across other"), new StringTextComponent(GRAY + "low priority tabs."), new StringTextComponent(RED + "Optional")), mouseX, mouseY);

        if (advancedButton.getMessage().getString().equals("Show Advanced Settings")) {
            if (mouseX >= allRegex.x && mouseX < allRegex.x + allRegex.getWidth() && mouseY >= allRegex.y && mouseY < allRegex.y + allRegex.getHeight()) {
                renderComponentTooltip(matrix, Arrays.asList(new StringTextComponent(GREEN + (BOLD + "Message Type: All")), new StringTextComponent(GRAY + "This will send all"), new StringTextComponent(GRAY + "messages, except those"), new StringTextComponent(GRAY + "deselected to this tab.")), mouseX, mouseY);
            } else if (mouseX >= localRegex.x && mouseX < localRegex.x + localRegex.getWidth() && mouseY >= localRegex.y && mouseY < localRegex.y + localRegex.getHeight()) {
                renderComponentTooltip(matrix, Arrays.asList(new StringTextComponent(GREEN + (BOLD + "Message Type: Local")), new StringTextComponent(GRAY + "This will send all"), new StringTextComponent(GRAY + "messages send by nearby"), new StringTextComponent(GRAY + "players to this tab.")), mouseX, mouseY);
            } else if (mouseX >= guildRegex.x && mouseX < guildRegex.x + guildRegex.getWidth() && mouseY >= guildRegex.y && mouseY < guildRegex.y + guildRegex.getHeight()) {
                renderComponentTooltip(matrix, Arrays.asList(new StringTextComponent(GREEN + (BOLD + "Message Type: Guild")), new StringTextComponent(GRAY + "This will send all"), new StringTextComponent(GRAY + "messages send by guild"), new StringTextComponent(GRAY + "members to this tab.")), mouseX, mouseY);
            } else if (mouseX >= partyRegex.x && mouseX < partyRegex.x + partyRegex.getWidth() && mouseY >= partyRegex.y && mouseY < partyRegex.y + partyRegex.getHeight()) {
                renderComponentTooltip(matrix, Arrays.asList(new StringTextComponent(GREEN + (BOLD + "Message Type: Party")), new StringTextComponent(GRAY + "This will send all"), new StringTextComponent(GRAY + "messages send by party"), new StringTextComponent(GRAY + "members to this tab.")), mouseX, mouseY);
            } else if (mouseX >= shoutsRegex.x && mouseX < shoutsRegex.x + shoutsRegex.getWidth() && mouseY >= shoutsRegex.y && mouseY < shoutsRegex.y + shoutsRegex.getHeight()) {
                renderComponentTooltip(matrix, Arrays.asList(new StringTextComponent(GREEN + (BOLD + "Message Type: Shouts")), new StringTextComponent(GRAY + "This will send all"), new StringTextComponent(GRAY + "shouts messages"), new StringTextComponent(GRAY + "to this tab.")), mouseX, mouseY);
            } else if (mouseX >= pmRegex.x && mouseX < pmRegex.x + pmRegex.getWidth() && mouseY >= pmRegex.y && mouseY < pmRegex.y + pmRegex.getHeight()) {
                renderComponentTooltip(matrix, Arrays.asList(new StringTextComponent(GREEN + (BOLD + "Message Type: PMs")), new StringTextComponent(GRAY + "This will send all"), new StringTextComponent(GRAY + "private messages"), new StringTextComponent(GRAY + "to this tab.")), mouseX, mouseY);
            }
        }

        if (saveButton.active && mouseX >= saveButton.x && mouseX < saveButton.x + saveButton.getWidth() && mouseY >= saveButton.y && mouseY < saveButton.y + saveButton.getHeight())
            renderComponentTooltip(matrix, Arrays.asList(new StringTextComponent(GREEN + (BOLD + "Save")), new StringTextComponent(GRAY + "Click here to save"), new StringTextComponent(GRAY + "this chat tab.")), mouseX, mouseY);

        if (deleteButton.active && mouseX >= deleteButton.x && mouseX < deleteButton.x + deleteButton.getWidth() && mouseY >= deleteButton.y && mouseY < deleteButton.y + deleteButton.getHeight())
            renderComponentTooltip(matrix, Arrays.asList(new StringTextComponent(DARK_RED + (BOLD + "Delete")), new StringTextComponent(GRAY + "Click here to delete"), new StringTextComponent(GRAY + "this chat tab."), new StringTextComponent(""), new StringTextComponent(RED + "Irreversible action")), mouseX, mouseY);

        saveButton.active = !regexTextField.getValue().isEmpty() && regexValid && !nameTextField.getValue().isEmpty();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {

        saveButton.mouseClicked(mouseX, mouseY, mouseButton);
        deleteButton.mouseClicked(mouseX, mouseY, mouseButton);
        advancedButton.mouseClicked(mouseX, mouseY, mouseButton);
        closeButton.mouseClicked(mouseX, mouseY, mouseButton);
        lowPriority.mouseClicked(mouseX, mouseY, mouseButton);
        
        boolean allRegexResult = allRegex.mouseClicked(mouseX, mouseY, mouseButton);
        
        if (allRegexResult) {
        	if (allRegex.selected()) {
        		simpleRegexSettings.forEach(b -> {
        			if (!b.selected()) {
        				b.onPress();
        				}
        			});
        		}	else {
            		simpleRegexSettings.forEach(b -> {
            			if (b.selected()) {
            				b.onPress();
            				}
            			});
            		}
        	}
        
        if (allRegexResult 
        		|| localRegex.mouseClicked(mouseX, mouseY, mouseButton) 
        		|| guildRegex.mouseClicked(mouseX, mouseY, mouseButton) 
        		|| partyRegex.mouseClicked(mouseX, mouseY, mouseButton) 
        		|| shoutsRegex.mouseClicked(mouseX, mouseY, mouseButton) 
        		|| pmRegex.mouseClicked(mouseX, mouseY, mouseButton)) {
        	regexTextField.setValue(regexCreator());
        	checkIfRegexIsValid();
        }
        
//        if (button.id >= 10 && button.id <= 16) {
//        	regexTextField.setValue(regexCreator());
//        	checkIfRegexIsValid();
//        }
        nameTextField.mouseClicked(mouseX, mouseY, mouseButton);
        regexTextField.mouseClicked(mouseX, mouseY, mouseButton);
        autoCommandField.mouseClicked(mouseX, mouseY, mouseButton);
        orderNbField.mouseClicked(mouseX, mouseY, mouseButton);
    	
        // Actually we should track the return of all componentts
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean charTyped(char typedChar, int j) {
    	nameTextField.charTyped(typedChar, j);
    	autoCommandField.charTyped(typedChar, j);
        orderNbField.charTyped(typedChar, j);
        regexTextField.charTyped(typedChar, j);
    	return super.charTyped(typedChar, j);
    }
    
    @Override
    public boolean keyPressed(int typedChar, int keyCode, int j) {
    	nameTextField.keyPressed(typedChar, keyCode, j);
        autoCommandField.keyPressed(typedChar, keyCode, j);
        orderNbField.keyPressed(typedChar, keyCode, j);
        if (regexTextField.keyPressed(typedChar, keyCode, j)) checkIfRegexIsValid();
        return super.keyPressed(typedChar, keyCode, j);
    }

    boolean regexValid = false;

    private void checkIfRegexIsValid() {
        try {
            Pattern.compile(regexTextField.getValue());
            regexTextField.setTextColor(0x55FF55);
            regexValid = true;
            return;
        } catch (Exception ignored) { }

        regexTextField.setTextColor(0xFF5555);
        regexValid = false;
    }

    private Map<String, Boolean> regexSettingsCreator() {
        if (advancedButton.getMessage().getString().equals("Hide Advanced Settings")) return null;

        Map<String, Boolean> r = new HashMap<>();
        simpleRegexSettings.forEach(b-> r.put(b.getMessage().getString(), b.selected()));
        return r;
    }

    private void applyRegexSettings() {
        if (tab == null || tab.getRegexSettings() == null) return;
        tab.getRegexSettings().forEach((k, v) -> {
            for (CheckboxButton cb: simpleRegexSettings) {
                if (cb.getMessage().getString().equals(k) && v == true) {
                	cb.onPress();
                }
            }
        });
    }

    private String regexCreator() {
        if (advancedButton.getMessage().getString().equals("Hide Advanced Settings")) return "";

        Map<String, Boolean> regexSettings = regexSettingsCreator();
        List<String> result = new ArrayList<>();
        boolean allIsPresent = regexSettings.get("All");

        regexSettings.forEach((k, v) -> {
            if ((v && !allIsPresent) || (allIsPresent && !v)) {
                switch (k) {
                    case "Local":
                        result.add("^&7\\[\\d+\\*?\\/\\w{2}");
                        break;
                    case "Guild":
                        result.add(TabManager.DEFAULT_GUILD_REGEX);
                        break;
                    case "Party":
                        result.add(TabManager.DEFAULT_PARTY_REGEX);
                        break;
                    case "Shouts":
                        result.add("(^&3.*shouts:)");
                        break;
                    case "PMs":
                        result.add("(&7\\[.*\u27A4.*&7\\])");
                        break;
                }
            }
        });

        if (allIsPresent && result.size() > 0) {
            return String.format("^((?!%s).)*$", String.join("|", result));
        } else if (allIsPresent) {
            return ".*";
        } else {
            return String.join("|", result);
        }
    }
}
