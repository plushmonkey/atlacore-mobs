package com.plushnode.atlacoremobs.wrappers.pathfinder;

import com.plushnode.atlacoremobs.util.ReflectionUtil;
import org.bukkit.entity.Entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class PathfinderGoalLookAtPlayer implements PathfinderGoal {
    private static Class<?> InternalClass;
    private static Constructor<?> constructor;
    private Object handle = null;

    static {
        InternalClass = ReflectionUtil.getInternalClass("net.minecraft.world.entity.ai.goal.PathfinderGoalLookAtPlayer");

        try {
            constructor = InternalClass.getConstructor(ReflectionUtil.EntityInsentient, Class.class, float.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public PathfinderGoalLookAtPlayer(Entity entity, Class<?> clazz, float lookDistance) {
        if (!ReflectionUtil.isInsentient(entity)) return;

        try {
            Object entityHandle = ReflectionUtil.getEntityHandle.invoke(entity);

            this.handle = constructor.newInstance(entityHandle, clazz, lookDistance);
        } catch (IllegalAccessException|InvocationTargetException|InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object getHandle() {
        return this.handle;
    }
}
