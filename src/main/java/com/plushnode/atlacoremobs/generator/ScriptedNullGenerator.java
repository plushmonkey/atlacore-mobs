package com.plushnode.atlacoremobs.generator;


import com.plushnode.atlacoremobs.ScriptedUser;
import com.plushnode.atlacoremobs.actions.NullAction;
import org.bukkit.entity.LivingEntity;

public class ScriptedNullGenerator implements ScriptedUserGenerator {
    @Override
    public ScriptedUser generate(LivingEntity entity) {
        ScriptedUser user = new ScriptedUser(entity);

        user.setDecisionTree(new NullAction());

        return user;
    }
}
