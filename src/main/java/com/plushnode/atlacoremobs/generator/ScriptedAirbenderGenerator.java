package com.plushnode.atlacoremobs.generator;

import com.plushnode.atlacore.collision.Collider;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.air.sequences.Twister;
import com.plushnode.atlacore.game.element.Elements;
import com.plushnode.atlacore.internal.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.VectorUtil;
import com.plushnode.atlacore.util.WorldUtil;
import com.plushnode.atlacoremobs.ScriptedUser;
import com.plushnode.atlacoremobs.actions.NullAction;
import com.plushnode.atlacoremobs.actions.air.AirBlastAction;
import com.plushnode.atlacoremobs.actions.air.AirScooterAction;
import com.plushnode.atlacoremobs.actions.air.AirSweepAction;
import com.plushnode.atlacoremobs.actions.air.TornadoAction;
import com.plushnode.atlacoremobs.decision.BooleanDecision;
import com.plushnode.atlacoremobs.decision.DecisionTreeNode;
import com.plushnode.atlacoremobs.decision.RandomWeightedAbilityDecision;
import com.plushnode.atlacoremobs.decision.RangeDecision;
import org.bukkit.entity.LivingEntity;

import java.util.*;

public class ScriptedAirbenderGenerator implements ScriptedUserGenerator {
    @Override
    public ScriptedUser generate(LivingEntity entity) {
        Map<AbilityDescription, Double> weights = new HashMap<>();

        weights.put(Game.getAbilityRegistry().getAbilityByName("AirBlast"), 1.0);
        weights.put(Game.getAbilityRegistry().getAbilityByName("AirSwipe"), 13.0);
        weights.put(Game.getAbilityRegistry().getAbilityByName("Twister"), 1.5);
        weights.put(Game.getAbilityRegistry().getAbilityByName("AirShield"), 5.0);

        ScriptedUser user = new ScriptedUser(entity);

        user.addElement(Elements.AIR);

        user.setSlotAbility(1, Game.getAbilityRegistry().getAbilityByName("AirBlast"));
        user.setSlotAbility(2, Game.getAbilityRegistry().getAbilityByName("AirSwipe"));
        user.setSlotAbility(3, Game.getAbilityRegistry().getAbilityByName("AirScooter"));
        user.setSlotAbility(4, Game.getAbilityRegistry().getAbilityByName("Tornado"));
        user.setSlotAbility(5, Game.getAbilityRegistry().getAbilityByName("AirShield"));
        //user.setSlotAbility(6, Game.getAbilityRegistry().getAbilityByName("Suffocate"));
        user.setSlotAbility(7, Game.getAbilityRegistry().getAbilityByName("Twister"));
        user.setSlotAbility(8, Game.getAbilityRegistry().getAbilityByName("AirSweep"));
        //user.setSlotAbility(9, Game.getAbilityRegistry().getAbilityByName("AirStream"));

        AbilityDescription tornadoDesc = Game.getAbilityRegistry().getAbilityByName("Tornado");
        AbilityDescription scooterDesc = Game.getAbilityRegistry().getAbilityByName("AirScooter");
        AbilityDescription airBlastDesc = Game.getAbilityRegistry().getAbilityByName("AirBlast");
        AbilityDescription airSweepDesc = Game.getAbilityRegistry().getAbilityByName("AirSweep");

        DecisionTreeNode fallbackNode = new RandomWeightedAbilityDecision(user, 500, weights);

        Random rand = new Random();

        // Fall back to a random ability if near the target, otherwise do nothing.
        DecisionTreeNode closeRangeDecision = new BooleanDecision(() -> fallbackNode, () -> NullAction::new, () -> {
            return user.getLocation().distanceSquared(user.getTarget().getLocation()) < 15 * 15;
        });

        DecisionTreeNode tornadoDecision = new BooleanDecision(() -> {
            return new TornadoAction(user, rand.nextInt(2000) + rand.nextInt(2000));
        }, () -> closeRangeDecision, () -> Math.random() < (1.0 / 8.0) && !user.isOnCooldown(tornadoDesc) && WorldUtil.isOnGround(user));

        DecisionTreeNode sweepDecision = new BooleanDecision(() -> {
            return new AirSweepAction(user);
        }, () -> tornadoDecision, () -> !user.isOnCooldown(airSweepDesc) && user.getLocation().distanceSquared(user.getTarget().getLocation()) < 15 * 15);

        DecisionTreeNode scooterDecision = new BooleanDecision(() -> {
            return new AirScooterAction(user, 10.0);
        }, () -> sweepDecision, () -> !user.isOnCooldown(scooterDesc) && WorldUtil.distanceAboveGround(user, Collections.singleton(Material.WATER)) < 4.0);

        DecisionTreeNode airBlastDecision = new BooleanDecision(() -> {
            User target = user.getTarget();

            double t = 0.75;
            if (target != null) {
                Vector3D dir = VectorUtil.normalizeOrElse(target.getLocation().subtract(user.getLocation()).toVector(), Vector3D.PLUS_J);
                t = 1.0 - dir.dotProduct(Vector3D.PLUS_J);
            }

            return new AirBlastAction(user, t);
        }, () -> scooterDecision, () -> {
            User target = user.getTarget();
            if (target == null) {
                return false;
            }

            // Only activate AirBlast when target is above user.
            if (target.getLocation().getY() - user.getLocation().getY() < 4.0) {
                return false;
            }

            // Don't activate AirBlast when climbing fast.
            // This makes it activate like a player would, by hopping at the peak of jumps.
            if (user.getVelocity().getY() > 1.0) {
                return false;
            }

            if (isInTwister(user, target)) {
                return false;
            }

            return !user.isOnCooldown(airBlastDesc);
        });

        user.setDecisionTree(airBlastDecision);

        return user;
    }

    private static boolean isInTwister(User caster, User target) {
        for (Twister twister : Game.getAbilityInstanceManager().getPlayerInstances(caster, Twister.class)) {
            for (Collider collider : twister.getColliders()) {
                if (collider.intersects(target.getBounds().at(target.getLocation()))) {
                    return true;
                }
            }
        }

        return false;
    }
}
