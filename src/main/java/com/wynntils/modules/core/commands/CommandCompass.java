/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.modules.core.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.wynntils.McIf;
import com.wynntils.core.framework.interfaces.ICommand;
import com.wynntils.core.utils.objects.Location;
import com.wynntils.modules.core.managers.CompassManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.Vec2Argument;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.*;

public class CommandCompass implements ICommand {
	public void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> commandCompass = Commands.literal("compass")
				.requires((command) -> {
					return command.hasPermission(0);
				});
		
		//direction
		commandCompass.then(Commands.argument("direction", StringArgumentType.word()).suggests(SUGGEST_DIRECTION)
		.executes((command) -> {
			return direction(command.getSource(), StringArgumentType.getString(command, "direction"));
		}));
		
		//share TODO add suggestions for first and second argument
		commandCompass.then(Commands.literal("share").then(Commands.argument("first", StringArgumentType.word()).then(Commands.argument("second", StringArgumentType.word())
				.executes((command) -> {
					return share(command.getSource(), StringArgumentType.getString(command, "first"), StringArgumentType.getString(command, "second"));
				}))
				.executes((command) -> {
					return share(command.getSource(), StringArgumentType.getString(command, "first"), null);
				}))
				.executes((command) -> {
					return share(command.getSource(), null, null);
				}));
		
		//Coordinate
		commandCompass.then(Commands.argument("coordinate", Vec2Argument.vec2()).executes((command) -> {
			CompassManager.setCompassLocation(new Location(Vec2Argument.getVec2(command, "coordinate").x, 0, Vec2Argument.getVec2(command, "coordinate").y));
			StringTextComponent text = new StringTextComponent("");
            text.withStyle(TextFormatting.GREEN);
            text.append("Compass is now pointing towards (");

            StringTextComponent xCoordinateText = new StringTextComponent(Float.toString(Vec2Argument.getVec2(command, "coordinate").x));
            xCoordinateText.withStyle(TextFormatting.DARK_GREEN);
            text.append(xCoordinateText);

            text.append(", ");

            StringTextComponent zCoordinateText = new StringTextComponent(Float.toString(Vec2Argument.getVec2(command, "coordinate").y));
            zCoordinateText.withStyle(TextFormatting.DARK_GREEN);
            text.append(zCoordinateText);

            text.append(").");
            command.getSource().sendSuccess(text, false);
			return 1;
		}));
				
		//clear
		commandCompass.then(Commands.literal("clear").executes((command) -> {
			if (CompassManager.getCompassLocation() != null) {
                CompassManager.reset();

                StringTextComponent text = new StringTextComponent("The beacon and icon of your desired coordinates have been cleared.");
                text.withStyle(TextFormatting.GREEN);
                command.getSource().sendSuccess(text, false);
                return 1;
            }
            command.getSource().sendFailure(new StringTextComponent("There is nothing to be cleared as you have not set any coordinates to be displayed as a beacon and icon."));
            return 1;
		}));
		
		//help
		commandCompass.executes((command) -> {
			command.getSource().sendFailure(new StringTextComponent("/" + USAGE));
			return 1;
		});
		
		dispatcher.register(commandCompass);
	}
	
    private static final String USAGE = "compass [<x> <z> | <direction> | clear | share [location] [guild|party|user]";

	private static HashMap<String, int[]> directions = new HashMap<String, int[]>() {{
		put("north", new int[] {0, -9999999});
		put("northeast", new int[] {9999999, -9999999});
		put("northwest", new int[] {-9999999, -9999999});
		put("south", new int[] {0, 9999999});
		put("southeast", new int[] {9999999, 9999999});
		put("southwest", new int[] {-9999999, 9999999});
		put("east", new int[] {9999999, 0});
		put("west", new int[] {-9999999, 0});
	}};
	
	private static final SuggestionProvider<CommandSource> SUGGEST_DIRECTION = (command, builder) -> {
		return ISuggestionProvider.suggest(directions.keySet().stream(), builder);
	};
    
    private int direction(CommandSource command, String dir) {
		switch (dir.toLowerCase(Locale.ROOT)) {
        case "n":
            dir = "north";
            break;
        case "ne":
            dir = "northeast";
            break;
        case "nw":
            dir = "northwest";
            break;
        case "s":
            dir = "south";
            break;
        case "se":
            dir = "southeast";
            break;
        case "sw":
            dir = "southwest";
            break;
        case "e":
            dir = "east";
            break;
        case "w":
            dir = "west";
            break;
        default:
            break;
            }
		if (!directions.containsKey(dir)) {
			command.sendFailure(new StringTextComponent("Invalid direction"));
			return 0;
		}
		CompassManager.setCompassLocation(new Location(directions.get(dir)[0], 0, directions.get(dir)[1]));

        dir = dir.substring(0, 1).toUpperCase() + dir.substring(1);
        StringTextComponent text = new StringTextComponent("Compass is now pointing towards ");
        text.withStyle(TextFormatting.GREEN);

        StringTextComponent directionText = new StringTextComponent(dir);
        directionText.withStyle(TextFormatting.DARK_GREEN);
        text.append(directionText);

        text.append(".");
        command.sendSuccess(text, false);            
		return 1;
    }

	private int share(CommandSource command, String first, String second) {
		String recipientUser = null;
		String type;
		double x;
        double z;
        if (first != null && first.equalsIgnoreCase("location")) {
        	// Use current location instead of compass
        	x = McIf.player().getX();
        	z = McIf.player().getZ();
        	type = "location";
        	if (second != null && !second.equalsIgnoreCase("party")) {
        		recipientUser = second;
        	}
        } else {
        	Location location = CompassManager.getCompassLocation();
        	if (location == null) {
        		command.sendFailure(new StringTextComponent("No compass location set (did you mean /compass share location?)"));
        		return 1;
        	}
        	x = location.getX();
        	z = location.getZ();
        	type = "compass";
        	if (first != null && !first.equalsIgnoreCase("party")) {
        		recipientUser = first;
        	}
        }
        shareCoordinates(recipientUser, type, (int) x, (int) z);
        return 1;
	}
    
    public static void shareCoordinates(String recipientUser, String type, int x, int z) {
        String location = "[" + x + ", " + z + "]";
        if (recipientUser == null) {
            McIf.player().chat("/p " + " My " + type + " is at " + location);
        }else if (recipientUser.equalsIgnoreCase("guild")) {
            McIf.player().chat("/g " + " My " + type + " is at " + location);
        } else {
            McIf.player().chat("/msg " + recipientUser + " My " + type + " is at " + location);
        }
    }
}
