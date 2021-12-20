/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.commands;

import java.util.Collection;
import java.util.Optional;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.wynntils.McIf;
import com.wynntils.core.framework.interfaces.ICommand;
import com.wynntils.core.utils.objects.Location;
import com.wynntils.modules.core.managers.CompassManager;
import com.wynntils.webapi.WebManager;
import com.wynntils.webapi.profiles.TerritoryProfile;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class CommandTerritory implements ICommand {
	public void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> commandTerritory = Commands.literal("territory")
				.requires((command) -> {
					return command.hasPermission(0);
				});
		
		commandTerritory.then(Commands.argument("territory_name", StringArgumentType.greedyString()).suggests(SUGGEST_TERRITORY).executes((command) -> {
			return execute(command, StringArgumentType.getString(command, "territory_name"));
		}));
		
		commandTerritory.executes((command) -> {
			StringTextComponent output = new StringTextComponent("Use: /territory <territory name>");
			output.withStyle(TextFormatting.RED);
			command.getSource().sendSuccess(output, false);
			return 1;
		});
		
		dispatcher.register(commandTerritory);
	}
	
	private static final SuggestionProvider<CommandSource> SUGGEST_TERRITORY = (command, builder) -> {
		Collection<TerritoryProfile> territories = WebManager.getTerritories().values();
		return ISuggestionProvider.suggest(territories.stream().map(TerritoryProfile::getFriendlyName), builder);
	};
	
	private static int execute(CommandContext<CommandSource> commandContext, String territory) {      
	
	      McIf.player().playSound(SoundEvents.PLAYER_LEVELUP, 1.0f, 10.0f);
	      
	      Collection<TerritoryProfile> territories = WebManager.getTerritories().values();

	      Optional<TerritoryProfile> selectedTerritory = territories.stream().filter(c -> c.getFriendlyName().equalsIgnoreCase(territory)).findFirst();
	      if (!selectedTerritory.isPresent()) {
	            McIf.player().playSound(SoundEvents.ANVIL_PLACE, 1.0f, 1.0f);
	            commandContext.getSource().sendFailure(new StringTextComponent("Invalid territory! Use: /territory [name] | Ex: /territory Detlas"));
	            return 0;
	        }
	      TerritoryProfile territoryProfile = selectedTerritory.get();
	      int xMiddle = territoryProfile.getStartX() + ((territoryProfile.getEndX() - territoryProfile.getStartX())/2);
	      int zMiddle = territoryProfile.getStartZ() + ((territoryProfile.getEndZ() - territoryProfile.getStartZ())/2);
	
	      CompassManager.setCompassLocation(new Location(xMiddle, 0, zMiddle));  // update compass location
	
	      StringTextComponent success = new StringTextComponent("The compass is now pointing towards " + territoryProfile.getFriendlyName() + " (" + xMiddle + ", " + zMiddle + ")");
	      success.withStyle(TextFormatting.GREEN);
	
	      StringTextComponent warn = new StringTextComponent("\nPlease be sure you know that this command redirects your compass to the middle of the territory.");
	      warn.withStyle(TextFormatting.AQUA);
	
	      success.append(warn);
	
	      StringTextComponent separator = new StringTextComponent("-----------------------------------------------------");
	      separator.withStyle(TextFormatting.DARK_GRAY, TextFormatting.STRIKETHROUGH);
	
	      commandContext.getSource().sendSuccess(separator, false);
	      commandContext.getSource().sendSuccess(success, false);
	      commandContext.getSource().sendSuccess(separator, false);
	      return 1;
	  }
}
