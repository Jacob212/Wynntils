/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

import com.wynntils.core.framework.interfaces.ICommand;
import com.wynntils.webapi.WebManager;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class CommandForceUpdate implements ICommand {
	public void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> commandForceUpdate = Commands.literal("forceupdate")
				.requires((command) -> {
					return command.hasPermission(0);
				});
		
		commandForceUpdate.executes((command) -> {
			WebManager.getUpdate().forceUpdate();
	        StringTextComponent text = new StringTextComponent("Forcing Wynntils to update...");
	        text.withStyle(TextFormatting.AQUA);
	        command.getSource().sendSuccess(text, false);
	        return 1;
				});
		
		dispatcher.register(commandForceUpdate);
	}
}
