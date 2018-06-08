package com.plushnode.atlacoremobs.generator;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.element.Elements;
import com.plushnode.atlacoremobs.ScriptedUser;
import org.bukkit.entity.LivingEntity;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

// Generates a ScriptedUser that uses any ability.
public class StrongScriptedUserGenerator implements ScriptedUserGenerator {
    private int slots;

    public StrongScriptedUserGenerator(int slots) {
        this.slots = Math.min(9, slots);
    }

    @Override
    public ScriptedUser generate(LivingEntity entity) {
        ScriptedUser user = new ScriptedUser(entity);

        List<AbilityDescription> abilities = Game.getAbilityRegistry().getAbilities().stream()
                .filter((desc) -> !desc.isActivatedBy(ActivationMethod.Passive))
                .filter((desc) -> !desc.getName().equalsIgnoreCase("fireburst"))
                .collect(Collectors.toList());

        Collections.shuffle(abilities);

        // Give the entity random abilities
        for (int slotIndex = 1; slotIndex <= slots && slotIndex <= abilities.size(); ++slotIndex) {
            user.setSlotAbility(slotIndex, abilities.get(slotIndex - 1));
        }

        user.addElement(Elements.FIRE);
        user.addElement(Elements.AIR);
        user.addElement(Elements.EARTH);

        return user;
    }
}
