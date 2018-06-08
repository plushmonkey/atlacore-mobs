package com.plushnode.atlacoremobs.wrappers.pathfinder;

import com.google.common.collect.Sets;
import com.plushnode.atlacoremobs.util.ReflectionUtil;
import org.bukkit.entity.Entity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PathfinderGoalSelector {
    private static final Class<?> InternalClass;
    private static Method setGoalMethod;
    private static Field bField, cField, goalSelectorField, targetSelectorField;
    private Object internalObject;

    static {
        InternalClass = ReflectionUtil.getInternalClass("net.minecraft.server.%s.PathfinderGoalSelector");
        Class<?> InternalGoal = ReflectionUtil.getInternalClass("net.minecraft.server.%s.PathfinderGoal");

        if (InternalClass != null) {
            try {
                bField = InternalClass.getDeclaredField("b");
                bField.setAccessible(true);

                cField = InternalClass.getDeclaredField("b");
                cField.setAccessible(true);

                goalSelectorField = ReflectionUtil.EntityInsentient.getDeclaredField("goalSelector");
                targetSelectorField = ReflectionUtil.EntityInsentient.getDeclaredField("targetSelector");

                setGoalMethod = InternalClass.getDeclaredMethod("a", int.class, InternalGoal);
            } catch (NoSuchFieldException|NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    public PathfinderGoalSelector(Entity entity, SelectorType type) {
        try {
            Object entityHandle = ReflectionUtil.getEntityHandle.invoke(entity);

            if (type == SelectorType.Goal) {
                internalObject = goalSelectorField.get(entityHandle);
            } else if (type == SelectorType.Target) {
                internalObject = targetSelectorField.get(entityHandle);
            }
        } catch (IllegalAccessException|InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void setGoal(int i, PathfinderGoal goal) {
        try {
            Object handle = goal.getHandle();
            if (handle != null) {
                setGoalMethod.invoke(internalObject, i, handle);
            }
        } catch (IllegalAccessException|InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    // Clears the b and c sets.
    public void clearSets() {
        try {
            bField.set(internalObject, Sets.newLinkedHashSet());
            cField.set(internalObject, Sets.newLinkedHashSet());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public enum SelectorType {
        Goal,
        Target
    }
}
