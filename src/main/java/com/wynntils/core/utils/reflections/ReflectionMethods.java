/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.core.utils.reflections;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.wynntils.ModCore;

public enum ReflectionMethods {

    Minecraft$setWindowIcon(Minecraft.class, "setWindowIcon", "func_175594_ao"),
    SPacketPlayerListItem$AddPlayerData_getProfile(ReflectionClasses.SPacketPlayerListItem$AddPlayerData.clazz, "getProfile", "func_179962_a"),
    SPacketPlayerListItem$AddPlayerData_getDisplayName(ReflectionClasses.SPacketPlayerListItem$AddPlayerData.clazz, "getDisplayName", "func_179961_d");

    final Method method;

    ReflectionMethods(Class<?> holdingClass, String unobfName, String srgname, Class<?>... parameterTypes) {
    	//FIXME hacky fix too not knowing all srg names
    	//havent tested this yet, should work tho
    	if (srgname != null) {//if srg name is given, this fast look up is used
    		this.method = ObfuscationReflectionHelper.findMethod(holdingClass, srgname, parameterTypes);
    		
    	} else {//if no srg name is given, then longer lookup is user but they pretty much do the same thing.
    		Method temp = null;
    		try {
    			temp = holdingClass.getDeclaredMethod(unobfName);
    		} catch (NoSuchMethodException e) {
    			e.printStackTrace();
    		}
    		this.method = temp;
    		this.method.setAccessible(true);
    	}
    }

    public Object invoke(Object obj, Object... parameters) {
        try {
            return method.invoke(obj, parameters);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

}
