package com.plushnode.atlacoremobs.util;

import org.bukkit.entity.Entity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ReflectionUtil {
    public static final Class<?> EntityCreature, EntityInsentient, CraftEntity, EntityHuman;
    public static Method getEntityHandle;

    private ReflectionUtil() {

    }

    static {
        CraftEntity = getInternalClass("org.bukkit.craftbukkit.%s.entity.CraftEntity");
        EntityCreature = getInternalClass("net.minecraft.server.%s.EntityCreature");
        EntityInsentient = getInternalClass("net.minecraft.server.%s.EntityInsentient");
        EntityHuman = getInternalClass("net.minecraft.server.%s.EntityHuman");

        if (CraftEntity != null) {
            try {
                getEntityHandle = CraftEntity.getDeclaredMethod("getHandle");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    public static Class<?> getInternalClass(String nmsClass) {
        String version = null;

        Pattern pattern = Pattern.compile("net\\.minecraft\\.(?:server)?\\.(v(?:\\d+_)+R\\d)");
        for (Package p : Package.getPackages()) {
            String name = p.getName();
            Matcher m = pattern.matcher(name);
            if (m.matches()) {
                version = m.group(1);
            }
        }

        if (version == null) return null;

        try {
            return Class.forName(String.format(nmsClass, version));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static boolean isInsentient(Entity entity) {
        return isEntityClass(entity, EntityInsentient);
    }

    public static boolean isCreature(Entity entity) {
        return isEntityClass(entity, EntityCreature);
    }

    public static boolean isEntityClass(Entity entity, Class<?> clazz) {
        try {
            Object entityHandle = ReflectionUtil.getEntityHandle.invoke(entity);

            if (!clazz.isAssignableFrom(entityHandle.getClass())) {
                return false;
            }
        } catch (IllegalAccessException|InvocationTargetException e) {
            e.printStackTrace();
        }

        return true;
    }
}
