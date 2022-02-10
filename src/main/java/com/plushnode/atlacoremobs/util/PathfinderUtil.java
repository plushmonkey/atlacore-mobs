package com.plushnode.atlacoremobs.util;

import com.plushnode.atlacoremobs.wrappers.pathfinder.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

// Utility class for controlling NMS pathfinder.
public final class PathfinderUtil {
    private PathfinderUtil() {

    }

    public static void disableAI(Entity entity) {
        if (!ReflectionUtil.isInsentient(entity)) return;

        PathfinderGoalSelector goalSelector = new PathfinderGoalSelector(entity, PathfinderGoalSelector.SelectorType.Goal);
        PathfinderGoalSelector targetSelector = new PathfinderGoalSelector(entity, PathfinderGoalSelector.SelectorType.Target);

        goalSelector.clearGoals();
        targetSelector.clearGoals();
    }

    public static void setDefaultAI(Entity entity, boolean meleeAttacks) {
        if (!ReflectionUtil.isInsentient(entity)) return;

        PathfinderGoalSelector goalSelector = new PathfinderGoalSelector(entity, PathfinderGoalSelector.SelectorType.Goal);

        goalSelector.clearGoals();

        // Must use melee attack goal since it's the only predefined goal that moves directly to the target.
        goalSelector.addGoal(2, new PathfinderGoalMeleeAttack(entity, 1.2, false));

        // Lowest priority is to randomly look and move around
        //goalSelector.addGoal(1, new PathfinderGoalRandomLookaround(entity));
        //goalSelector.addGoal(2, new PathfinderGoalRandomStroll(entity, 1.0));
        //goalSelector.addGoal(7, new PathfinderGoalRandomStrollLand(entity, 1.0f));
        //goalSelector.addGoal(8, new PathfinderGoalLookAtPlayer(entity, ReflectionUtil.EntityHuman, 8.0f));
        //goalSelector.addGoal(10, new PathfinderGoalFloat(entity));

        PathfinderGoalSelector targetSelector = new PathfinderGoalSelector(entity, PathfinderGoalSelector.SelectorType.Target);

        // Clear target selector since the bending ai will select the target manually.
        targetSelector.clearGoals();

        // Non-monsters don't have ATTACK_DAMAGE initialized, which is required for the PathfinderGoalMeleeAttack.
        if (((LivingEntity)entity).getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) == null) {
            AttributeUtil.initializeAttackDamage(entity);
        }

        AttributeInstance attribute = ((LivingEntity)entity).getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);

        if (attribute != null) {
            attribute.setBaseValue(meleeAttacks ? 0.5f : 0.0f);
        }

        // Sets an entity's navigation to the default one.
        // The default navigation is required for one of the goals. It will crash if a turtle is spawned without it.
        NavigationUtil.setDefaultNavigation(entity);
    }
}
