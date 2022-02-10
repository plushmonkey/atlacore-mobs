package com.plushnode.atlacoremobs.behavior;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ParallelNode implements BehaviorNode {
    private List<BehaviorNode> children;
    private List<BehaviorNode> runningChildren = new ArrayList<>();

    public ParallelNode(List<BehaviorNode> children) {
        this.children = children;
    }

    @Override
    public ExecuteResult execute(ExecuteContext ctx) {
        ExecuteResult result = ExecuteResult.Success;

        if (!runningChildren.isEmpty()) {
            return executeRunning(ctx);
        }

        // Always execute each child and prioritize return result as failure, running, then success
        for (BehaviorNode node : children) {
            ExecuteResult child_result = node.execute(ctx);

            if (child_result == ExecuteResult.Running) {
                runningChildren.add(node);
            }

            if (result != ExecuteResult.Failure && child_result != ExecuteResult.Success) {
                result = child_result;
            }
        }

        return result;
    }

    private ExecuteResult executeRunning(ExecuteContext ctx) {
        ExecuteResult result = ExecuteResult.Success;

        for (Iterator<BehaviorNode> iter = runningChildren.iterator(); iter.hasNext();) {
            BehaviorNode node = iter.next();

            ExecuteResult child_result = node.execute(ctx);

            if (result != ExecuteResult.Failure && child_result != ExecuteResult.Success) {
                result = child_result;
            }

            if (child_result != ExecuteResult.Running) {
                iter.remove();
            }
        }

        return result;
    }
}
