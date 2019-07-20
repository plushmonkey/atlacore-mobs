package com.plushnode.atlacoremobs;

import com.plushnode.atlacore.internal.apache.commons.math3.distribution.NormalDistribution;
import com.plushnode.atlacore.internal.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import com.plushnode.atlacore.platform.BukkitBendingPlayer;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.User;

public class GaussianAimPolicy implements AimPolicy {
    private NormalDistribution distribution;
    private double accuracyRange;
    private boolean predictive;

    public GaussianAimPolicy(double mean, double sd, double accuracyRange, boolean predictive) {
        this.distribution = new NormalDistribution(mean, sd);
        this.accuracyRange = accuracyRange;
        this.predictive = predictive;
    }

    @Override
    public Location getTarget(ScriptedUser user, User target) {
        Location aimCenter = target.getLocation().clone().add(0, 1.25, 0);

        double distance = aimCenter.distance(user.getEyeLocation());

        final double projSpeed = 5.0;

        Vector3D targetVelocity = Vector3D.ZERO;

        if (target instanceof BukkitBendingPlayer) {
            targetVelocity = AtlaCoreMobsPlugin.plugin.getTrackingService().getVelocity(((BukkitBendingPlayer) target).getBukkitPlayer());
        }

        Location aim = aimCenter;
        if (predictive) {
            Vector3D aimVec = calculateShot(user.getEyeLocation().toVector(), aimCenter.toVector(), user.getVelocity(), targetVelocity, projSpeed);
            aim = aimCenter.getWorld().getLocation(aimVec);
        }

        // Reduce accuracy when aiming far away.
        double scale = distance / (accuracyRange / 2.0);

        scale = Math.min(scale, 3.0);

        Vector3D aimOffset = new Vector3D(distribution.sample(3)).scalarMultiply(scale);

        return aim.add(aimOffset);
    }

    // Returns the aiming position
    private Vector3D calculateShot(Vector3D shooter, Vector3D target, Vector3D shooterVel, Vector3D targetVel, double projectileSpeed) {
        Vector3D toTarget = target.subtract(shooter);
        Vector3D relativeVel = targetVel.subtract(shooterVel);

        double a = relativeVel.dotProduct(relativeVel) - (projectileSpeed * projectileSpeed);
        double b = 2.0 * relativeVel.dotProduct(toTarget);
        double c = toTarget.dotProduct(toTarget);

        double discriminant = (b * b) - 4 * a * c;
        double t = -1.0;

        if (discriminant >= 0.0) {
            double t1 = (-b + Math.sqrt(discriminant)) / (2 * a);
            double t2 = (-b - Math.sqrt(discriminant)) / (2 * a);

            if (t1 < t2 && t1 >= 0) {
                t = t1;
            } else {
                t = t2;
            }
        }

        if (t < 0) {
            return target;
        }

        return target.add(relativeVel.scalarMultiply(t));
    }
}
