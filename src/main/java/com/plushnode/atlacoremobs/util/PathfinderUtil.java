package com.plushnode.atlacoremobs.util;

import com.plushnode.atlacoremobs.wrappers.pathfinder.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

// Utility class for controlling NMS pathfinder.
public final class PathfinderUtil {
    private PathfinderUtil() {

    }

    public static void disableAI(Entity entity) {
        if (!ReflectionUtil.isInsentient(entity)) return;

        PathfinderGoalSelector goalSelector = new PathfinderGoalSelector(entity, PathfinderGoalSelector.SelectorType.Goal);
        PathfinderGoalSelector targetSelector = new PathfinderGoalSelector(entity, PathfinderGoalSelector.SelectorType.Target);

        goalSelector.clearSets();
        targetSelector.clearSets();
    }

    public static void setDefaultAI(Entity entity, boolean meleeAttacks) {
        if (!ReflectionUtil.isInsentient(entity)) return;

        PathfinderGoalSelector goalSelector = new PathfinderGoalSelector(entity, PathfinderGoalSelector.SelectorType.Goal);

        goalSelector.clearSets();

        goalSelector.setGoal(0, new PathfinderGoalFloat(entity));
        if (meleeAttacks) {
            //goalSelector.setGoal(2, new PathfinderGoalMeleeAttack(entity, 1.0, false));
        }
        goalSelector.setGoal(5, new PathfinderGoalMoveTowardsRestriction(entity, 1.0));
        goalSelector.setGoal(7, new PathfinderGoalRandomStroll(entity, 1.0));
        goalSelector.setGoal(8, new PathfinderGoalLookAtPlayer(entity, ReflectionUtil.EntityHuman, 1.0f));
        goalSelector.setGoal(8, new PathfinderGoalRandomLookaround(entity));

        PathfinderGoalSelector targetSelector = new PathfinderGoalSelector(entity, PathfinderGoalSelector.SelectorType.Target);

        targetSelector.clearSets();
        targetSelector.setGoal(2, new PathfinderGoalNearestAttackableTarget(entity, ReflectionUtil.EntityHuman, 10, true, false, null));

        // Non-monsters don't have ATTACK_DAMAGE initialized, which is required for the PathfinderGoalMeleeAttack.
        if (((LivingEntity)entity).getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) == null) {
            //AttributeUtil.initializeAttackDamage(entity);
        }

        // Sets an entity's navigation to the default one.
        // The default navigation is required for one of the goals. It will crash if a turtle is spawned without it.
        NavigationUtil.setDefaultNavigation(entity);
    }
}
