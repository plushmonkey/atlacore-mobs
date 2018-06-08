package com.plushnode.atlacoremobs.actions.air;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.internal.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import com.plushnode.atlacoremobs.ScriptedUser;
import com.plushnode.atlacoremobs.decision.DecisionAction;

// Sourced self-push action
public class AirBlastAction extends DecisionAction {
    private ScriptedUser user;
    private AbilityDescription desc;
    private boolean done;
    private boolean sourced;
    private long sourcedTime;

    public AirBlastAction(ScriptedUser user) {
        this.user = user;
        this.desc = Game.getAbilityRegistry().getAbilityByName("AirBlast");
        this.done = false;
        this.sourced = false;
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

        if (!user.isOnCooldown(this.desc)) {
            user.setSelectedIndex(index);

            Ability ability = desc.createAbility();

            if (!sourced) {
                // Look directly down and then source it.
                user.setScriptedDirection(Vector3D.MINUS_J);

                if (ability.activate(user, ActivationMethod.Sneak)) {
                    Game.getAbilityInstanceManager().addAbility(user, ability);
                    this.sourced = true;
                    this.sourcedTime = System.currentTimeMillis();
                }
            } else if (System.currentTimeMillis() >= this.sourcedTime + 50) {
                Vector3D direction = user.getDirection();

                double t = 0.75;
                // Set direction between straight up and toward target.
                direction = Vector3D.PLUS_J.scalarMultiply(1.0 - t).add(direction.scalarMultiply(t));

                user.setScriptedDirection(direction);

                if (ability.activate(user, ActivationMethod.Punch)) {
                    Game.getAbilityInstanceManager().addAbility(user, ability);
                    this.done = true;
                }
            }
        }
    }

    @Override
    public String getName() {
        return "AirBlastAction";
    }

    @Override
    public boolean isDone() {
        return done;
    }
}
