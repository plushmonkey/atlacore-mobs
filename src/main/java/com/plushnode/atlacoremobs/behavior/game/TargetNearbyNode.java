package com.plushnode.atlacoremobs.behavior.game;

import com.plushnode.atlacoremobs.ScriptedUser;
import com.plushnode.atlacoremobs.behavior.BehaviorNode;
import com.plushnode.atlacoremobs.behavior.ExecuteContext;
import com.plushnode.atlacoremobs.behavior.ExecuteResult;

public class TargetNearbyNode implements BehaviorNode {
    private double distance;

    public TargetNearbyNode(double distance) {
        this.distance = distance;
    }

    @Override
    public ExecuteResult execute(ExecuteContext ctx) {
        ScriptedUser user = ctx.getUser();
        boolean nearby = user.getLocation().distanceSquared(user.getTarget().getLocation()) <= distance * distance;

        return nearby ? ExecuteResult.Success : ExecuteResult.Failure;
    }
}
