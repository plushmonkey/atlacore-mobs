package com.plushnode.atlacoremobs.generator;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.element.Elements;
import com.plushnode.atlacoremobs.ScriptedUser;
import com.plushnode.atlacoremobs.decision.RandomWeightedAbilityDecision;
import org.bukkit.entity.LivingEntity;

import java.util.*;

public class ScriptedFirebenderGenerator implements ScriptedUserGenerator {
    @Override
    public ScriptedUser generate(LivingEntity entity) {
        Map<AbilityDescription, Double> weights = new HashMap<>();

        weights.put(Game.getAbilityRegistry().getAbilityByName("FireBlast"), 15.0);
        weights.put(Game.getAbilityRegistry().getAbilityByName("FireJet"), 1.0);
        weights.put(Game.getAbilityRegistry().getAbilityByName("FireShield"), 1.0);
        weights.put(Game.getAbilityRegistry().getAbilityByName("FireWall"), 1.0);
        weights.put(Game.getAbilityRegistry().getAbilityByName("HeatControl"), 1.0);
        weights.put(Game.getAbilityRegistry().getAbilityByName("Combustion"), 1.5);
        weights.put(Game.getAbilityRegistry().getAbilityByName("Lightning"), 1.0);
        weights.put(Game.getAbilityRegistry().getAbilityByName("Blaze"), 2.0);
        weights.put(Game.getAbilityRegistry().getAbilityByName("FireKick"), 3.0);
        weights.put(Game.getAbilityRegistry().getAbilityByName("JetBlast"), 1.0);
        weights.put(Game.getAbilityRegistry().getAbilityByName("JetBlaze"), 0.8);
        weights.put(Game.getAbilityRegistry().getAbilityByName("FireSpin"), 1.3);
        weights.put(Game.getAbilityRegistry().getAbilityByName("FireWheel"), 1.3);

        ScriptedUser user = new ScriptedUser(entity);

        user.addElement(Elements.FIRE);
        user.addElement(Elements.AIR);

        List<AbilityDescription> abilities = new ArrayList<>(weights.keySet());
        Collections.shuffle(abilities);

        for (int i = 1; i <= 9; ++i) {
            user.setSlotAbility(i, abilities.get(i - 1));
        }

        user.setDecisionTree(new RandomWeightedAbilityDecision(user, 1500, weights));

        return user;
    }
}
