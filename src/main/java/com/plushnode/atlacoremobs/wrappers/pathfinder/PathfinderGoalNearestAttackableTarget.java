package com.plushnode.atlacoremobs.wrappers.pathfinder;

import com.plushnode.atlacoremobs.util.ReflectionUtil;
import org.bukkit.entity.Entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Predicate;

public class PathfinderGoalNearestAttackableTarget implements PathfinderGoal {
    private static Class<?> InternalClass;
    private static Constructor<?> constructor;
    private Object handle = null;

    static {
        InternalClass = ReflectionUtil.getInternalClass("net.minecraft.world.entity.ai.goal.target.PathfinderGoalNearestAttackableTarget");

        try {
            Class<?> entityClazz = ReflectionUtil.EntityCreature;

            if (ReflectionUtil.getServerVersion() >= 14) {
                entityClazz = ReflectionUtil.EntityInsentient;
            }
            constructor = InternalClass.getConstructor(entityClazz, Class.class, int.class, boolean.class, boolean.class, Predicate.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public PathfinderGoalNearestAttackableTarget(Entity entity, Class<?> targetType, int intervalTicks, boolean mustSee, boolean mustReach, Predicate<?> predicate) {
        if (!ReflectionUtil.isCreature(entity)) return;

        try {
            Object entityHandle = ReflectionUtil.getEntityHandle.invoke(entity);
            this.handle = constructor.newInstance(entityHandle, targetType, intervalTicks, mustSee, mustReach, predicate);
        } catch (IllegalAccessException|InvocationTargetException|InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object getHandle() {
        return this.handle;
    }
}
