/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.modules.core.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.wynntils.McIf;
import com.wynntils.Reference;
import com.wynntils.core.framework.interfaces.ICommand;
import com.wynntils.core.framework.rendering.textures.Textures;
import com.wynntils.core.utils.helpers.Delay;
import com.wynntils.modules.core.config.CoreDBConfig;
import com.wynntils.modules.core.enums.UpdateStream;
import com.wynntils.modules.core.overlays.ui.ChangelogUI;
import com.wynntils.modules.richpresence.RichPresenceModule;
import com.wynntils.modules.richpresence.profiles.RichProfile;
import com.wynntils.modules.utilities.managers.KeyManager;
import com.wynntils.webapi.WebManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.util.ArrayList;
import java.util.List;

public class CommandWynntils implements ICommand {
	public void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> commandWynntils = Commands.literal("wynntils")
				.requires((command) -> {
					return command.hasPermission(0);
				});

		//donate
		commandWynntils.then(Commands.literal("donate").executes((command) -> {
			StringTextComponent c = new StringTextComponent("You can donate to Wynntils at: ");
            c.withStyle(TextFormatting.AQUA);
            StringTextComponent url = new StringTextComponent("https://www.patreon.com/Wynntils");
            url.setStyle(Style.EMPTY.withColor(TextFormatting.LIGHT_PURPLE).setUnderlined(true)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.patreon.com/Wynntils"))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Click here to open in your browser."))));
            url.append(c);
            command.getSource().sendSuccess(url, false);
            return 1;
		}));
		
		//discord
		commandWynntils.then(Commands.literal("discord").executes((command) -> {
			StringTextComponent msg = new StringTextComponent("You're welcome to join our Discord server at:\n");
	        msg.withStyle(TextFormatting.GOLD);
	        String discordInvite = WebManager.getApiUrls() == null ? null : WebManager.getApiUrls().get("DiscordInvite");
	        StringTextComponent link = new StringTextComponent(discordInvite == null ? "<Wynntils servers are down>" : discordInvite);
	        link.withStyle(TextFormatting.DARK_AQUA);
	        if (discordInvite != null) {
	            link.setStyle(Style.EMPTY
	            		.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, discordInvite))
		                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Click here to join our Discord server.")))
		                );

	            RichProfile.OverlayManager o = RichPresenceModule.getModule().getRichPresence().getOverlayManager();
	            if (o != null) {
	                o.openGuildInvite(discordInvite.replace("https://discord.gg/", ""));
	            }
	        }
	        command.getSource().sendSuccess(msg.append(link), false);
			return 1;
		}));
		
		//version
		commandWynntils.then(Commands.literal("version").executes((command) -> {
			return handleModVersion(command);
		}));
		
		//reloadapi
		commandWynntils.then(Commands.literal("reloadapi").executes((command) -> {
			WebManager.reset();
	        WebManager.setupUserAccount();
	        WebManager.setupWebApi(false);
			return 1;
		}));
		
		//changelog TODO command works but need to check that it shows the changelog correctly
		commandWynntils.then(Commands.literal("changelog")
				.then(Commands.literal("latest").executes((command) -> {
					new Delay(() -> ChangelogUI.loadChangelogAndShow(CoreDBConfig.INSTANCE.updateStream == UpdateStream.STABLE, true), 1);
					return 1;
					}))
				.then(Commands.literal("major").executes((command) -> {
					new Delay(() -> ChangelogUI.loadChangelogAndShow(true, false), 1);
					return 1;
					}))
		.executes((command) -> {
			new Delay(() -> ChangelogUI.loadChangelogAndShow(CoreDBConfig.INSTANCE.updateStream == UpdateStream.STABLE, false), 1);
			return 1;
		}));
		
		//debug
		commandWynntils.then(Commands.literal("debug").executes((command) -> {
			if (!Reference.developmentEnvironment) {
				StringTextComponent message = new StringTextComponent(TextFormatting.RED + "You can't use this command outside a development environment");
	
	            McIf.sendMessage(message);
	            return 1;
	        }
	
	        Textures.loadTextures();
			return 1;
		}));
		
		//help
		List<StringTextComponent> help = new ArrayList<>();
		help.add(new StringTextComponent(""));
        help.get(0).getStyle().withColor(TextFormatting.GOLD);
        help.get(0).append("Wynntils' command list: ");
        help.get(0).append("\n");
        addCommandDescription(help.get(0), "-wynntils", " help", "This shows a list of all available commands for Wynntils.");
        help.get(0).append("\n");
        addCommandDescription(help.get(0), "-wynntils", " discord", "This provides you with an invite to our Discord server.");
        help.get(0).append("\n");
        addCommandDescription(help.get(0), "-wynntils", " version", "This shows the installed Wynntils version.");
        help.get(0).append("\n");
        addCommandDescription(help.get(0), "-wynntils", " changelog [major/latest]", "This shows the changelog of your installed version.");
        help.get(0).append("\n");
        addCommandDescription(help.get(0), "-wynntils", " reloadapi", "This reloads all API data.");
        help.get(0).append("\n");
        addCommandDescription(help.get(0), "-wynntils", " donate", "This provides our Patreon link.");
        help.get(0).append("\n");
        addCommandDescription(help.get(0), "-", "token", "This provides a clickable token for you to create a Wynntils account to manage your cosmetics.");
        help.get(0).append("\n");
        addCommandDescription(help.get(0), "-", "compass", "This makes your compass point towards an x and z or a direction (e.g. north, SE).");
        help.get(0).append("\n");
        addCommandDescription(help.get(0), "-", "territory", "This makes your compass point towards a specified territory.");
        help.get(0).append("\n")
        .append("Page 1 (out of 2) ")
        .append(new StringTextComponent(">>>").setStyle(Style.EMPTY
        		.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wynntils help 2"))
        		.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Go to the next page")))));
        
        help.add(new StringTextComponent(""));
        help.get(1).getStyle().withColor(TextFormatting.GOLD);
        help.get(1).append("Wynntils' command list: ");
        help.get(1).append("\n");
        addCommandDescription(help.get(1), "-", "lootrun", "This allows you to record and display lootrun paths.");
        help.get(1).append("\n");
        addCommandDescription(help.get(1), "-", "s", "This lists all online worlds in Wynncraft and the details of each world.");
        help.get(1).append("\n");
        addCommandDescription(help.get(1), "-", "exportdiscoveries", "This exports all discovered discoveries as a .csv file.");
        help.get(1).append("\n");
        addCommandDescription(help.get(1), "-", "forceupdate", "This downloads and installs the latest successful build.");
        help.get(1).append("\n")
                .append(new StringTextComponent("<<<").setStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wynntils help 1"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Go to the next page")))))
                .append(new StringTextComponent(" Page 2 (out of 2)"));
		
		commandWynntils.then(Commands.literal("help").then(Commands.argument("page", IntegerArgumentType.integer(1,2)).executes((command -> {
			command.getSource().sendSuccess(help.get(IntegerArgumentType.getInteger(command, "page")-1), false);
			return 1;
		})))).executes((command) -> {
			command.getSource().sendSuccess(help.get(0), false);
			return 1;
		});

		commandWynntils.executes((command) -> {
			command.getSource().sendSuccess(help.get(0), false);
			return 1;
		});
		
		dispatcher.register(commandWynntils);
	}

    private static void addCommandDescription(StringTextComponent text, String prefix, String name, String description) {
        StringTextComponent prefixText = new StringTextComponent(prefix);
        prefixText.withStyle(TextFormatting.DARK_GRAY);
        text.append(prefixText);

        StringTextComponent nameText = new StringTextComponent(name);
        nameText.withStyle(TextFormatting.RED);
        text.append(nameText);

        text.append(" ");

        StringTextComponent descriptionText = new StringTextComponent(description);
        descriptionText.withStyle(TextFormatting.GRAY);
        text.append(descriptionText);
    }

    private static int handleModVersion(CommandContext<CommandSource> commandContext) {
        if (Reference.developmentEnvironment) {
            StringTextComponent text = new StringTextComponent("Wynntils is running in a development environment.");
            text.getStyle().withColor(TextFormatting.GOLD);
            commandContext.getSource().sendSuccess(text, false);
            return 1;
        }

        StringTextComponent releaseStreamText;
        StringTextComponent buildText;
        if (CoreDBConfig.INSTANCE.updateStream == UpdateStream.STABLE) {
            releaseStreamText = new StringTextComponent("You are using Stable release stream: ");
            buildText = new StringTextComponent("Version " + Reference.VERSION);
        } else {
            releaseStreamText = new StringTextComponent("You are using Cutting Edge release stream: ");
            if (Reference.BUILD_NUMBER == -1) {
                buildText = new StringTextComponent("Unknown Build");
            } else {
                buildText = new StringTextComponent("Build " + Reference.BUILD_NUMBER);
            }
        }
        releaseStreamText.withStyle(TextFormatting.GOLD);
        buildText.withStyle(TextFormatting.YELLOW);
        StringTextComponent versionText = new StringTextComponent("");
        versionText.append(releaseStreamText);
        versionText.append(buildText);

        //TODO This part might not work, needs checking
        StringTextComponent updateCheckText;
        if (WebManager.getUpdate().updateCheckFailed()) {
            updateCheckText = new StringTextComponent("Wynntils failed to check for updates. Press " + KeyManager.getCheckForUpdatesKey().getKeyBinding().getName() + " to try again.");
            updateCheckText.withStyle(TextFormatting.DARK_RED);
        } else if (WebManager.getUpdate().hasUpdate()) {
            updateCheckText = new StringTextComponent("Wynntils is currently outdated. Press " + KeyManager.getCheckForUpdatesKey().getKeyBinding().getName() + " to update now.");
            updateCheckText.withStyle(TextFormatting.DARK_RED);
        } else {
            updateCheckText = new StringTextComponent("Wynntils was up-to-date when last checked. Press " + KeyManager.getCheckForUpdatesKey().getKeyBinding().getName() + " to check for updates.");
            updateCheckText.withStyle(TextFormatting.DARK_GREEN);
        }
        commandContext.getSource().sendSuccess(updateCheckText, false);
        return 1;
    }
	
}

