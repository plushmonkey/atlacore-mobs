package com.plushnode.atlacoremobs.actions.air;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.internal.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import com.plushnode.atlacore.util.VectorUtil;
import com.plushnode.atlacoremobs.ScriptedUser;
import com.plushnode.atlacoremobs.decision.DecisionAction;

// Sourced self-push action
public class AirBlastAction extends DecisionAction {
    private ScriptedUser user;
    private AbilityDescription desc;
    private boolean done;
    private boolean sourced;
    private long sourcedTime;
    private double t;

    public AirBlastAction(ScriptedUser user, double t) {
        this.user = user;
        this.desc = Game.getAbilityRegistry().getAbilityByName("AirBlast");
        this.done = false;
        this.sourced = false;
        this.t = Math.max(0.0, Math.min(t, 1.0));
    }

    @Override
    public void act() {
        if (this.desc == null) {
            this.done = true;
            return;
        }

        int index = user.getAbilityIndex("AirBlast");
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
                Vector3D direction = VectorUtil.normalizeOrElse(VectorUtil.setY(user.getDirection(), 0), Vector3D.PLUS_I);

                // Set direction between straight up and horizontally toward target.
                direction = Vector3D.PLUS_J.scalarMultiply(1.0 - t).add(direction.scalarMultiply(t));

                user.setScriptedDirection(direction);

                if (ability.activate(user, ActivationMethod.Punch)) {
                    Game.getAbilityInstanceManager().addAbility(user, ability);
                }

                this.done = true;
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
