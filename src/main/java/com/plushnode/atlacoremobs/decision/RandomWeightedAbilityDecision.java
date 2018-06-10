package com.plushnode.atlacoremobs.decision;

import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.internal.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import com.plushnode.atlacoremobs.ScriptedUser;
import com.plushnode.atlacoremobs.actions.NullAction;
import com.plushnode.atlacoremobs.actions.PunchActivateAction;
import com.plushnode.atlacoremobs.actions.SneakActivateAction;

import java.util.*;

public class RandomWeightedAbilityDecision implements DecisionTreeNode {
    private static Map<String, Integer> sneakMap = new HashMap<>();

    private ScriptedUser user;
    private Random rand;
    private long delay;
    private Map<AbilityDescription, Double> weights;

    static {
        sneakMap.put("Shockwave", 2500);
        sneakMap.put("Lightning", 3500);
        sneakMap.put("Combustion", 2500);
        sneakMap.put("FireBurst", 3500);
        sneakMap.put("AirShield", 1500);
        sneakMap.put("Suffocate", 3500);
        sneakMap.put("Tornado", 2500);
        sneakMap.put("AirStream", 2500);
    }

    public RandomWeightedAbilityDecision(ScriptedUser user, long delay, Map<AbilityDescription, Double> weights) {
        this.user = user;
        this.rand = new Random();
        this.delay = delay;
        this.weights = weights;
    }

    @Override
    public DecisionAction decide() {
        AbilityDescription selected = selectAbility();
        // Generate a random delay up to the max delay.
        long selectedDelay = (int)(Math.random() * this.delay);

        if (selected == null) {
            return new NullAction();
        }

        if (sneakMap.containsKey(selected.getName())) {
            int delay = sneakMap.get(selected.getName());

            return new SneakActivateAction(user, selected, delay);
        }

        if (selected.getName().equalsIgnoreCase("AirScooter")) {
            return new PunchActivateAction(user, selected.getName(), selectedDelay, false, (v) -> user.setVelocity(new Vector3D(0, 0.5, 0)));
        }

        return new PunchActivateAction(user, selected.getName(), selectedDelay, false, (v) -> {});
    }

    private AbilityDescription selectAbility() {
        List<AbilityDescription> abilities = getAbilities();
        List<Double> currentWeights = new ArrayList<>();

        if (abilities.isEmpty()) {
            return null;
        }

        double totalWeight = 0.0;
        for (AbilityDescription ability : abilities) {
            Double currentWeight = weights.get(ability);
            double w = 0.0;

            if (currentWeight != null) {
                w = currentWeight;
            }

            currentWeights.add(w);
            totalWeight += w;
        }

        double randomWeight = rand.nextDouble() * totalWeight;

        for (int i = 0; i < abilities.size(); ++i) {
            AbilityDescription desc = abilities.get(i);
            double weight = currentWeights.get(i);

            randomWeight -= weight;

            if (randomWeight <= 0) {
                return desc;
            }
        }

        return null;
    }

    private List<AbilityDescription> getAbilities() {
        List<AbilityDescription> abilities = new ArrayList<>();

        for (int slotIndex = 1; slotIndex <= 9; ++slotIndex) {
            AbilityDescription desc = user.getSlotAbility(slotIndex);
            if (desc != null && !user.isOnCooldown(desc)) {
                abilities.add(desc);
            }
        }

        return abilities;
    }
}
