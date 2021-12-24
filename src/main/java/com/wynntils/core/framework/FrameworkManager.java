/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.core.framework;

import com.ibm.icu.impl.Pair;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.wynntils.McIf;
import com.wynntils.ModCore;
import com.wynntils.Reference;
import com.wynntils.core.events.custom.WynncraftServerEvent;
import com.wynntils.core.framework.enums.Priority;
import com.wynntils.core.framework.instances.KeyHolder;
import com.wynntils.core.framework.instances.Module;
import com.wynntils.core.framework.instances.containers.ModuleContainer;
import com.wynntils.core.framework.interfaces.ICommand;
import com.wynntils.core.framework.interfaces.Listener;
import com.wynntils.core.framework.interfaces.annotations.ModuleInfo;
import com.wynntils.core.framework.overlays.Overlay;
import com.wynntils.core.framework.rendering.ScreenRenderer;
import com.wynntils.core.framework.settings.SettingsContainer;
import com.wynntils.core.framework.settings.annotations.SettingsInfo;
import com.wynntils.core.framework.settings.instances.SettingsHolder;
import com.wynntils.core.framework.entities.EntityManager;
import com.wynntils.core.framework.entities.interfaces.EntitySpawnCodition;
import com.wynntils.core.utils.Utils;
import com.wynntils.core.utils.objects.Location;
import com.wynntils.core.utils.reflections.ReflectionFields;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.command.CommandSource;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.event.TickEvent;
import org.apache.logging.log4j.LogManager;

import java.util.*;
import java.util.function.Consumer;

import static net.minecraft.client.gui.screen.Screen.GUI_ICONS_LOCATION;

public class FrameworkManager {

    protected final static Map<String, ModuleContainer> availableModules = new HashMap<>();
    protected final static Map<Priority, List<Overlay>> registeredOverlays = new LinkedHashMap<>();
    protected final static Set<EntitySpawnCodition> registeredSpawnConditions = new HashSet<>();
    protected final static Set<ICommand> registeredCommands = new HashSet<>();

    public static final IEventBus eventBus = BusBuilder.builder().startShutdown().build();
    
    public static final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher();

    static {
        registeredOverlays.put(Priority.LOWEST, new ArrayList<>());
        registeredOverlays.put(Priority.LOW, new ArrayList<>());
        registeredOverlays.put(Priority.NORMAL, new ArrayList<>());
        registeredOverlays.put(Priority.HIGH, new ArrayList<>());
        registeredOverlays.put(Priority.HIGHEST, new ArrayList<>());
    }

    public static void registerModule(Module module) {
        ModuleInfo info = module.getClass().getAnnotation(ModuleInfo.class);
        if (info == null) {
            return;
        }

        module.setLogger(LogManager.getFormatterLogger(Reference.MOD_ID + "-" + info.name().toLowerCase(Locale.ROOT)));

        availableModules.put(info.name(), new ModuleContainer(info, module));
    }

    public static void registerEvents(Module module, Listener listener) {
        ModuleInfo info = module.getClass().getAnnotation(ModuleInfo.class);
        if (info == null) {
            return;
        }

        availableModules.get(info.name()).registerEvents(listener);
    }

    public static void registerSpawnCondition(Module module, EntitySpawnCodition entity) {
        ModuleInfo info = module.getClass().getAnnotation(ModuleInfo.class);
        if (info == null) {
            return;
        }

        registeredSpawnConditions.add(entity);
    }

    public static void registerSettings(Module module, Class<? extends SettingsHolder> settingsClass) {
        ModuleInfo info = module.getClass().getAnnotation(ModuleInfo.class);
        if (info == null)
            return;

        availableModules.get(info.name()).registerSettings(settingsClass);
    }


    public static void registerOverlay(Module module, Overlay overlay, Priority priority) {
        ModuleInfo info = module.getClass().getAnnotation(ModuleInfo.class);
        if (info == null)
            return;

        ModuleContainer container = availableModules.get(info.name());
        overlay.module = container;
        container.registerSettings("overlay" + overlay.displayName, overlay);
        registeredOverlays.get(priority).add(overlay);
    }

    public static KeyHolder registerKeyBinding(Module module, KeyHolder holder) {
        ModuleInfo info = module.getClass().getAnnotation(ModuleInfo.class);
        if (info == null) {
            return null;
        }

        availableModules.get(info.name()).registerKeyBinding(holder);
        return holder;
    }
    
    public static void registerCommand(Module module, ICommand command) {
    	ModuleInfo info = module.getClass().getAnnotation(ModuleInfo.class);
        if (info == null) {
            return;
        }
        
    	registeredCommands.add(command);
    }
    
    public static void reloadSettings() {
        availableModules.values().forEach(ModuleContainer::reloadSettings);
    }

    public static void startModules() {
        availableModules.values().forEach(c -> c.getModule().onEnable());
    }

    public static void postEnableModules() {
        availableModules.values().forEach(c -> c.getModule().postEnable());
    }

    public static void disableModules() {
        availableModules.values().forEach(c -> {
            c.getModule().onDisable(); c.unregisterAllEvents();
        });
    }
    
    public static void setupCommands(InterModProcessEvent e) {
    	e.getIMCStream("register_command"::equals)
        .map(message -> Pair.of(message.getSenderModId(), message.getMessageSupplier().get()))
        .filter(command -> command.second instanceof LiteralArgumentBuilder<?>)
        .map(command -> Pair.of(command.first, (LiteralArgumentBuilder<CommandSource>) command.second))
        .forEachOrdered(command -> {
        	ModCore.LOGGER.debug("Registering client command '{1}' for '{0}'", command.first, command.second.getLiteral());
			dispatcher.register(command.second);
        });;
        
        e.getIMCStream("reigster_command"::equals)
        .map(message -> Pair.of(message.getSenderModId(), message.getMessageSupplier().get()))
        .filter(command -> command.second instanceof Consumer<?>)
        .map(command -> Pair.of(command.first, (Consumer<CommandDispatcher<CommandSource>>) command.second))
        .forEachOrdered(command -> {
        	ModCore.LOGGER.debug("Calling command registry callback for '{0}'", command.first);
        	command.second.accept(dispatcher);
        });;
    }

    public static void triggerCommandsRegister() {
    	for (ICommand command : registeredCommands) {
            command.register(dispatcher);
        }
    }
    
    public static void triggerEvent(Event e) {
        if (
                Reference.onServer
                || e instanceof WynncraftServerEvent
                || e instanceof TickEvent.RenderTickEvent
                || e instanceof GuiScreenEvent
        ) {
            ReflectionFields.Event_phase.setValue(e, null);
            eventBus.post(e);
        }
    }

    public static void triggerPreHud(RenderGameOverlayEvent.Pre e) {
        if (Reference.onServer && !McIf.mc().gameMode.isAlwaysFlying()) {
            if (e.getType() == RenderGameOverlayEvent.ElementType.AIR ||  // move it to somewhere else if you want, it seems pretty core to wynncraft tho..
               e.getType() == RenderGameOverlayEvent.ElementType.ARMOR) {
                e.setCanceled(true);
                return;
            }

            McIf.mc().getProfiler().push("preRenOverlay");
            for (List<Overlay> overlays : registeredOverlays.values()) {
                for (Overlay overlay : overlays) {
                    if (!overlay.active) continue;

                    if (overlay.overrideElements.length != 0) {
                        boolean contained = false;
                        for (RenderGameOverlayEvent.ElementType type : overlay.overrideElements) {
                            if (e.getType() == type) {
                                contained = true;
                                break;
                            }
                        }
                        if (contained)
                            e.setCanceled(true);
                        else
                            continue;
                    }
                    if ((overlay.module == null || overlay.module.getModule().isActive()) && overlay.visible && overlay.active) {
                        McIf.mc().getProfiler().push(overlay.displayName);
                        ScreenRenderer.beginGL(overlay.position.getDrawingX(), overlay.position.getDrawingY());
                        overlay.render(e);
                        ScreenRenderer.endGL();
                        McIf.mc().getProfiler().pop();
                    }
                }
            }
            McIf.mc().getProfiler().pop();

            McIf.mc().getTextureManager().bind(GUI_ICONS_LOCATION);
        }
    }

    public static void triggerPostHud(RenderGameOverlayEvent.Post e) {
        if (Reference.onServer && !McIf.mc().gameMode.isAlwaysFlying()) {
            McIf.mc().getProfiler().push("posRenOverlay");
            for (List<Overlay> overlays : registeredOverlays.values()) {
                for (Overlay overlay : overlays) {
                    if (!overlay.active) continue;

                    if ((overlay.module == null || overlay.module.getModule().isActive()) && overlay.visible && overlay.active) {
                        McIf.mc().getProfiler().push(overlay.displayName);

                        ScreenRenderer.beginGL(overlay.position.getDrawingX(), overlay.position.getDrawingY());
                        overlay.render(e);
                        ScreenRenderer.endGL();

                        McIf.mc().getProfiler().pop();
                    }
                }
            }
            McIf.mc().getProfiler().pop();
        }
    }

    public static void triggerHudTick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.START || !Reference.onServer) return;

        for (List<Overlay> overlays : registeredOverlays.values()) {
            for (Overlay overlay : overlays) {
                if ((overlay.module == null || overlay.module.getModule().isActive()) && overlay.active) {
                    overlay.position.refresh(ScreenRenderer.screen);
                    overlay.tick(e, 0);
                }
            }
        }
    }

    public static void triggerNaturalSpawn() {
        if (registeredSpawnConditions.isEmpty()) return;

        Random r = Utils.getRandom();
        if (r.nextBoolean()) return; // reduce spawn chances by half

        ClientPlayerEntity player = McIf.player();
        for (double x = -10; x < 10; x++) {
            for (double y = -1; y < 6; y++) {
                for (double z = -10; z < 10; z++) {
                    for (EntitySpawnCodition condition : registeredSpawnConditions) {
                        Location relative = new Location(player).add(x, y, z);
                        if (!condition.shouldSpawn(relative, player.level, player, r)) continue;

                        EntityManager.spawnEntity(condition.createEntity(relative, player.level, player, r));
                    }
                }
            }
        }
    }

    public static void triggerKeyPress() {
        if (!Reference.onServer) return;

        availableModules.values().forEach(ModuleContainer::triggerKeyBinding);
    }

    public static SettingsContainer getSettings(Module module, SettingsHolder holder) {
        ModuleInfo info = module.getClass().getAnnotation(ModuleInfo.class);
        if (info == null) {
            return null;
        }

        SettingsInfo info2 = holder.getClass().getAnnotation(SettingsInfo.class);
        if (info2 == null) {
            if (holder instanceof Overlay)
                return availableModules.get(info.name()).getRegisteredSettings().get("overlay" + ((Overlay) holder).displayName);
            else
                return null;
        }

        return availableModules.get(info.name()).getRegisteredSettings().get(info2.name());
    }

    public static Map<String, ModuleContainer> getAvailableModules() {
        return availableModules;
    }

    public static IEventBus getEventBus() {
        return eventBus;
    }

}
