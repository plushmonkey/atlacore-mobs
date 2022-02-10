package com.plushnode.atlacoremobs.behavior;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomChildNode implements BehaviorNode {
    private List<BehaviorNode> children;
    private List<Double> weights;
    private Random random = new Random();
    private BehaviorNode runningChild;

    public RandomChildNode(List<BehaviorNode> children) {
        this.children = children;

        this.weights = new ArrayList<>();

        for (int i = 0; i < children.size(); ++i) {
            this.weights.add(1.0);
        }
    }

    public RandomChildNode(List<BehaviorNode> children, List<Double> weights) {
        this.children = children;
        this.weights = weights;
    }

    @Override
    public ExecuteResult execute(ExecuteContext ctx) {
        BehaviorNode child = runningChild;

        if (child == null) {
            child = getRandomChild();
        }

        if (child != null) {
            ExecuteResult result = child.execute(ctx);

            if (result == ExecuteResult.Running) {
                this.runningChild = child;
            } else {
                this.runningChild = null;
            }

            return result;
        }

        return ExecuteResult.Failure;
    }

    private BehaviorNode getRandomChild() {
        double totalWeight = getTotalWeight();
        double randomWeight = random.nextDouble() * totalWeight;

        for (int i = 0; i < children.size(); ++i) {
            double weight = weights.get(i);
            randomWeight -= weight;

            if (randomWeight <= 0) {
                return children.get(i);
            }
        }

        return null;
    }

    private double getTotalWeight() {
        double totalWeight = 0.0;

        for (Double weight : weights) {
            totalWeight += weight;
        }

        return totalWeight;
    }
}
