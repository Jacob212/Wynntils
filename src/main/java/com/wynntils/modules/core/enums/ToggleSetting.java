/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.modules.core.enums;

import com.wynntils.McIf;
import com.wynntils.core.utils.helpers.CommandResponse;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.util.regex.Pattern;

public enum ToggleSetting {

    MUSIC ("music", "You will now hear music again!", "Music is now fading out..."),
    QUEST_TRACKER("autotracking", "Your quests will now auto-track.", "Your quests will not auto-track anymore.");

    private static final Pattern TOGGLE_MESSAGE_PATTERN = Pattern.compile("(^§2)");

    String name;
    String enabledMatcher;
    String disabledMatcher;

    ToggleSetting(String name, String enabledMatcher, String disabledMatcher) {
        this.name = name;
        this.enabledMatcher = enabledMatcher;
        this.disabledMatcher = disabledMatcher;
    }

    /**
     * Updates the provided toggle setting to the defined value
     * If the setting was toggled also sends a message warning the player.
     *
     * @param value the expected value
     */
    public void set(boolean value) {
        set(value, true);
    }

    private void set(boolean value, boolean showMessage) {
        String matcher = value ? enabledMatcher : disabledMatcher;
        String inverseMatcher = value ? disabledMatcher : enabledMatcher;

        CommandResponse response = new CommandResponse("/toggle " + name, (m, t) -> {
            String message = McIf.getUnformattedText(t);
            // Try again if the current value was already the expected result
            if (!message.contains(matcher)) {
                // Make sure it's the right toggle in case we're running multiple
                if (!message.contains(inverseMatcher)) return;

                set(value, false);
                return;
            }

            // show message is false when the option was already on the value status
            if (!showMessage) return;

            McIf.sendMessage(getToggleText(value));
        }, TOGGLE_MESSAGE_PATTERN);

        response.setCancel(true);

        response.executeCommand();
    }

    private ITextComponent getToggleText(boolean value) {
        String function = value ? "enabled" : "disabled";
        String callback = value ? "disable" : "enable";

        StringTextComponent base = new StringTextComponent(
                "Wynntils automatically "
        );
        base.getStyle().withColor(TextFormatting.GRAY);

        StringTextComponent status = new StringTextComponent(function);
        status.getStyle().withColor(TextFormatting.WHITE);
        base.append(status);

        StringTextComponent continuation = new StringTextComponent(" Wynncraft toggle option ");
        continuation.getStyle().withColor(TextFormatting.GRAY);
        base.append(continuation);

        StringTextComponent toggle = new StringTextComponent(name);
        toggle.getStyle().withColor(TextFormatting.WHITE);
        base.append(toggle);

        StringTextComponent back = new StringTextComponent(" (mostly likely for conflict issues).\nTo " + callback + " it again type ");
        back.getStyle().withColor(TextFormatting.GRAY);
        base.append(back);

        StringTextComponent backCommand = new StringTextComponent("/toggle " + name);
        backCommand.getStyle().withColor(TextFormatting.WHITE);
        backCommand.getStyle().setUnderlined(true);
        backCommand.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/toggle " + name));
        backCommand.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new StringTextComponent("Click to run /toggle " + name))
        );
        base.append(backCommand);

        StringTextComponent end = new StringTextComponent(".\n");
        end.getStyle().withColor(TextFormatting.GRAY);
        base.append(end);

        return base;
    }

}
