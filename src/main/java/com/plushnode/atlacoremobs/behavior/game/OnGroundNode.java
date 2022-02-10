package com.plushnode.atlacoremobs.behavior.game;

import com.plushnode.atlacore.util.WorldUtil;
import com.plushnode.atlacoremobs.behavior.BehaviorNode;
import com.plushnode.atlacoremobs.behavior.ExecuteContext;
import com.plushnode.atlacoremobs.behavior.ExecuteResult;

public class OnGroundNode implements BehaviorNode {
    @Override
    public ExecuteResult execute(ExecuteContext ctx) {
        return WorldUtil.isOnGround(ctx.getUser()) ? ExecuteResult.Success : ExecuteResult.Failure;
    }
}
