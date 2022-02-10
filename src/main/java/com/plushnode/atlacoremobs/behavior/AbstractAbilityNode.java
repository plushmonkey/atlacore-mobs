package com.plushnode.atlacoremobs.behavior;

import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacoremobs.ScriptedUser;

public abstract class AbstractAbilityNode implements BehaviorNode {
    protected void selectAbility(ScriptedUser user, AbilityDescription description) {
        for (int i = 1; i <= 9; ++i) {
            if (description.equals(user.getSlotAbility(i))) {
                user.setSelectedIndex(i);
                break;
            }
        }
    }
}
