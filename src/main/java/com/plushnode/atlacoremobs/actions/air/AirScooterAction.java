package com.plushnode.atlacoremobs.actions.air;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacoremobs.ScriptedUser;
import com.plushnode.atlacoremobs.decision.DecisionAction;

public class AirScooterAction extends DecisionAction {
    private ScriptedUser user;
    private AbilityDescription desc;
    private double closeDistanceSq;
    private boolean done;

    public AirScooterAction(ScriptedUser user, double closeDistance) {
        this.user = user;
        this.desc = Game.getAbilityRegistry().getAbilityByName("AirScooter");
        this.done = false;
        this.closeDistanceSq = closeDistance * closeDistance;
    }

    @Override
    public void act() {
        if (this.desc == null) {
            this.done = true;
            return;
        }

        int index = user.getAbilityIndex("AirScooter");
        if (index == -1) {
            this.done = true;
            return;
        }

        boolean hasScooter = Game.getAbilityInstanceManager().hasAbility(user, desc);

        if (!hasScooter && !user.isOnCooldown(this.desc)) {
            user.setSelectedIndex(index);

            Ability ability = desc.createAbility();

            user.teleport(user.getLocation().add(0, 1, 0));
            if (ability.activate(user, ActivationMethod.Punch)) {
                Game.getAbilityInstanceManager().addAbility(user, ability);
                hasScooter = true;
            }
        }

        User target = user.getTarget();

        if (target == null || !hasScooter) {
            this.done = true;
            Game.getAbilityInstanceManager().destroyInstanceType(user, desc);
            return;
        }

        // Horizontal distance squared from target.
        double distSq = target.getLocation().setY(0).distanceSquared(user.getLocation().setY(0));
        double dy = target.getLocation().getY() - user.getLocation().getY();

        if (distSq <= closeDistanceSq || dy >= 4.0) {
            this.done = true;
            Game.getAbilityInstanceManager().destroyInstanceType(user, desc);
        }
    }

    @Override
    public String getName() {
        return "AirScooterAction";
    }

    @Override
    public boolean isDone() {
        return done;
    }
}
