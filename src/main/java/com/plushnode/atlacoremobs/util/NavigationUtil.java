package com.plushnode.atlacoremobs.util;

import org.bukkit.entity.Entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class NavigationUtil {
    private static Class<?> navigationClazz, worldClazz, craftWorldClazz;
    private static Method getWorldHandleMethod;
    private static Field navigationField;
    private static Constructor<?> navigationConstructor;

    static {
        try {
            navigationClazz = ReflectionUtil.getInternalClass("net.minecraft.world.entity.ai.navigation.Navigation");

            worldClazz = ReflectionUtil.getInternalClass("net.minecraft.world.level.World");
            craftWorldClazz = ReflectionUtil.getBukkitClass("org.bukkit.craftbukkit.%s.CraftWorld");

            navigationField = ReflectionUtil.EntityInsentient.getDeclaredField("bQ");
            navigationField.setAccessible(true);

            getWorldHandleMethod = craftWorldClazz.getDeclaredMethod("getHandle");
            navigationConstructor = navigationClazz.getConstructor(ReflectionUtil.EntityInsentient, worldClazz);
        } catch (NoSuchFieldException|NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private NavigationUtil() {

    }

    public static void setDefaultNavigation(Entity entity) {
        try {
            Object entityHandle = ReflectionUtil.getEntityHandle.invoke(entity);
            Object worldHandle = getWorldHandleMethod.invoke(entity.getWorld());
            Object newNavigation = navigationConstructor.newInstance(entityHandle, worldHandle);

            navigationField.set(entityHandle, newNavigation);
        } catch (IllegalAccessException|InvocationTargetException|InstantiationException e) {
            e.printStackTrace();
        }
    }
}
