package com.plushnode.atlacoremobs.decision;

import java.util.function.Supplier;

public abstract class Decision implements DecisionTreeNode {
    protected Supplier<DecisionTreeNode> trueNode;
    protected Supplier<DecisionTreeNode> falseNode;

    public Decision(Supplier<DecisionTreeNode> trueNode, Supplier<DecisionTreeNode> falseNode) {
        this.trueNode = trueNode;
        this.falseNode = falseNode;
    }

    @Override
    public DecisionAction decide() {
        return getBranch().get().decide();
    }

    public abstract Supplier<DecisionTreeNode> getBranch();
}
