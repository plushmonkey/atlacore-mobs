package com.plushnode.atlacoremobs.util;

import org.bukkit.entity.Entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Consumer;

public final class AttributeUtil {
    private static Class EntityLiving, AttributeModifiable, AttributeBase, AttributeMapBase, GenericAttributes, AttributeProvider;
    private static Method getAttributeMap, createModifiable;
    private static Object ATTACK_DAMAGE;

    static {
        try {
            EntityLiving = ReflectionUtil.getInternalClass("net.minecraft.world.entity.EntityLiving");
            AttributeMapBase = ReflectionUtil.getInternalClass("net.minecraft.world.entity.ai.attributes.AttributeMapBase");
            AttributeBase = ReflectionUtil.getInternalClass("net.minecraft.world.entity.ai.attributes.AttributeBase");
            AttributeModifiable = ReflectionUtil.getInternalClass("net.minecraft.world.entity.ai.attributes.AttributeModifiable");
            AttributeProvider = ReflectionUtil.getInternalClass("net.minecraft.world.entity.ai.attributes.AttributeProvider");
            GenericAttributes = ReflectionUtil.getInternalClass("net.minecraft.world.entity.ai.attributes.GenericAttributes");

            getAttributeMap = EntityLiving.getDeclaredMethod("ep");
            createModifiable = AttributeMapBase.getDeclaredMethod("a", AttributeBase);
            Field attackDamageField = GenericAttributes.getDeclaredField("f");

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

            Field attributeField = AttributeMapBase.getDeclaredField("b");
            attributeField.setAccessible(true);

            Map<Object, Object> attributes = (Map)attributeField.get(attributeMap);

            Constructor ModifiableConstructor = AttributeModifiable.getConstructor(AttributeBase, Consumer.class);
            Consumer consumer = (obj) -> {};

            // Construct a new AttributeModifiable to store in the attributes map.
            Object modifiable = ModifiableConstructor.newInstance(ATTACK_DAMAGE, consumer);

            attributes.put(ATTACK_DAMAGE, modifiable);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchFieldException | NoSuchMethodException | InstantiationException e) {
            e.printStackTrace();
        }
    }
}
