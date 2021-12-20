/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.wynntils.McIf;
import com.wynntils.core.framework.interfaces.ICommand;
import com.wynntils.modules.core.enums.AccountType;
import com.wynntils.modules.core.managers.UserManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class CommandAdmin implements ICommand {
	public void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> commandAdmin = Commands.literal("wadmin")
				.requires((command) -> {
					return UserManager.isAccountType(McIf.player().getUUID(), AccountType.MODERATOR);					
		});
		
		commandAdmin.then(Commands.literal("broadcast")
				.then(Commands.argument("type", StringArgumentType.word()).then(Commands.argument("message", StringArgumentType.greedyString())
						.executes((command) -> {
							//Dont know if this command is still used or what?
//							SocketManager.emitEvent("sendBroadcast", StringArgumentType.getString(command, "type"), StringArgumentType.getString(command, "message"));
							return 1;
							}))));
		commandAdmin.executes((command) -> {
			StringTextComponent output = new StringTextComponent("Use: /wadmin broadcast <TITLE/MESSAGE> <message>");
			output.withStyle(TextFormatting.RED);
			command.getSource().sendSuccess(output, false);
			return 1;
			});
		
		dispatcher.register(commandAdmin);
	}
}
