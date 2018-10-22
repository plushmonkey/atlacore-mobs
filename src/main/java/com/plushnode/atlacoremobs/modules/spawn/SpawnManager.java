package com.plushnode.atlacoremobs.modules.spawn;

import com.plushnode.atlacoremobs.AtlaCoreMobsPlugin;
import com.plushnode.atlacoremobs.ScriptedUser;
import com.plushnode.atlacoremobs.generator.ScriptedFirebenderGenerator;
import com.plushnode.atlacoremobs.generator.ScriptedUserGenerator;
import com.plushnode.atlacoremobs.modules.spawn.commands.ClearSpawnsCommand;
import com.plushnode.atlacoremobs.modules.spawn.commands.SpawnCommand;
import com.plushnode.atlacoremobs.modules.spawn.listeners.SpawnListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

// Manages all of the entities created from the spawn command.
public class SpawnManager {
    private AtlaCoreMobsPlugin plugin;
    private Set<LivingEntity> spawns = new HashSet<>();

    public SpawnManager(AtlaCoreMobsPlugin plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(new SpawnListener(this), plugin);

        plugin.getCommandMultiplexer().registerCommand(new SpawnCommand(plugin));
        plugin.getCommandMultiplexer().registerCommand(new ClearSpawnsCommand(plugin));
    }

    public ScriptedUser spawn(Player creator, EntityType type, ScriptedUserGenerator generator) {
        Entity e = creator.getWorld().spawnEntity(creator.getLocation(), type);

        if (!(e instanceof LivingEntity)) {
            e.remove();
            return null;
        }

        LivingEntity entity = (LivingEntity) e;

        spawns.add(entity);

        return plugin.getUserService().create(entity, generator);
    }

    public boolean isSpawn(LivingEntity entity) {
        return spawns.contains(entity);
    }

    public void removeSpawn(LivingEntity entity) {
        spawns.remove(entity);
    }

    public int destroySpawns() {
        int size = spawns.size();

        spawns.forEach(LivingEntity::remove);
        spawns.clear();

        return size;
    }
}
