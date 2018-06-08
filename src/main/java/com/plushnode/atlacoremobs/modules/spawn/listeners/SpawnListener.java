package com.plushnode.atlacoremobs.modules.spawn.listeners;

import com.plushnode.atlacoremobs.modules.spawn.SpawnManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class SpawnListener implements Listener {
    private SpawnManager spawnManager;

    public SpawnListener(SpawnManager spawnManager) {
        this.spawnManager = spawnManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (spawnManager.isSpawn(entity)) {
            spawnManager.removeSpawn(entity);
        }
    }
}
