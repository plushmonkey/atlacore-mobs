package com.plushnode.atlacoremobs.behavior.game;

import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacoremobs.behavior.BehaviorNode;
import com.plushnode.atlacoremobs.behavior.ExecuteContext;
import com.plushnode.atlacoremobs.behavior.ExecuteResult;

public class StartableNode implements BehaviorNode {
    private AbilityDescription abilityDescription;

    public StartableNode(AbilityDescription abilityDescription) {
        this.abilityDescription = abilityDescription;
    }

    @Override
    public ExecuteResult execute(ExecuteContext ctx) {
        return ctx.getUser().isOnCooldown(abilityDescription) ? ExecuteResult.Failure : ExecuteResult.Success;
    }
}
