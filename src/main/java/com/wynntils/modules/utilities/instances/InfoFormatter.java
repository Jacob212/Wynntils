/*
 *  * Copyright © Wynntils - 2018 - 2020.
 */

package com.wynntils.modules.utilities.instances;

import com.wynntils.core.utils.ItemUtils;
import com.wynntils.core.utils.StringUtils;
import com.wynntils.Reference;
import com.wynntils.core.framework.instances.PlayerInfo;
import com.wynntils.core.framework.instances.PlayerInfo.HorseData;
import com.wynntils.core.framework.instances.PlayerInfo.UnprocessedAmount;
import com.wynntils.core.utils.Utils;
import com.wynntils.core.utils.reference.EmeraldSymbols;
import com.wynntils.modules.core.managers.PingManager;
import com.wynntils.modules.richpresence.RichPresenceModule;
import com.wynntils.modules.utilities.interfaces.InfoModule;
import com.wynntils.modules.utilities.managers.SpeedometerManager;
import net.minecraft.client.Minecraft;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InfoFormatter {
    private Minecraft mc = Minecraft.getMinecraft();

    public HashMap<String, String> cache = new HashMap<String, String>();
    public HashMap<String, InfoModule> formatters = new HashMap<String, InfoModule>();

    private int tick = 0;

    private static final Pattern formatRegex = Pattern.compile(
        "%([a-zA-Z_]+|%)%|\\\\([\\\\n%§EBLMH]|x[0-9A-Fa-f]{2}|u[0-9A-Fa-f]{4}|U[0-9A-Fa-f]{8})"
    );

    public InfoFormatter() {
        // Escape for % character
        registerFormatter((input) -> {
            return "%";
        }, "%");

        // Blocks per second
        registerFormatter((input) -> {
            return PlayerInfo.perFormat.format(SpeedometerManager.getCurrentSpeed());
        }, "bps");

        // Blocks per minute
        registerFormatter((input) -> {
            return PlayerInfo.perFormat.format(SpeedometerManager.getCurrentSpeed() * 60);
        }, "bpm");

        // Kilometers per hour (1000 blocks per hour)
        registerFormatter((input) -> {
            return PlayerInfo.perFormat.format(SpeedometerManager.getCurrentSpeed() * 3.6);
        }, "kmph");

        // X coordinate
        registerFormatter((input) -> {
            return Integer.toString((int) mc.player.posX);
        }, "x");

        // Y coordinate
        registerFormatter((input) -> {
            return Integer.toString((int) mc.player.posY);
        }, "y");

        // Z coordinate
        registerFormatter((input) -> {
            return Integer.toString((int) mc.player.posZ);
        }, "z");

        // The facing cardinal direction
        registerFormatter((input) -> {
            return Utils.getPlayerDirection(mc.player.rotationYaw);
        }, "dir");

        // Frames per second
        registerFormatter((input) -> {
            return Integer.toString(Minecraft.getDebugFPS());
        }, "fps");

        // The world/server number
        registerFormatter((input) -> {
            return Reference.getUserWorld();
        }, "world");

        // The ping time to the server
        registerFormatter((input) -> {
            PingManager.calculatePing();
            return Long.toString(PingManager.getLastPing());
        }, "ping");

        // The wall clock time, formatted in the current locale style
        registerFormatter((input) -> {
            LocalDateTime date = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
            return date.format(formatter);
        }, "clock");

        // Current mana
        registerFormatter((input) -> {
            return Integer.toString(PlayerInfo.getPlayerInfo().getCurrentMana());
        },"mana");

        // Max mana
        registerFormatter((input) -> {
            return Integer.toString(PlayerInfo.getPlayerInfo().getMaxMana());
        }, "mana_max");

        // Current health
        registerFormatter((input) -> {
            return Integer.toString(PlayerInfo.getPlayerInfo().getCurrentHealth());
        }, "health");

        // Max health
        registerFormatter((input) -> {
            return Integer.toString(PlayerInfo.getPlayerInfo().getMaxHealth());
        }, "health_max");

        // Current XP (formatted)
        registerFormatter((input) -> {
            return StringUtils.integerToShortString(PlayerInfo.getPlayerInfo().getCurrentXP());
        }, "xp");

        // Current XP (raw)
        registerFormatter((input) -> {
            return Long.toString(PlayerInfo.getPlayerInfo().getCurrentXP());
        }, "xp_raw");

        // XP required to level up (formatted)
        registerFormatter((input) -> {
            return StringUtils.integerToShortString(PlayerInfo.getPlayerInfo().getXpNeededToLevelUp());
        }, "xp_req");

        // XP required to level up (raw)
        registerFormatter((input) -> {
            return Long.toString(PlayerInfo.getPlayerInfo().getXpNeededToLevelUp());
        }, "xp_req_raw");

        // Percentage XP to next level
        registerFormatter((input) -> {
            return PlayerInfo.getPlayerInfo().getCurrentXPAsPercentage();
        }, "xp_pct");

        // Horse XP
        registerFormatter((input) -> {
            if (!cache.containsKey("horsexp")) {
                cacheHorseData();
            }

            return cache.get("horsexp");
        }, "horse_xp");

        // Horse Level
        registerFormatter((input) -> {
            if (!cache.containsKey("horselevel")) {
                cacheHorseData();
            }

            return cache.get("horselevel");
        }, "horse_level");

        // Max horse level
        registerFormatter((input) -> {
            if (!cache.containsKey("horselevelmax")) {
                cacheHorseData();
            }

            return cache.get("horselevelmax");
        }, "horse_level_max");

        // Horse Tier
        registerFormatter((input) -> {
            if (!cache.containsKey("horsetier")) {
                cacheHorseData();
            }

            return cache.get("horsetier");
        }, "horse_tier");

        // Number of items in ingredient pouch
        registerFormatter((input) -> {
            return Integer.toString(PlayerInfo.getPlayerInfo().getIngredientPouchCount(false));
        }, "pouch");

        // Number of free slots in ingredient pouch
        registerFormatter((input) -> {
            return Integer.toString(27 - PlayerInfo.getPlayerInfo().getIngredientPouchCount(true));
        }, "pouch_free");

        // Number of used slots in ingredient pouch
        registerFormatter((input) -> {
            return Integer.toString(PlayerInfo.getPlayerInfo().getIngredientPouchCount(true));
        }, "pouch_slots");

        // Number of free slots in the inventory
        registerFormatter((input) -> {
            return Integer.toString(PlayerInfo.getPlayerInfo().getFreeInventorySlots());
        }, "inv_free");

        // Number of used slots in the inventory
        registerFormatter((input) -> {
            return Integer.toString(28 - PlayerInfo.getPlayerInfo().getFreeInventorySlots());
        }, "inv_slots");

        // Current location (town)
        registerFormatter((input) -> {
            return RichPresenceModule.getModule().getData().getLocation();
        }, "location", "loc");

        // Current level
        registerFormatter((input) -> {
            return Integer.toString(PlayerInfo.getPlayerInfo().getLevel());
        }, "level", "lvl");

        // Time until soul point (formatted)
        registerFormatter((input) -> {
            if (!cache.containsKey("soulpointtimer")) {
                cacheSoulPointTimer();
            }

            return cache.get("soulpointtimer");
        }, "soulpoint_timer", "sp_timer");

        // Minutes until soul point
        registerFormatter((input) -> {
            if (!cache.containsKey("soulpointsminutes")) {
                cacheSoulPointTimer();
            }

            return cache.get("soulpointsminutes");
        },"soulpoint_timer_m", "sp_timer_m");

        // Seconds until soul point
        registerFormatter((input) -> {
            if (!cache.containsKey("soulpointseconds")) {
                cacheSoulPointTimer();
            }

            return cache.get("soulpointseconds");
        }, "soulpointtimer_s", "sptimer_s");

        // Current soul points
        registerFormatter((input) -> {
            return Integer.toString(PlayerInfo.getPlayerInfo().getSoulPoints());
        }, "soulpoints", "sp");

        // Max soul points
        registerFormatter((input) -> {
            return Integer.toString(PlayerInfo.getPlayerInfo().getMaxSoulPoints());
        }, "soulpoints_max", "sp_max");

        // Total money in inventory
        registerFormatter((input) -> {
            if (!cache.containsKey("money")) {
                cacheMoney();
            }

            return cache.get("money");
        }, "money");

        // Total money in inventory, formatted as le, blocks and emeralds
        registerFormatter((input) -> {
            if (!cache.containsKey("money_desc")) {
                cacheMoney();
            }

            return cache.get("money_desc");
        }, "money_desc");

        // Count of full liquid emeralds in inventory
        registerFormatter((input) -> {
            if (!cache.containsKey("liquid")) {
                cacheMoney();
            }

            return cache.get("liquid");
        }, "le", "liquidemeralds");

        // Count of full emerald blocks in inventory (excluding that in liquid emerald counter)
        registerFormatter((input) -> {
            if (!cache.containsKey("blocks")) {
                cacheMoney();
            }

            return cache.get("blocks");
        }, "eb", "blocks", "emeraldblocks");

        // Count of emeralds in inventory (excluding that in liquid emerald and block counters)
        registerFormatter((input) -> {
            if (!cache.containsKey("emeralds")) {
                cacheMoney();
            }

            return cache.get("emeralds");
        }, "e", "emeralds");

        // Count of health potions
        registerFormatter((input) -> {
            return Integer.toString(PlayerInfo.getPlayerInfo().getHealthPotions());
        }, "potions_health");

        // Count of mana potions
        registerFormatter((input) -> {
            return Integer.toString(PlayerInfo.getPlayerInfo().getManaPotions());
        }, "potions_mana");

        // Current class
        registerFormatter((input) -> {
            String className = PlayerInfo.getPlayerInfo().getCurrentClass().name().toLowerCase();

            if (input.equals("Class")) {  // %Class% is title case
                className = StringUtils.capitalizeFirst(className);
            } else if (input.equals("CLASS")) {  // %CLASS% is all caps
                className = className.toUpperCase();
            }

            return className;
        }, "class");

        // Max allocated memory
        registerFormatter((input) -> {
            if (!cache.containsKey("memorymax")) {
                cacheMemory();
            }

            return cache.get("memorymax");
        }, "memmax", "mem_max");

        // Current used memory
        registerFormatter((input) -> {
            if (!cache.containsKey("memoryused")) {
                cacheMemory();
            }

            return cache.get("memoryused");
        }, "memused", "mem_used");

        // Current used memory percent
        registerFormatter((input) -> {
            if (!cache.containsKey("memorypct")) {
                cacheMemory();
            }

            return cache.get("memorypct");
        }, "mempct", "mem_pct");

        // Current amount of unprocessed materials
        registerFormatter((input) -> {
            if(!cache.containsKey("unprocessedcurrent")) {
                cacheUnprocessed();
            }

            return cache.get("unprocessedcurrent");
        }, "unprocessed");

        // Max amount of unprocessed materials
        registerFormatter((input) -> {
            if(!cache.containsKey("unprocessedmax")) {
                cacheUnprocessed();
            }
            return cache.get("unprocessedmax");
        }, "unprocessed_max");

        // Number of players in the party
        registerFormatter((input) -> {
            return Integer.toString(PlayerInfo.getPlayerInfo().getPlayerParty().getPartyMembers().size());
        }, "party_count");

        // Owner of players party
        registerFormatter((input) -> {
            return PlayerInfo.getPlayerInfo().getPlayerParty().getOwner();
        }, "party_owner");
    }

    private void registerFormatter(InfoModule formatter, String... vars) {
        for (String var : vars) {
            formatters.put(var, formatter);
        }
    }

    String doEscapeFormat(String escaped) {
        switch (escaped) {
            case "\\": return "\\\\";
            case "n": return "\n";
            case "%": return "%";
            case "§": return "&";
            case "E": return EmeraldSymbols.E_STRING;
            case "B": return EmeraldSymbols.B_STRING;
            case "L": return EmeraldSymbols.L_STRING;
            case "M": return "✺";
            case "H": return "❤";
            default:
                // xXX, uXXXX, UXXXXXXXX
                int codePoint = Integer.parseInt(escaped.substring(1), 16);
                if (Utils.StringUtils.isValidCodePoint(codePoint)) {
                    return new String(new int[]{ codePoint }, 0, 1);
                }
                return null;
        }
    }

    public String doFormat(String format) {
        StringBuffer sb = new StringBuffer(format.length() + 10);
        Matcher m = formatRegex.matcher(format);
        while (m.find()) {
            String replacement = null;
            String group;
            if ((group = m.group(1)) != null) {
                // %name%
                InfoModule module = formatters.getOrDefault(group.toLowerCase(), null);
                if (module != null) {
                    replacement = module.generate(group);
                }

            } else if ((group = m.group(2)) != null) {
                // \escape
                replacement = doEscapeFormat(group);
            }
            if (replacement == null) {
                replacement = m.group(0);
            }
            m.appendReplacement(sb, replacement);
        }
        m.appendTail(sb);

        tick++;

        if (tick > 4) {
            cache.clear();
            tick = 0;
        }

        return sb.toString();
    }

    private void cacheMoney() {
        int total = PlayerInfo.getPlayerInfo().getMoney();

        String eb = Integer.toString(total / 64);
        String em = Integer.toString(total % 64);
        String le = Integer.toString(total / 4096);
        String output = Integer.toString(total);

        cache.put("blocks", eb);
        cache.put("liquid", le);
        cache.put("emeralds", em);
        cache.put("money", output);
        cache.put("money_desc", ItemUtils.describeMoney(total));
    }

    private void cacheMemory() {
        long max = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        long used = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);

        int pct = (int) (((float) used / max) * 100f);

        cache.put("memorymax", Long.toString(max));
        cache.put("memoryused", Long.toString(used));
        cache.put("memorypct", Long.toString(pct));
    }

    private void cacheSoulPointTimer() {
        int totalseconds = PlayerInfo.getPlayerInfo().getTicksToNextSoulPoint() / 20;

        int seconds = totalseconds % 60;
        int minutes = totalseconds / 60;
        String timer = String.format("%d:%02d", minutes, seconds);

        cache.put("soulpointtimer", timer);
        cache.put("soulpointminutes", Integer.toString(minutes));
        cache.put("soulpointseconds", Integer.toString(seconds));
    }

    private void cacheHorseData() {
        HorseData horse = PlayerInfo.getPlayerInfo().getHorseData();

        if (horse == null) {
            cache.put("horselevel", "??");
            cache.put("horsexp", "??");
            cache.put("horsetier", "?");
            cache.put("horselevelmax", "??");

            return;
        }

        cache.put("horselevel", Integer.toString(horse.level));
        cache.put("horsexp", Integer.toString(horse.xp));
        cache.put("horsetier", Integer.toString(horse.tier));
        cache.put("horselevelmax", Integer.toString(horse.maxLevel));
    }

    private void cacheUnprocessed() {
        UnprocessedAmount unproc = PlayerInfo.getPlayerInfo().getUnprocessedAmount();

        if (unproc.maximum == -1) {
            cache.put("unprocessedmax", "??");
        } else {
            cache.put("unprocessedmax", Integer.toString(unproc.maximum));
        }

        cache.put("unprocessedcurrent", Integer.toString(unproc.current));
    }
}

