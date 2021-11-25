/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.modules.visual.events;

import com.wynntils.McIf;
import com.wynntils.Reference;
import com.wynntils.core.events.custom.GameEvent;
import com.wynntils.core.events.custom.PacketEvent;
import com.wynntils.core.events.custom.WynnWorldEvent;
import com.wynntils.core.framework.entities.EntityManager;
import com.wynntils.core.framework.interfaces.Listener;
import com.wynntils.core.utils.objects.Location;
import com.wynntils.modules.visual.configs.VisualConfig;
import com.wynntils.modules.visual.entities.EntityDamageSplash;
import com.wynntils.modules.visual.managers.CachedChunkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SChunkDataPacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEvents implements Listener {

    @SubscribeEvent
    public void damageIndicators(GameEvent.DamageEntity e) {
        if (!VisualConfig.DamageSplash.INSTANCE.enabled) return;
        EntityManager.spawnEntity(new EntityDamageSplash(e.getDamageTypes(),
                new Location(e.getEntity())));

        e.getEntity().kill();
    }

    @SubscribeEvent
    public void cacheChunks(PacketEvent<SChunkDataPacket> event) {
        if (!Reference.onWorld || !VisualConfig.CachedChunks.INSTANCE.enabled) return;

        SChunkDataPacket packet = event.getPacket();

        // Requests the chunk to be unloaded if loaded before loading (???)
        // this fixes some weird ass issue with optifine, don't ask too much
        if (packet.isFullChunk() && McIf.world().getChunk(packet.getX(), packet.getZ()).isLoaded()) {
        	McIf.mc().submit(() -> McIf.world().unload(McIf.world().getChunk(packet.getX(), packet.getZ())));
        }

        CachedChunkManager.asyncCacheChunk(packet);
    }

    @SubscribeEvent
    public void joinWorld(WynnWorldEvent.Join e) {
        if (!VisualConfig.CachedChunks.INSTANCE.enabled) return;
        CachedChunkManager.startAsyncChunkLoader();
    }

}
