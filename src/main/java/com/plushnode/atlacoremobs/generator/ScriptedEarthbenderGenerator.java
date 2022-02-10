package com.plushnode.atlacoremobs.generator;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.element.Elements;
import com.plushnode.atlacore.util.WorldUtil;
import com.plushnode.atlacoremobs.ScriptedUser;
import com.plushnode.atlacoremobs.actions.NullAction;
import com.plushnode.atlacoremobs.actions.earth.EarthBlastAction;
import com.plushnode.atlacoremobs.behavior.BehaviorNode;
import com.plushnode.atlacoremobs.behavior.game.ActivateNode;
import com.plushnode.atlacoremobs.decision.BooleanDecision;
import com.plushnode.atlacoremobs.decision.DecisionTreeNode;
import org.bukkit.entity.LivingEntity;

import java.util.Random;

public class ScriptedEarthbenderGenerator implements ScriptedUserGenerator {
    @Override
    public ScriptedUser generate(LivingEntity entity) {
        ScriptedUser user = new ScriptedUser(entity);

        user.addElement(Elements.EARTH);

        user.setSlotAbility(1, Game.getAbilityRegistry().getAbilityByName("EarthBlast"));
        user.setSlotAbility(2, Game.getAbilityRegistry().getAbilityByName("Catapult"));
        user.setSlotAbility(3, Game.getAbilityRegistry().getAbilityByName("Shockwave"));
        user.setSlotAbility(4, Game.getAbilityRegistry().getAbilityByName("RaiseEarth"));

        AbilityDescription ebDesc = Game.getAbilityRegistry().getAbilityByName("EarthBlast");
        AbilityDescription cataDesc = Game.getAbilityRegistry().getAbilityByName("Catapult");
        AbilityDescription shockWaveDesc = Game.getAbilityRegistry().getAbilityByName("Shockwave");
        AbilityDescription raiseEarthDesc = Game.getAbilityRegistry().getAbilityByName("RaiseEarth");

        Random rand = new Random();

        DecisionTreeNode groundCheck = new BooleanDecision(() -> new EarthBlastAction(user), () -> NullAction::new, () -> {
            return WorldUtil.isOnGround(user) && !user.isOnCooldown(ebDesc);
        });


        BehaviorNode tree = new ActivateNode(ActivationMethod.Punch);
        //user.setBehaviorTree(tree);
        user.setDecisionTree(groundCheck);

        return user;
    }
}
