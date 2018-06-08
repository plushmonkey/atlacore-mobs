package com.plushnode.atlacoremobs.decision;

import java.util.function.Supplier;

public class RangeDecision extends Decision {
    private Supplier<Double> testFunction;
    private double min;
    private double max;

    public RangeDecision(Supplier<DecisionTreeNode> trueNode, Supplier<DecisionTreeNode> falseNode, Supplier<Double> testFunction, double min, double max) {
        super(trueNode, falseNode);

        this.testFunction = testFunction;
        this.min = min;
        this.max = max;
    }

    @Override
    public Supplier<DecisionTreeNode> getBranch() {
        double test = testFunction.get();

        if (min <= test && test <= max) {
            return trueNode;
        }

        return falseNode;
    }
}
