package com.plushnode.atlacoremobs.behavior;

public class DelayNode implements BehaviorNode {
    private int delay;

    public DelayNode(int delay) {
        this.delay = delay;
    }

    @Override
    public ExecuteResult execute(ExecuteContext ctx) {
        long time = System.currentTimeMillis();

        Long endTime = ctx.getBlackboard().getLong(BlackboardKeys.DELAY_NODE_END_TIME);

        if (endTime == null) {
            endTime = time + this.delay;
            ctx.getBlackboard().set(BlackboardKeys.DELAY_NODE_END_TIME, endTime);
        } else if (time >= endTime) {
            ctx.getBlackboard().erase(BlackboardKeys.DELAY_NODE_END_TIME);
            return ExecuteResult.Success;
        }

        return ExecuteResult.Running;
    }
}
