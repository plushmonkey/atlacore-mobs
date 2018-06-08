package com.plushnode.atlacoremobs.decision;

import java.util.function.Supplier;

public class BooleanDecision extends Decision {
    private Supplier<Boolean> testFunction;

    public BooleanDecision(Supplier<DecisionTreeNode> trueNode, Supplier<DecisionTreeNode> falseNode, Supplier<Boolean> testFunction) {
        super(trueNode, falseNode);

        this.testFunction = testFunction;
    }

    @Override
    public Supplier<DecisionTreeNode> getBranch() {
        if (testFunction.get()) {
            return trueNode;
        }

        return falseNode;
    }
}
