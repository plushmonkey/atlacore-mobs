package com.plushnode.atlacoremobs.generator;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.element.Elements;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacoremobs.ScriptedUser;
import com.plushnode.atlacoremobs.actions.NullAction;
import com.plushnode.atlacoremobs.actions.PunchActivateAction;
import com.plushnode.atlacoremobs.actions.SneakActivateAction;
import com.plushnode.atlacoremobs.actions.air.AirBlastAction;
import com.plushnode.atlacoremobs.actions.air.AirScooterAction;
import com.plushnode.atlacoremobs.actions.air.AirSweepAction;
import com.plushnode.atlacoremobs.actions.air.TornadoAction;
import com.plushnode.atlacoremobs.decision.BooleanDecision;
import com.plushnode.atlacoremobs.decision.DecisionTreeNode;
import com.plushnode.atlacoremobs.decision.RandomWeightedAbilityDecision;
import org.bukkit.entity.LivingEntity;

import java.util.*;

public class ScriptedAirbenderGenerator implements ScriptedUserGenerator {
    @Override
    public ScriptedUser generate(LivingEntity entity) {
        Map<AbilityDescription, Double> weights = new HashMap<>();

        weights.put(Game.getAbilityRegistry().getAbilityByName("AirBlast"), 1.0);
        weights.put(Game.getAbilityRegistry().getAbilityByName("AirSwipe"), 13.0);
        weights.put(Game.getAbilityRegistry().getAbilityByName("Twister"), 3.0);
        weights.put(Game.getAbilityRegistry().getAbilityByName("AirShield"), 5.0);

        ScriptedUser user = new ScriptedUser(entity);

        user.addElement(Elements.AIR);

        user.setSlotAbility(1, Game.getAbilityRegistry().getAbilityByName("AirBlast"));
        user.setSlotAbility(2, Game.getAbilityRegistry().getAbilityByName("AirSwipe"));
        user.setSlotAbility(3, Game.getAbilityRegistry().getAbilityByName("AirScooter"));
        user.setSlotAbility(4, Game.getAbilityRegistry().getAbilityByName("Tornado"));
        user.setSlotAbility(5, Game.getAbilityRegistry().getAbilityByName("AirShield"));
        user.setSlotAbility(6, Game.getAbilityRegistry().getAbilityByName("Suffocate"));
        user.setSlotAbility(7, Game.getAbilityRegistry().getAbilityByName("Twister"));
        user.setSlotAbility(8, Game.getAbilityRegistry().getAbilityByName("AirSweep"));
        user.setSlotAbility(9, Game.getAbilityRegistry().getAbilityByName("AirStream"));

        AbilityDescription tornadoDesc = Game.getAbilityRegistry().getAbilityByName("Tornado");
        AbilityDescription scooterDesc = Game.getAbilityRegistry().getAbilityByName("AirScooter");
        AbilityDescription airBlastDesc = Game.getAbilityRegistry().getAbilityByName("AirBlast");
        AbilityDescription airSweepDesc = Game.getAbilityRegistry().getAbilityByName("AirSweep");

        DecisionTreeNode fallbackNode = new RandomWeightedAbilityDecision(user, 1500, weights);

        DecisionTreeNode tornadoDecision = new BooleanDecision(() -> {
            return new TornadoAction(user, 3000);
        }, () -> fallbackNode, () -> Math.random() < (1.0 / 5.0) && !user.isOnCooldown(tornadoDesc));

        DecisionTreeNode sweepDecision = new BooleanDecision(() -> {
            return new AirSweepAction(user);
        }, () -> tornadoDecision, () -> !user.isOnCooldown(airSweepDesc));

        DecisionTreeNode scooterDecision = new BooleanDecision(() -> {
            return new AirScooterAction(user, 10.0);
        }, () -> sweepDecision, () -> !user.isOnCooldown(scooterDesc));

        DecisionTreeNode airBlastDecision = new BooleanDecision(() -> {
            return new AirBlastAction(user);
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

            return !user.isOnCooldown(airBlastDesc);
        });

        user.setDecisionTree(airBlastDecision);

        return user;
    }
}
