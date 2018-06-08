package com.plushnode.atlacoremobs.decision;

import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.internal.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import com.plushnode.atlacoremobs.ScriptedUser;
import com.plushnode.atlacoremobs.actions.NullAction;
import com.plushnode.atlacoremobs.actions.PunchActivateAction;
import com.plushnode.atlacoremobs.actions.SneakActivateAction;

import java.util.*;

public class RandomAbilityDecision implements DecisionTreeNode {
    private static Map<String, Integer> sneakMap = new HashMap<>();

    private ScriptedUser user;
    private Random rand;
    private long delay;

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

    public RandomAbilityDecision(ScriptedUser user, long delay) {
        this.user = user;
        this.rand = new Random();
        this.delay = delay;
    }

    @Override
    public DecisionAction decide() {
        List<AbilityDescription> abilities = getAbilities();

        if (abilities.isEmpty()) {
            return new NullAction();
        }

        AbilityDescription selected = abilities.get(rand.nextInt(abilities.size()));
        // Generate a random delay up to the max delay.
        long selectedDelay = (int)(Math.random() * this.delay);

        if (sneakMap.containsKey(selected.getName())) {
            int delay = sneakMap.get(selected.getName());

            return new SneakActivateAction(user, selected, delay);
        }

        if (selected.getName().equalsIgnoreCase("AirScooter")) {
            return new PunchActivateAction(user, selected.getName(), selectedDelay, false, (v) -> user.setVelocity(new Vector3D(0, 0.5, 0)));
        }

        return new PunchActivateAction(user, selected.getName(), selectedDelay, false, (v) -> {});
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
