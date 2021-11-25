/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.modules.music.events;

import com.wynntils.McIf;
import com.wynntils.Reference;
import com.wynntils.core.events.custom.*;
import com.wynntils.core.framework.enums.ClassType;
import com.wynntils.core.framework.interfaces.Listener;
import com.wynntils.core.utils.helpers.Delay;
import com.wynntils.core.utils.objects.Location;
import com.wynntils.modules.core.enums.ToggleSetting;
import com.wynntils.modules.core.overlays.inventories.ChestReplacer;
import com.wynntils.modules.music.configs.MusicConfig;
import com.wynntils.modules.music.managers.AreaTrackManager;
import com.wynntils.modules.music.managers.BossTrackManager;
import com.wynntils.modules.music.managers.SoundTrackManager;
import com.wynntils.modules.utilities.overlays.hud.WarTimerOverlay;
import com.wynntils.webapi.WebManager;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.network.play.server.SWindowItemsPacket;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;


public class ClientEvents implements Listener {

    // player ticking
    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent e) {
        if (!MusicConfig.INSTANCE.enabled || e.phase == TickEvent.Phase.START) return;

        SoundTrackManager.getPlayer().update();
    }

    @SubscribeEvent
    public void serverLeft(WynncraftServerEvent.Leave e) {
        SoundTrackManager.getPlayer().stop();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void worldLeft(WynnWorldEvent.Leave e) {
        SoundTrackManager.getPlayer().stop();
    }

    // character selection
    @SubscribeEvent
    public void characterChange(WynnClassChangeEvent e) {
        if (e.getNewClass() == ClassType.NONE && Reference.onWorld) return; // character selection

        // Toggle wynncraft music off if wynntils music replacer is enabled
        if (MusicConfig.INSTANCE.replaceJukebox && MusicConfig.INSTANCE.enabled && Reference.onWorld) {
            new Delay(() -> ToggleSetting.MUSIC.set(false), 20);
        }

        SoundTrackManager.getPlayer().stop();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void openCharacterSelection(GuiOverlapEvent.ChestOverlap.InitGui e) {
        if (!MusicConfig.INSTANCE.classSelectionMusic || !McIf.toText(e.getGui().getTitle()).contains("Select a Class")) return;

        SoundTrackManager.findTrack(WebManager.getMusicLocations().getEntryTrack("characterSelector"), true, MusicConfig.INSTANCE.characterSelectorQuiet);
    }

    // special tracks
    @SubscribeEvent
    public void dungeonTracks(PacketEvent<STitlePacket> e) {
        if (!MusicConfig.INSTANCE.replaceJukebox || e.getPacket().getType() != STitlePacket.Type.TITLE) return;

        String title = McIf.getTextWithoutFormattingCodes(e.getPacket().getText().toString());
        String songName = WebManager.getMusicLocations().getDungeonTrack(title);
        if (songName == null) return;

        SoundTrackManager.findTrack(songName, true);
    }

    @SubscribeEvent
    public void warTrack(WarStageEvent e) {
        if (!MusicConfig.INSTANCE.replaceJukebox || e.getNewStage() != WarTimerOverlay.WarStage.WAITING_FOR_MOB_TIMER) return;

        SoundTrackManager.findTrack(WebManager.getMusicLocations().getEntryTrack("wars"), true);
    }

    // area tracks
    @SubscribeEvent
    public void areaTracks(SchedulerEvent.RegionUpdate e) {
        if (!MusicConfig.INSTANCE.replaceJukebox) return;

        McIf.mc().submit(BossTrackManager::update);

        if (BossTrackManager.isAlive()) return;
        AreaTrackManager.update(new Location(McIf.player()));
    }

    // mythic found sfx
    @SubscribeEvent
    public void onMythicFound(PacketEvent<SWindowItemsPacket> e) {
        if (!MusicConfig.SoundEffects.INSTANCE.mythicFound) return;
        if (McIf.mc().screen == null) return;
        if (!(McIf.mc().screen instanceof ChestReplacer)) return;

        ChestReplacer chest = (ChestReplacer) McIf.mc().screen;
        if (!McIf.toText(chest.getTitle()).contains("Loot Chest") &&
                !McIf.toText(chest.getTitle()).contains("Daily Rewards") &&
                !McIf.toText(chest.getTitle()).contains("Objective Rewards")) return;

        int size = Math.min(chest.getLowerInv().getContainerSize(), e.getPacket().getItems().size());
        for (int i = 0; i < size; i++) {
            ItemStack stack = e.getPacket().getItems().get(i);
            if (stack.isEmpty() || !stack.hasCustomHoverName()) continue;
            if (!McIf.toText(stack.getDisplayName()).contains(TextFormatting.DARK_PURPLE.toString())) continue;
            if (!McIf.toText(stack.getDisplayName()).contains("Unidentified")) continue;

            SoundTrackManager.findTrack(WebManager.getMusicLocations().getEntryTrack("mythicFound"),
                    true, false, false, false, true, false);
            break;
        }
    }

}
