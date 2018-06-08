package com.plushnode.atlacoremobs.modules.raid.listeners;

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.plushnode.atlacoremobs.modules.raid.Raid;
import com.plushnode.atlacoremobs.modules.raid.TownyRaidGame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;

import java.util.List;

public class RaidEntityListener implements Listener {
    private TownyRaidGame game;

    public RaidEntityListener(TownyRaidGame game) {
        this.game = game;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Raid raid = game.getRaid();
        if (raid == null) {
            return;
        }

        LivingEntity entity = event.getEntity();

        if (raid.isRaidEntity(entity)) {
            Player killer = entity.getKiller();
            if (killer == null) {
                return;
            }

            raid.onKill(killer, entity);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        Raid raid = game.getRaid();
        if (raid == null) return;

        Player player = (Player)event.getDamager();
        if (!event.getEntity().getWorld().equals(player.getWorld())) return;

        Town town = raid.getTown();
        if (town == null) return;

        List<TownBlock> blocks = town.getTownBlocks();
        TownBlock block = TownyRaidGame.getTownBlock(player.getLocation(), town.getWorld());

        // Don't allow players to attack from inside of the town.
        if (blocks.contains(block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityCombust(EntityCombustEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;

        Raid raid = game.getRaid();
        if (raid == null) return;

        if (raid.isRaidEntity((LivingEntity)event.getEntity())) {
            // Stop the scripted entities from burning in the sun.
            event.setCancelled(true);
        }
    }
}
