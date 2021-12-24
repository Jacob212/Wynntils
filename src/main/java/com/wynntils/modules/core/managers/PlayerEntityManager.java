/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.managers;

import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerEntityManager {
    private static Map<UUID, PlayerEntity> map = new HashMap<>();

    /**
     * @param uuid UUID of player
     * @return The {@link PlayerEntity} with the given uuid, or null if no such player exists
     */
    public static PlayerEntity getPlayerByUUID(UUID uuid) {
        return map.get(uuid);
    }

    /**
     * @param uuid UUID of player
     * @return If true, {@link #getPlayerByUUID(UUID)} will not return null.
     */
    public static boolean containsUUID(UUID uuid) {
        return map.containsKey(uuid);
    }

    public static void onPlayerJoin(PlayerEntity e) {
        map.put(e.getUUID(), e);
    }

    public static void onPlayerLeave(PlayerEntity e) {
        map.remove(e.getUUID());
    }
}
