package com.plushnode.atlacoremobs.wrappers.pathfinder;

import com.google.common.collect.Sets;
import com.plushnode.atlacoremobs.util.ReflectionUtil;
import org.bukkit.entity.Entity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Ref;
import java.util.Set;

public class PathfinderGoalSelector {
    private static final Class<?> InternalClass;
    private static Method addGoalMethod;
    private static Field availableGoals, goalSelectorField, targetSelectorField;
    private Object internalObject;

    static {
        InternalClass = ReflectionUtil.getInternalClass("net.minecraft.world.entity.ai.goal.PathfinderGoalSelector");
        Class<?> InternalGoal = ReflectionUtil.getInternalClass("net.minecraft.world.entity.ai.goal.PathfinderGoal");

        if (InternalClass != null) {
            try {
                availableGoals = InternalClass.getDeclaredField("d");

                availableGoals.setAccessible(true);

                goalSelectorField = ReflectionUtil.EntityInsentient.getDeclaredField("bR");
                targetSelectorField = ReflectionUtil.EntityInsentient.getDeclaredField("bS");

                addGoalMethod = InternalClass.getDeclaredMethod("a", int.class, InternalGoal);
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

    public void addGoal(int priority, PathfinderGoal goal) {
        try {
            Object handle = goal.getHandle();
            if (handle != null) {
                addGoalMethod.invoke(internalObject, priority, handle);
            }
        } catch (IllegalAccessException|InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public Set getGoals() {
        try {
            return (Set)availableGoals.get(internalObject);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void clearGoals() {
        try {
            availableGoals.set(internalObject, Sets.newLinkedHashSet());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public enum SelectorType {
        Goal,
        Target
    }
}
