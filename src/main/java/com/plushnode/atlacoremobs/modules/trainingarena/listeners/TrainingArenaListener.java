package com.plushnode.atlacoremobs.modules.trainingarena.listeners;

import com.plushnode.atlacore.game.element.Element;
import com.plushnode.atlacore.game.element.Elements;
import com.plushnode.atlacoremobs.AtlaCoreMobsPlugin;
import com.plushnode.atlacoremobs.ScriptedUser;
import com.plushnode.atlacoremobs.modules.trainingarena.TrainingArenaModule;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class TrainingArenaListener implements Listener {
    private TrainingArenaModule module;

    public TrainingArenaListener(TrainingArenaModule module) {
        this.module = module;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (dead instanceof Player) return;

        Player killer = dead.getKiller();
        if (killer == null) return;

        if (!module.isSpawn(dead)) return;

        module.getKillTracker().addKill(killer);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player dead = event.getEntity();

        if (module.inArena(dead.getLocation())) {
            int kills = module.getKillTracker().getAndReset(dead);

            EntityDamageEvent lastCause = dead.getLastDamageCause();
            if (!(lastCause instanceof EntityDamageByEntityEvent)) return;

            Entity killer = ((EntityDamageByEntityEvent) lastCause).getDamager();
            if (!(killer instanceof LivingEntity) || !module.isSpawn((LivingEntity)killer)) return;

            ScriptedUser killerUser = AtlaCoreMobsPlugin.plugin.getUserService().get(killer);
            if (killerUser == null) return;

            String message = dead.getName() + "[" + kills + "]" + " was slain by " + createKillerMessage(killerUser);

            event.setDeathMessage(message);
        }
    }

    private String createKillerMessage(ScriptedUser killer) {
        String name = killer.getBukkitEntity().getName();

        if (killer.getBukkitEntity() instanceof Villager) {
            Villager villager = (Villager)killer.getBukkitEntity();
            name = villager.getProfession().toString();

            name = name.replace("_", "");

            if (name.length() > 1) {
                name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
            }
        }

        if (killer.getElements().isEmpty()) {
            return name;
        }

        Element element = killer.getElements().get(0);

        String benderType = "";
        String article = "";

        if (element.equals(Elements.AIR)) {
            article = "an ";
            benderType = "Airbending ";
        } else if (element.equals(Elements.EARTH)) {
            article = "an ";
            benderType = "Earthbending ";
        } else if (element.equals(Elements.FIRE)) {
            article = "a ";
            benderType = "Firebending ";
        } else if (element.equals(Elements.WATER)) {
            article = "a ";
            benderType = "Waterbending ";
        }

        return article + element.getColor().toString() + benderType + name;
    }
}
