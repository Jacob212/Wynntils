/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.core.utils.reflections;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.gui.overlay.PlayerTabOverlayGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.HorseInventoryScreen;
//import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.network.play.client.CClientSettingsPacket;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import com.wynntils.ModCore;

public enum ReflectionFields {
    // FIXME: All of these filds are likely incorrect...
	// FIXME: find srg names of values correctly set to null
	//this works while set to null but will probs run faster with srg names
	ContainerScreen_Menu(ContainerScreen.class, "menu", null),
    Entity_CUSTOM_NAME(Entity.class, "DATA_CUSTOM_NAME", null),
    Entity_CUSTOM_NAME_VISIBLE(Entity.class, "DATA_CUSTOM_NAME_VISIBLE", "field_184233_aA"),
    ItemFrameEntity_ITEM(ItemFrameEntity.class, "DATA_ITEM", "field_184525_c"),
    Event_phase(Event.class, "phase", null),
    GuiScreen_buttonList(Screen.class, "buttons", "field_230710_m_"),
    HorseInventoryScreen_horseEntity(HorseInventoryScreen.class, "horseEntity", "field_147034_x"),
    HorseInventoryScreen_horseInventory(AbstractHorseEntity.class, "inventory", null),
    IngameGui_persistantChatGUI(IngameGui.class, "chat", "field_73840_e"),
    IngameGui_remainingHighlightTicks(IngameGui.class, "remainingHighlightTicks", "field_92017_k"),
    IngameGui_highlightingItemStack(IngameGui.class, "highlightingItemStack", "field_92016_l"),
    IngameGui_displayedSubTitle(IngameGui.class, "displayedSubTitle", "field_175200_y"),
//     FIXME: protected final PlayerTabOverlayGui tabList;
    IngameGui_overlayPlayerList(IngameGui.class, "overlayPlayerList", "field_175196_v"),
    GuiChat_defaultInputFieldText(ChatScreen.class, "initial", "field_146409_v"),
    PlayerTabOverlayGui_ENTRY_ORDERING(PlayerTabOverlayGui.class, "PLAYER_ORDERING", null),
    Minecraft_resourcePackRepository(Minecraft.class, "resourcePackRepository", "field_110448_aq"),
    CClientSettingsPacket_chatVisibility(CClientSettingsPacket.class, "chatVisibility", "field_149529_c"),
    // FIXME: missing!
    //ModelRenderer_compiled(ModelRenderer.class, "compiled", "field_78812_q"),
    Minecraft_renderItem(Minecraft.class, "renderItem", "field_175621_X"),
    RenderItem_itemModelMesher(ItemRenderer.class, "itemModelMesher", "field_175059_m");

    static {
        PlayerTabOverlayGui_ENTRY_ORDERING.removeFinal();
    }

    final Field field;

    ReflectionFields(Class<?> holdingClass, String unobfName, String srgName) {
    	//FIXME hacky fix too not knowing all srg names
    	if (srgName != null) {//if srg name is given, this fast look up is used
    		this.field = ObfuscationReflectionHelper.findField(holdingClass, srgName);
    		
    	} else {//if no srg name is given, then longer lookup is user but they pretty much do the same thing.
    		Field temp = null;
    		try {
    			temp = holdingClass.getDeclaredField(unobfName);
    		} catch (NoSuchFieldException e) {
    			e.printStackTrace();
    		}
    		this.field = temp;
    		this.field.setAccessible(true);
    	}
    }

    public <T> T getValue(Object parent) {
        try {
            return (T) field.get(parent);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setValue(Object parent, Object value) {
        try {
            field.set(parent, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static Field modifiersField = null;

    private void removeFinal() {
        if (modifiersField == null) {
            try {
                modifiersField = Field.class.getDeclaredField("modifiers");
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                return;
            }
            modifiersField.setAccessible(true);
        }

        try {
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
