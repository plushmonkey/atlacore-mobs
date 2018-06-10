package com.plushnode.atlacoremobs.actions.air;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.internal.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacoremobs.ScriptedUser;
import com.plushnode.atlacoremobs.decision.DecisionAction;

public class TornadoAction extends DecisionAction {
    private ScriptedUser user;
    private AbilityDescription desc;
    private boolean done;
    private boolean activated;
    private long startTime;
    private long duration;

    public TornadoAction(ScriptedUser user, long duration) {
        this.user = user;
        this.desc = Game.getAbilityRegistry().getAbilityByName("Tornado");
        this.done = false;
        this.activated = false;
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public void act() {
        if (this.desc == null) {
            this.done = true;
            return;
        }

        int index = user.getAbilityIndex("Tornado");
        if (index == -1) {
            this.done = true;
            return;
        }

        User target = user.getTarget();

        if (target != null) {
            Vector3D toTarget = target.getLocation().subtract(user.getLocation()).toVector();
            Vector3D direction = Vector3D.ZERO;

            if (toTarget.getNormSq() > 0) {
                direction = toTarget.normalize();
            }

            double t = 0.5;
            // Set direction between straight down and toward target.
            direction = Vector3D.MINUS_J.scalarMultiply(1.0 - t).add(direction.scalarMultiply(t));

            user.setScriptedDirection(direction);
        }

        if (!this.activated && !user.isOnCooldown(this.desc)) {
            user.setSelectedIndex(index);
            user.setSneaking(true);

            Ability ability = desc.createAbility();
            if (ability.activate(user, ActivationMethod.Sneak)) {
                Game.getAbilityInstanceManager().addAbility(user, ability);
                this.activated = true;
            }
        }

        if (this.activated && !Game.getAbilityInstanceManager().hasAbility(user, desc)) {
            user.setSneaking(false);
            this.done = true;
        }

        if (System.currentTimeMillis() >= this.startTime + this.duration) {
            user.setSneaking(false);
            this.done = true;
        }
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public String getName() {
        return "TornadoAction";
    }
}
