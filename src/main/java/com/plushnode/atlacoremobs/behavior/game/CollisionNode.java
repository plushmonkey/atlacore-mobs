package com.plushnode.atlacoremobs.behavior.game;

import com.plushnode.atlacore.collision.Collider;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacoremobs.ScriptedUser;
import com.plushnode.atlacoremobs.behavior.BehaviorNode;
import com.plushnode.atlacoremobs.behavior.ExecuteContext;
import com.plushnode.atlacoremobs.behavior.ExecuteResult;

public class CollisionNode<T extends Ability> implements BehaviorNode {
    private Class<T> type;

    public CollisionNode(Class<T> type) {
        this.type = type;
    }

    @Override
    public ExecuteResult execute(ExecuteContext ctx) {
        ScriptedUser caster = ctx.getUser();
        User target = ctx.getUser().getTarget();

        for (T instance : Game.getAbilityInstanceManager().getPlayerInstances(caster, type)) {
            for (Collider collider : instance.getColliders()) {
                if (collider.intersects(target.getBounds().at(target.getLocation()))) {
                    return ExecuteResult.Success;
                }
            }
        }

        return ExecuteResult.Failure;
    }
}
