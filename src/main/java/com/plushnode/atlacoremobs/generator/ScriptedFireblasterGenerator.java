package com.plushnode.atlacoremobs.generator;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.element.Elements;
import com.plushnode.atlacoremobs.ScriptedUser;
import com.plushnode.atlacoremobs.behavior.BehaviorNode;
import com.plushnode.atlacoremobs.behavior.game.ActivateNode;
import com.plushnode.atlacoremobs.decision.RandomWeightedAbilityDecision;
import org.bukkit.entity.LivingEntity;

import java.util.*;

// Generate a test entity that only uses FireBlast.
public class ScriptedFireblasterGenerator implements ScriptedUserGenerator {
    @Override
    public ScriptedUser generate(LivingEntity entity) {
        Map<AbilityDescription, Double> weights = new HashMap<>();

        weights.put(Game.getAbilityRegistry().getAbilityByName("FireBlast"), 15.0);

        ScriptedUser user = new ScriptedUser(entity);

        user.addElement(Elements.FIRE);
        user.addElement(Elements.AIR);

        user.setSlotAbility(1, Game.getAbilityRegistry().getAbilityByName("FireBlast"));

        BehaviorNode tree = new ActivateNode(ActivationMethod.Punch);
        //user.setBehaviorTree(tree);
        user.setDecisionTree(new RandomWeightedAbilityDecision(user, 1500, weights));

        return user;
    }
}
