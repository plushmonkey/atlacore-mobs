package com.plushnode.atlacoremobs.modules.mobarena.listeners;

import com.garbagemule.MobArena.framework.Arena;
import com.plushnode.atlacore.events.BendingPlayerCreateEvent;
import com.plushnode.atlacore.game.conditionals.CompositeBendingConditional;
import com.plushnode.atlacoremobs.AtlaCoreMobsPlugin;
import com.plushnode.atlacoremobs.modules.mobarena.MobArenaBendingConditional;
import com.plushnode.atlacoremobs.modules.mobarena.MobArenaGame;
import com.plushnode.atlacoremobs.generator.ScriptedUserGenerator;
import com.plushnode.atlacoremobs.generator.StrongScriptedUserGenerator;
import com.plushnode.atlacoremobs.generator.WeakScriptedUserGenerator;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class ArenaListener implements Listener {
    private AtlaCoreMobsPlugin plugin;

    public ArenaListener(AtlaCoreMobsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBendingPlayerCreate(BendingPlayerCreateEvent event) {
        //System.out.println("Adding MobArena conditional to user " + user);
        CompositeBendingConditional cond = event.getPlayer().getBendingConditional();
        cond.add(new MobArenaBendingConditional());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;

        LivingEntity entity = (LivingEntity)event.getEntity();

        if (MobArenaGame.isInArena(entity.getLocation())) {
            Arena arena = MobArenaGame.getMobArena().getArenaMaster().getArenaAtLocation(entity.getLocation());
            int waveNumber = arena.getWaveManager().getWaveNumber();

            ScriptedUserGenerator generator;
            if (waveNumber >= 7) {
                // Start by spawning them with 3 slots bound, increment every wave.
                generator = new StrongScriptedUserGenerator(waveNumber - 4);
            } else {
                generator = new WeakScriptedUserGenerator();
            }

            plugin.getUserService().create(entity, generator);
        }
    }
}
