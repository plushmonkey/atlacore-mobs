package com.plushnode.atlacoremobs.wrappers.pathfinder;

import com.plushnode.atlacoremobs.util.ReflectionUtil;
import org.bukkit.entity.Entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class PathfinderGoalRandomStrollLand implements PathfinderGoal {
    private static Class<?> InternalClass;
    private static Constructor<?> constructor;
    private Object handle = null;

    static {
        InternalClass = ReflectionUtil.getInternalClass("net.minecraft.world.entity.ai.goal.PathfinderGoalRandomStrollLand");

        try {
            constructor = InternalClass.getConstructor(ReflectionUtil.EntityCreature, double.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public PathfinderGoalRandomStrollLand(Entity entity, double speedModifier) {
        if (!ReflectionUtil.isCreature(entity)) return;

        try {
            Object entityHandle = ReflectionUtil.getEntityHandle.invoke(entity);
            this.handle = constructor.newInstance(entityHandle, speedModifier);
        } catch (IllegalAccessException|InvocationTargetException|InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object getHandle() {
        return this.handle;
    }
}
