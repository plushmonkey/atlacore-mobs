package com.plushnode.atlacoremobs.behavior;

import java.util.List;

public class SelectorNode implements BehaviorNode {
    private List<BehaviorNode> children;
    private int runningIndex = 0;

    public SelectorNode(List<BehaviorNode> children) {
        this.children = children;
    }

    @Override
    public ExecuteResult execute(ExecuteContext ctx) {
        ExecuteResult result = ExecuteResult.Failure;

        int index = 0;

        if (runningIndex < children.size()) {
            index = runningIndex;
        }

        for (; index < children.size(); ++index) {
            BehaviorNode node = children.get(index);
            ExecuteResult child_result = node.execute(ctx);

            if (child_result == ExecuteResult.Running) {
                runningIndex = index;
            } else {
                runningIndex = 0;
            }

            if (child_result != ExecuteResult.Failure) {
                return child_result;
            }
        }

        runningIndex = 0;

        return result;
    }
}
