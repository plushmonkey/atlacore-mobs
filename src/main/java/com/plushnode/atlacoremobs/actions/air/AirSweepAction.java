package com.plushnode.atlacoremobs.actions.air;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.internal.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import com.plushnode.atlacore.math.CubicHermiteSpline;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.util.VectorUtil;
import com.plushnode.atlacoremobs.ScriptedUser;
import com.plushnode.atlacoremobs.decision.DecisionAction;

public class AirSweepAction extends DecisionAction {
    private static final double SWEEP_RANGE = 14.0;
    private static final int KNOT_COUNT = 7;

    private ScriptedUser user;
    private AbilityDescription desc;
    private boolean done;
    private boolean started;
    private long startTime;
    private CubicHermiteSpline spline;

    public AirSweepAction(ScriptedUser user) {
        this.user = user;
        this.desc = Game.getAbilityRegistry().getAbilityByName("AirSweep");
        this.done = false;
        this.started = false;
        this.spline = new CubicHermiteSpline(0.1);
    }

    @Override
    public void act() {
        long time = System.currentTimeMillis();

        if (this.desc == null) {
            this.done = true;
            return;
        }

        int index = user.getAbilityIndex("AirSweep");
        if (index == -1) {
            this.done = true;
            return;
        }

        if (!started && !user.isOnCooldown(this.desc)) {
            user.setSelectedIndex(index);

            Ability ability = desc.createAbility();

            if (ability.activate(user, ActivationMethod.Sneak)) {
                Game.getAbilityInstanceManager().addAbility(user, ability);
                this.started = true;
                this.startTime = time;

                Vector3D direction = user.getDirection();
                Vector3D left = VectorUtil.normalizeOrElse(user.getDirection().crossProduct(Vector3D.PLUS_J), Vector3D.PLUS_I);
                Vector3D right = VectorUtil.normalizeOrElse(Vector3D.PLUS_J.crossProduct(user.getDirection()), Vector3D.MINUS_I);

                double t = 0.3;
                Vector3D start = left.scalarMultiply(1.0 - t).add(direction.scalarMultiply(t)).scalarMultiply(SWEEP_RANGE);
                Vector3D end = right.scalarMultiply(1.0 - t).add(direction.scalarMultiply(t)).scalarMultiply(SWEEP_RANGE);

                Location base = user.getLocation().add(0, 1.0, 0);
                spline.addKnot(base.add(start).toVector());

                for (int i = 0; i < (KNOT_COUNT - 2); ++i) {
                    double ct = (i + 1.0) / KNOT_COUNT;
                    double r = Math.random() - Math.random();

                    Vector3D current = start.scalarMultiply(1.0 - ct).add(end.scalarMultiply(ct));
                    current = current.add(new Vector3D(0, r * 2.0, 0));

                    spline.addKnot(base.add(current).toVector());
                }

                spline.addKnot(base.add(end).toVector());
            }
        }

        if (started) {
            if (time < this.startTime + 400) {
                double t = (time - this.startTime) / 400.0;
                Vector3D target = spline.interpolate(t);
                Vector3D toTarget = target.subtract(user.getEyeLocation().toVector());

                user.setScriptedDirection(VectorUtil.normalizeOrElse(toTarget, Vector3D.PLUS_I));
            } else {
                this.done = true;
            }
        }
    }

    @Override
    public String getName() {
        return "AirSweepAction";
    }

    @Override
    public boolean isDone() {
        return done;
    }
}
