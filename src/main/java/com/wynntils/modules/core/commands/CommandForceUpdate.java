/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
//import com.wynntils.webapi.WebManager;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class CommandForceUpdate {
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> commandForceUpdate = Commands.literal("forceupdate")
				.requires((command) -> {
					return true;
//					return command.hasPermission(0);
				}).executes((command) -> {
					return update(command);
				});
		
		
		dispatcher.register(commandForceUpdate);
	}
	
	static int update(CommandContext<CommandSource> commandContext) {
//		WebManager.getUpdate().forceUpdate();

        StringTextComponent text = new StringTextComponent("Forcing Wynntils to update...");
        text.getStyle().withColor(TextFormatting.AQUA);
        commandContext.getSource().getEntity().sendMessage(text, commandContext.getSource().getEntity().getUUID());;
        return 1;
	}
}
