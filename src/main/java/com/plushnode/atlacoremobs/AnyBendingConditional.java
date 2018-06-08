package com.plushnode.atlacoremobs;

import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.conditionals.BendingConditional;
import com.plushnode.atlacore.platform.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Allows a user to bend if any of the contained conditionals are true.
public class AnyBendingConditional implements BendingConditional {
    private List<BendingConditional> conditionals = new ArrayList<>();

    public AnyBendingConditional() {

    }

    public AnyBendingConditional(BendingConditional... conditionals) {
        this.conditionals = new ArrayList<>(Arrays.asList(conditionals));
    }

    public void addConditional(BendingConditional conditional) {
        this.conditionals.add(conditional);
    }

    @Override
    public boolean canBend(User user, AbilityDescription abilityDescription) {
        return conditionals.stream().anyMatch((cond) -> cond.canBend(user, abilityDescription));
    }
}
