package com.plushnode.atlacoremobs.actions;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacoremobs.ScriptedUser;
import com.plushnode.atlacoremobs.decision.DecisionAction;

import java.util.List;
import java.util.stream.Collectors;

public class SneakActivateAction extends DecisionAction {
    private ScriptedUser user;
    private AbilityDescription abilityDescription;
    private Class<? extends Ability> type;
    private boolean done = false;
    private long activateTime;

    public SneakActivateAction(ScriptedUser user, AbilityDescription abilityDescription, long delay) {
        this.user = user;
        this.abilityDescription = abilityDescription;
        this.type = abilityDescription.createAbility().getClass();
        this.activateTime = System.currentTimeMillis() + delay;
    }

    @Override
    public void act() {
        if (abilityDescription == null) {
            done = true;
            return;
        }

        List<Ability> instances = Game.getAbilityInstanceManager().getPlayerInstances(user).stream()
                .filter((ability -> ability.getClass().isAssignableFrom(type)))
                .collect(Collectors.toList());

        if (instances.isEmpty()) {
            if (!user.isOnCooldown(abilityDescription)) {
                user.setSneaking(true);

                Ability ability = abilityDescription.createAbility();

                if (ability.activate(user, ActivationMethod.Sneak)) {
                    Game.getAbilityInstanceManager().addAbility(user, ability);
                    //System.out.println("Creating " + abilityDescription.getName());
                } else {
                    done = true;
                    return;
                }
            }
        }

        if (System.currentTimeMillis() > this.activateTime) {
            user.setSneaking(false);
            done = true;
        }
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public String getName() {
        return "SneakActivate " + abilityDescription.getName();
    }
}
