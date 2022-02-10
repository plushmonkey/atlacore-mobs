package com.plushnode.atlacoremobs.behavior.game;

import com.plushnode.atlacoremobs.behavior.BehaviorNode;
import com.plushnode.atlacoremobs.behavior.ExecuteContext;
import com.plushnode.atlacoremobs.behavior.ExecuteResult;

import java.util.Random;

// Selects a random slot that has something bound to it.
public class RandomSlotNode implements BehaviorNode {
    @Override
    public ExecuteResult execute(ExecuteContext ctx) {
        for (int i = 1; i <= 9; ++i) {
            if (ctx.getUser().getSlotAbility(i) == null) {
                if (i == 1) {
                    // Should this be failure?
                    return ExecuteResult.Success;
                }

                ctx.getUser().setSelectedIndex(new Random().nextInt(i - 1) + 1);
                break;
            }
        }

        return ExecuteResult.Success;
    }
}
