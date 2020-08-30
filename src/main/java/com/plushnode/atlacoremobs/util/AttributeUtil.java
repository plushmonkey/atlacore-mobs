package com.plushnode.atlacoremobs.util;

import com.google.common.collect.Maps;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AttributeUtil {
    private static Class EntityLiving, AttributeModifiable, AttributeBase, AttributeMapBase, GenericAttributes, AttributeProvider;
    private static Method getAttributeMap, createModifiable;
    private static Object ATTACK_DAMAGE;

    static {
        try {
            EntityLiving = ReflectionUtil.getInternalClass("net.minecraft.server.%s.EntityLiving");
            AttributeMapBase = ReflectionUtil.getInternalClass("net.minecraft.server.%s.AttributeMapBase");
            AttributeBase = ReflectionUtil.getInternalClass("net.minecraft.server.%s.AttributeBase");
            AttributeModifiable = ReflectionUtil.getInternalClass("net.minecraft.server.%s.AttributeModifiable");
            AttributeProvider = ReflectionUtil.getInternalClass("net.minecraft.server.%s.AttributeProvider");
            GenericAttributes = ReflectionUtil.getInternalClass("net.minecraft.server.%s.GenericAttributes");

            getAttributeMap = EntityLiving.getDeclaredMethod("getAttributeMap");
            createModifiable = AttributeMapBase.getDeclaredMethod("a", AttributeBase);
            Field attackDamageField = GenericAttributes.getDeclaredField("ATTACK_DAMAGE");

            ATTACK_DAMAGE = attackDamageField.get(null);
        } catch (IllegalAccessException|NoSuchMethodException|NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private AttributeUtil() {

    }

    public static void initializeAttackDamage(Entity entity) {
        try {
            Object entityHandle = ReflectionUtil.getEntityHandle.invoke(entity);
            Object attributeMap = getAttributeMap.invoke(entityHandle);

            /*
            Field providerField = AttributeMapBase.getDeclaredField("d");
            providerField.setAccessible(true);

            Field mapField = AttributeProvider.getDeclaredField("a");
            mapField.setAccessible(true);

            Object provider = providerField.get(attributeMap);
            Object immutableMap = mapField.get(provider);

            HashMap newMap = new HashMap((Map)immutableMap);

            mapField.set(provider, newMap);

            Object r = createModifiable.invoke(attributeMap, ATTACK_DAMAGE);

            if (r == null) {
                System.out.println("Failed to add attack damage modifier.");
            } else {
                System.out.println("Got attack damage modifier back");
            }
             */
        } catch (IllegalAccessException|InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
