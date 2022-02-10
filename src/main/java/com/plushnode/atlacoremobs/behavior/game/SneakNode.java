package com.plushnode.atlacoremobs.behavior.game;

import com.plushnode.atlacoremobs.behavior.BehaviorNode;
import com.plushnode.atlacoremobs.behavior.ExecuteContext;
import com.plushnode.atlacoremobs.behavior.ExecuteResult;

public class SneakNode implements BehaviorNode {
    private boolean sneak;

    public SneakNode(boolean sneak) {
        this.sneak = sneak;
    }

    @Override
    public ExecuteResult execute(ExecuteContext ctx) {
        ctx.getUser().setSneaking(sneak);
        return ExecuteResult.Success;
    }
}
