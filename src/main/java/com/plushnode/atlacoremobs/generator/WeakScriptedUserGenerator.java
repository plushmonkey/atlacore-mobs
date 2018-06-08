package com.plushnode.atlacoremobs.generator;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.air.AirScooter;
import com.plushnode.atlacore.game.element.Elements;
import com.plushnode.atlacore.internal.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacoremobs.ScriptedUser;
import com.plushnode.atlacoremobs.actions.PunchActivateAction;
import com.plushnode.atlacoremobs.decision.BooleanDecision;
import com.plushnode.atlacoremobs.decision.DecisionAction;
import com.plushnode.atlacoremobs.decision.DecisionTreeNode;
import com.plushnode.atlacoremobs.decision.RangeDecision;
import org.bukkit.entity.LivingEntity;

import java.util.function.Supplier;

public class WeakScriptedUserGenerator implements ScriptedUserGenerator {
    @Override
    public ScriptedUser generate(LivingEntity entity) {
        ScriptedUser user = new ScriptedUser(entity);

        AbilityDescription fireBlast = Game.getAbilityRegistry().getAbilityByName("FireBlast");
        AbilityDescription airScooter = Game.getAbilityRegistry().getAbilityByName("AirScooter");

        user.setSlotAbility(1, fireBlast);
        user.setSlotAbility(2, airScooter);

        user.addElement(Elements.FIRE);
        user.addElement(Elements.AIR);
        user.addElement(Elements.EARTH);

        Supplier<DecisionTreeNode> scooterSupplier = () -> {
            return new PunchActivateAction(user, "AirScooter", 0, true, (v) -> user.setVelocity(new Vector3D(0, 0.5, 0)));
        };

        Supplier<DecisionTreeNode> destroyScooterSupplier = () -> new DecisionAction() {
                @Override
                public void act() {
                    Game.getAbilityInstanceManager().destroyInstanceType(user, AirScooter.class);
                }

                @Override
                public String getName() {
                    return "DestroyScooter";
                }
            };

        // Destroy scooter if close to target. Activate if not.
        DecisionTreeNode scooterNode = new RangeDecision(scooterSupplier, destroyScooterSupplier, () -> {
            Location first = user.getTarget().getLocation().setY(0);
            Location second = user.getLocation().setY(0);

            return first.distanceSquared(second);
        }, 5.0 * 5.0, 1000.0 * 1000.0);

        Supplier<DecisionTreeNode> scooterDecisionSupplier = () -> scooterNode;
        Supplier<DecisionTreeNode> fireBlastSupplier = () -> new PunchActivateAction(user, "FireBlast", 0, false, (v) -> {});

        // Create scooter when fire blast is on cooldown. Fire it if it isn't.
        DecisionTreeNode decisionTree = new BooleanDecision(scooterDecisionSupplier, fireBlastSupplier, () -> {
            return user.isOnCooldown(Game.getAbilityRegistry().getAbilityByName("FireBlast"));
        });

        user.setDecisionTree(decisionTree);

        return user;
    }
}
