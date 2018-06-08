package com.plushnode.atlacoremobs.decision;

public abstract class DecisionAction implements DecisionTreeNode {
    @Override
    public DecisionAction decide() {
        return this;
    }

    public abstract void act();

    public boolean isDone() {
        return true;
    }

    public abstract String getName();
}
