package com.plushnode.atlacoremobs.behavior;

public class InvertNode implements BehaviorNode {
    private BehaviorNode child;

    public InvertNode(BehaviorNode child) {
        this.child = child;
    }
    @Override
    public ExecuteResult execute(ExecuteContext ctx) {
        ExecuteResult result = child.execute(ctx);

        if (result == ExecuteResult.Failure) {
            return ExecuteResult.Success;
        } else if (result == ExecuteResult.Success) {
            return ExecuteResult.Failure;
        }

        return result;
    }
}
