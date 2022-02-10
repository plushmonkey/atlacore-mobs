package com.plushnode.atlacoremobs.behavior;

import com.plushnode.atlacoremobs.ScriptedUser;

public class ExecuteContext {
    private Blackboard blackboard;
    private ScriptedUser user;

    public ExecuteContext(Blackboard blackboard, ScriptedUser user) {
        this.blackboard = blackboard;
        this.user = user;
    }

    public Blackboard getBlackboard() {
        return this.blackboard;
    }

    public ScriptedUser getUser() {
        return this.user;
    }
}

