/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.wynntils.core.framework.interfaces.ICommand;
import com.wynntils.webapi.WebManager;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

public class CommandToken implements ICommand {
	public void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> commandToken = Commands.literal("token")
				.requires((command) -> {
					return command.hasPermission(0);
				});
		commandToken.executes((command) -> {
					return execute(command);
				});
		dispatcher.register(commandToken);
	}
	
	static int execute(CommandContext<CommandSource> commandContext) {
		if (WebManager.getAccount().getToken() != null) {
            ITextComponent text = new StringTextComponent("Wynntils Token ");
            text.getStyle().withColor(TextFormatting.AQUA);

            ITextComponent token = new StringTextComponent(WebManager.getAccount().getToken());

            token.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                    "https://account.wynntils.com/register.php?token=" + WebManager.getAccount().getToken()));
            token.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new StringTextComponent("Click me to register an account.")));

            token.getStyle().withColor(TextFormatting.DARK_AQUA);
            token.getStyle().setUnderlined(true);
            text.getSiblings().add(token);

            commandContext.getSource().sendSuccess(token, false);
            return 1;
        }

        StringTextComponent text = new StringTextComponent("Error when getting token, try restarting your client");
        text.getStyle().withColor(TextFormatting.RED);
        
        commandContext.getSource().sendSuccess(text, false);
		return 1;
	}
}
