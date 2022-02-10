package com.plushnode.atlacoremobs.behavior.game;

import com.plushnode.atlacoremobs.behavior.BehaviorNode;
import com.plushnode.atlacoremobs.behavior.ExecuteContext;
import com.plushnode.atlacoremobs.behavior.ExecuteResult;

public class SelectSlotNode implements BehaviorNode {
    private int index;

    public SelectSlotNode(int index) {
        this.index = index;

        if (this.index < 1 || this.index > 9) {
            this.index = 1;
        }
    }

    @Override
    public ExecuteResult execute(ExecuteContext ctx) {
        ctx.getUser().setSelectedIndex(index);
        return ExecuteResult.Success;
    }
}
