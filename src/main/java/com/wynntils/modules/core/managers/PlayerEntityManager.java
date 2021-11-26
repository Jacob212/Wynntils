/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.managers;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;

import javax.annotation.Nullable;
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

    static void onPlayerJoin(PlayerEntity e) {
        map.put(e.getUUID(), e);
    }

    static void onPlayerLeave(PlayerEntity e) {
        map.remove(e.getUUID());
    }

    //TODO need to find new location of world event listener
//    public static void onWorldLoad(World w) {
//        w.addEventListener(new Listener());
//    }

    public static void onWorldUnload() {
        map.clear();
    }

//    private static class Listener implements IWorldEventListener {
//
//        @Override public void notifyBlockUpdate(World worldIn, BlockPos pos, BlockState oldState, BlockState newState, int flags) { }
//        @Override public void notifyLightSet(BlockPos pos) { }
//        @Override public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) { }
//        @Override public void playSoundToAllNearExcept(@Nullable PlayerEntity player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch) { }
//        @Override public void playRecord(SoundEvent soundIn, BlockPos pos) { }
//        @Override public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) { }
//        @Override public void spawnParticle(int id, boolean ignoreRange, boolean minimiseParticleLevel, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int... parameters) { }
//        @Override public void broadcastSound(int soundID, BlockPos pos, int data) { }
//        @Override public void playEvent(PlayerEntity player, int type, BlockPos blockPosIn, int data) { }
//        @Override public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) { }
//
//        @Override
//        public void onEntityAdded(Entity entityIn) {
//            // FIXME: instead use net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.EntityJoinWorldEvent
//            if (entityIn instanceof PlayerEntity) {
//                onPlayerJoin((PlayerEntity) entityIn);
//            }
//        }
//
//        @Override
//        public void onEntityRemoved(Entity entityIn) {
//            if (entityIn instanceof PlayerEntity) {
//                onPlayerLeave((PlayerEntity) entityIn);
//            }
//        }
//
//    }
}
