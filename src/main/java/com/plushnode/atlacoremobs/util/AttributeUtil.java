package com.plushnode.atlacoremobs.util;

import org.bukkit.entity.Entity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class AttributeUtil {
    private static Class EntityLiving, IAttribute, AttributeMapBase, GenericAttributes;
    private static Method getAttributeMap, initializeAttribute;
    private static Object ATTACK_DAMAGE;

    static {
        try {
            EntityLiving = ReflectionUtil.getInternalClass("net.minecraft.server.%s.EntityLiving");
            IAttribute = ReflectionUtil.getInternalClass("net.minecraft.server.%s.IAttribute");
            AttributeMapBase = ReflectionUtil.getInternalClass("net.minecraft.server.%s.AttributeMapBase");
            GenericAttributes = ReflectionUtil.getInternalClass("net.minecraft.server.%s.GenericAttributes");
            getAttributeMap = EntityLiving.getDeclaredMethod("getAttributeMap");
            initializeAttribute = AttributeMapBase.getDeclaredMethod("b", IAttribute);
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

            initializeAttribute.invoke(attributeMap, ATTACK_DAMAGE);
        } catch (IllegalAccessException|InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
