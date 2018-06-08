package com.plushnode.atlacoremobs.util;

import com.plushnode.atlacoremobs.wrappers.pathfinder.*;
import org.bukkit.entity.Entity;

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

    public static void setDefaultAI(Entity entity) {
        if (!ReflectionUtil.isInsentient(entity)) return;

        PathfinderGoalSelector goalSelector = new PathfinderGoalSelector(entity, PathfinderGoalSelector.SelectorType.Goal);

        goalSelector.setGoal(0, new PathfinderGoalFloat(entity));
        goalSelector.setGoal(5, new PathfinderGoalMoveTowardsRestriction(entity, 1.0));
        goalSelector.setGoal(7, new PathfinderGoalRandomStroll(entity, 1.0));
        goalSelector.setGoal(8, new PathfinderGoalLookAtPlayer(entity, ReflectionUtil.EntityHuman, 1.0f));
        goalSelector.setGoal(8, new PathfinderGoalRandomLookaround(entity));

        PathfinderGoalSelector targetSelector = new PathfinderGoalSelector(entity, PathfinderGoalSelector.SelectorType.Target);

        goalSelector.setGoal(2, new PathfinderGoalMeleeAttack(entity, 1.0, true));
        targetSelector.setGoal(2, new PathfinderGoalNearestAttackableTarget(entity, ReflectionUtil.EntityHuman, 0, true, false, null));
    }
}
