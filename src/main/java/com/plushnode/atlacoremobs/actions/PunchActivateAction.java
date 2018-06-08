package com.plushnode.atlacoremobs.actions;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacoremobs.ScriptedUser;
import com.plushnode.atlacoremobs.decision.DecisionAction;

import java.util.function.Consumer;

public class PunchActivateAction extends DecisionAction {
    private ScriptedUser user;
    private String abilityName;
    private boolean singular;
    private Consumer<Void> action;
    private long endTime;
    private boolean activated;

    public PunchActivateAction(ScriptedUser user, String abilityName, long delay, boolean singular, Consumer<Void> action) {
        this.user = user;
        this.abilityName = abilityName;
        this.singular = singular;
        this.action = action;
        this.endTime = System.currentTimeMillis() + delay;
        this.activated = false;
    }

    @Override
    public boolean isDone() {
        return activated && System.currentTimeMillis() >= endTime;
    }

    @Override
    public void act() {
        if (activated) {
            return;
        }

        AbilityDescription desc = Game.getAbilityRegistry().getAbilityByName(abilityName);
        if (desc == null) {
            activated = true;
            return;
        }

        if (singular && Game.getAbilityInstanceManager().hasAbility(user, desc)) {
            activated = true;
            return;
        }

        if (!user.isOnCooldown(desc)) {
            if (action != null) {
                action.accept(null);
            }

            Ability ability = desc.createAbility();

            if (ability.activate(user, ActivationMethod.Punch)) {
                Game.getAbilityInstanceManager().addAbility(user, ability);
                //System.out.println("Creating " + desc.getName());
            }

            // Some abilities always return false, so just set it as finished no matter what.
            activated = true;
        }
    }

    @Override
    public String getName() {
        return "PunchActivate " + abilityName;
    }
}
