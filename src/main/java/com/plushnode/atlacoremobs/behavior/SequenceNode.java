package com.plushnode.atlacoremobs.behavior;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SequenceNode implements BehaviorNode {
    private List<BehaviorNode> children;
    private int runningIndex = 0;

    public SequenceNode(List<BehaviorNode> children) {
        this.children = children;
    }

    public SequenceNode(BehaviorNode... nodes) {
        this.children = new ArrayList<>(Arrays.asList(nodes));
    }

    @Override
    public ExecuteResult execute(ExecuteContext ctx) {
        int index = 0;

        if (runningIndex < children.size()) {
            index = runningIndex;
        }

        for (; index < children.size(); ++index) {
            BehaviorNode node = children.get(index);
            ExecuteResult result = node.execute(ctx);

            if (result == ExecuteResult.Failure) {
                runningIndex = 0;
                return result;
            } else if (result == ExecuteResult.Running) {
                runningIndex = index;
                return result;
            }
        }

        runningIndex = 0;

        return ExecuteResult.Success;
    }
}
