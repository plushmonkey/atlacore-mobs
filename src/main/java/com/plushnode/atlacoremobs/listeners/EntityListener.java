package com.plushnode.atlacoremobs.listeners;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.fire.HeatControl;
import com.plushnode.atlacore.game.element.Elements;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacoremobs.AtlaCoreMobsPlugin;
import com.plushnode.atlacoremobs.ScriptedUser;
import com.plushnode.atlacoremobs.generator.StrongScriptedUserGenerator;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;

public class EntityListener implements Listener {
    private AtlaCoreMobsPlugin plugin;

    public EntityListener(AtlaCoreMobsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onFireTickDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FIRE && event.getCause() != EntityDamageEvent.DamageCause.FIRE_TICK) return;
        if (event.getEntity() instanceof Player) return;

        ScriptedUser user = plugin.getUserService().get(event.getEntity());
        if (user == null) return;

        int index = user.getAbilityIndex("HeatControl");
        if (index <= 0) return;

        user.setSelectedIndex(index);

        if (!HeatControl.canBurn(user)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (event.getEntity() instanceof Player) return;

        ScriptedUser user = plugin.getUserService().get(event.getEntity());
        if (user == null) return;

        if (user.hasElement(Elements.AIR)) {
            event.setCancelled(true);
        }

        int index = user.getAbilityIndex("Shockwave");
        if (index <= 0) return;

        user.setSelectedIndex(index);

        AbilityDescription desc = Game.getAbilityRegistry().getAbilityByName("Shockwave");

        if (!user.isOnCooldown(desc)) {
            Ability ability = desc.createAbility();

            if (ability.activate(user, ActivationMethod.Fall)) {
                Game.getAbilityInstanceManager().addAbility(user, ability);
                //System.out.println("Creating " + desc.getName());
            }
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;

        if (event.getTarget() == null) {
            ScriptedUser scriptedUser = plugin.getUserService().get(event.getEntity());
            if (scriptedUser != null) {
                scriptedUser.setTarget(null);
            }

            return;
        }

        if (!(event.getTarget() instanceof Player)) {
            event.setCancelled(true);
            return;
        }

        User user = Game.getPlayerService().getPlayerByName(event.getTarget().getName());
        if (user == null) return;

        ScriptedUser scriptedEntity = plugin.getUserService().get(event.getEntity());
        if (scriptedEntity != null) {
            scriptedEntity.setTarget(user);
        }
    }
}
