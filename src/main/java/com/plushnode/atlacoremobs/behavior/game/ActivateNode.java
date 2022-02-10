package com.plushnode.atlacoremobs.behavior.game;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacoremobs.behavior.BehaviorNode;
import com.plushnode.atlacoremobs.behavior.ExecuteContext;
import com.plushnode.atlacoremobs.behavior.ExecuteResult;

public class ActivateNode implements BehaviorNode {
    private ActivationMethod method;

    public ActivateNode(ActivationMethod method) {
        this.method = method;
    }

    @Override
    public ExecuteResult execute(ExecuteContext ctx) {
        AbilityDescription abilityDescription = ctx.getUser().getSelectedAbility();

        if (ctx.getUser().isOnCooldown(abilityDescription)) {
            return ExecuteResult.Failure;
        }

        Ability ability = abilityDescription.createAbility();

        if (ability.activate(ctx.getUser(), method)) {
            Game.getAbilityInstanceManager().addAbility(ctx.getUser(), ability);
        }

        return ExecuteResult.Success;
    }
}
