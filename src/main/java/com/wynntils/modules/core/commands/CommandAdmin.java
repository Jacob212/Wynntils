/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
//import com.wynntils.modules.core.enums.AccountType;
//import com.wynntils.modules.core.managers.UserManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class CommandAdmin {
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> commandAdmin = Commands.literal("wadmin")
				.requires((command) -> {
//					return UserManager.isAccountType(McIf.player().getUUID(), AccountType.MODERATOR);
					return true;
		}).then(Commands.literal("broadcast").then(Commands.argument("type", StringArgumentType.word())
				.then(Commands.argument("message", StringArgumentType.greedyString()).executes((command) -> {
					return execute(command, StringArgumentType.getString(command, "type"), StringArgumentType.getString(command, "message"));
					}))))
				
				.executes((command) -> {
					return error(command);
				});
		dispatcher.register(commandAdmin);
	}
	
	static int execute(CommandContext<CommandSource> commandContext, String type, String message) throws CommandSyntaxException {
		System.out.println(message);
		ITextComponent output = new StringTextComponent(message);
		
//		SocketManager.emitEvent("sendBroadcast", type, message);
		commandContext.getSource().getServer().getPlayerList().broadcastMessage(output, ChatType.CHAT, commandContext.getSource().getEntity().getUUID());;
		return 1;
	}
	
	static int error(CommandContext<CommandSource> commandContext) {
		ITextComponent output = new StringTextComponent("Use: /wadmin broadcast");
		output.getStyle().withColor(TextFormatting.RED);
		commandContext.getSource().getEntity().sendMessage(output, commandContext.getSource().getEntity().getUUID());
		return 1;
	}
}
