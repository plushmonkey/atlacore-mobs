package com.plushnode.atlacoremobs.wrappers.pathfinder;

import com.plushnode.atlacoremobs.util.ReflectionUtil;
import org.bukkit.entity.Entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class PathfinderGoalMeleeAttack implements PathfinderGoal {
    private static Class<?> InternalClass;
    private static Constructor<?> constructor;
    private Object handle = null;

    static {
        InternalClass = ReflectionUtil.getInternalClass("net.minecraft.server.%s.PathfinderGoalMeleeAttack");

        try {
            constructor = InternalClass.getConstructor(ReflectionUtil.EntityCreature, double.class, boolean.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public PathfinderGoalMeleeAttack(Entity entity, double d, boolean b) {
        if (!ReflectionUtil.isCreature(entity)) return;

        try {
            Object entityHandle = ReflectionUtil.getEntityHandle.invoke(entity);
            this.handle = constructor.newInstance(entityHandle, d, b);
        } catch (IllegalAccessException|InvocationTargetException |InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object getHandle() {
        return this.handle;
    }
}
