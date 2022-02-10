package com.plushnode.atlacoremobs.behavior;

public class SuccessNode implements BehaviorNode {
    private BehaviorNode child;

    public SuccessNode(BehaviorNode child) {
        this.child = child;
    }
    @Override
    public ExecuteResult execute(ExecuteContext ctx) {
        child.execute(ctx);
        return ExecuteResult.Success;
    }
}
