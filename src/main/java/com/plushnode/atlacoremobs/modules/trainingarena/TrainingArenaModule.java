package com.plushnode.atlacoremobs.modules.trainingarena;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.util.Task;
import com.plushnode.atlacoremobs.AtlaCoreMobsPlugin;
import com.plushnode.atlacoremobs.ScriptedUser;
import com.plushnode.atlacoremobs.compatibility.projectkorra.ProjectKorraHook;
import com.plushnode.atlacoremobs.generator.ScriptedAirbenderGenerator;
import com.plushnode.atlacoremobs.generator.ScriptedFirebenderGenerator;
import com.plushnode.atlacoremobs.generator.ScriptedUserGenerator;
import com.plushnode.atlacoremobs.util.SpawnUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.*;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class TrainingArenaModule {
    private static final List<EntityType> ENTITY_TYPES = Arrays.asList(EntityType.VILLAGER, EntityType.VILLAGER,
            EntityType.VILLAGER, EntityType.VILLAGER, EntityType.VILLAGER, EntityType.SHEEP,
            EntityType.LLAMA, EntityType.DONKEY, EntityType.COW, EntityType.MUSHROOM_COW);

    private AtlaCoreMobsPlugin plugin;
    private Task updateTask;
    private String regionName;
    private World world;
    private Set<LivingEntity> spawns = new HashSet<>();
    private long nextUpdateTime;
    private long updateDelay;
    private long nextAllowedSpawnTime;
    private long spawnDelay;

    public TrainingArenaModule(AtlaCoreMobsPlugin plugin) {
        this.plugin = plugin;

        boolean trainingArena = plugin.getConfigRoot().getNode("training-arena", "enabled").getBoolean(false);

        if (!trainingArena) {
            return;
        }

        String worldName = plugin.getConfigRoot().getNode("training-arena", "region", "world").getString("world");
        world = Bukkit.getWorld(worldName);

        if (world == null) {
            return;
        }

        this.updateTask = Game.plugin.createTaskTimer(this::update, 1, 1);
        this.regionName = plugin.getConfigRoot().getNode("training-arena", "region", "name").getString("ai-training");
        this.updateDelay = plugin.getConfigRoot().getNode("training-arena", "update-delay").getLong(1000);
        this.spawnDelay = plugin.getConfigRoot().getNode("training-arena", "spawn-delay").getLong(5000);
    }

    public void stop() {
        for (LivingEntity entity : spawns) {
            entity.remove();
        }

        spawns.clear();

        if (this.updateTask != null) {
            this.updateTask.cancel();
        }
    }

    private void update() {
        long time = System.currentTimeMillis();

        if (time < this.nextUpdateTime) {
            return;
        }

        this.nextUpdateTime = time + this.updateDelay;

        RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        if (manager == null) {
            return;
        }

        ProtectedRegion region = manager.getRegion(regionName);

        if (region == null) {
            return;
        }

        Random rand = new Random();

        List<Player> players = getPlayers(region);

        if (players.size() > spawns.size() && time >= this.nextAllowedSpawnTime) {
            EntityType type = ENTITY_TYPES.get(rand.nextInt(ENTITY_TYPES.size()));
            ScriptedUserGenerator generator = new ScriptedAirbenderGenerator();

            if (rand.nextFloat() < 0.5) {
                generator = new ScriptedFirebenderGenerator();
            }

            spawn(type, generator, rand);
        } else if (players.size() < spawns.size()) {
            Iterator<LivingEntity> iterator = spawns.iterator();

            if (iterator.hasNext()) {
                LivingEntity entity = iterator.next();
                entity.remove();
                iterator.remove();
            }
        }

        enforceArena(region);

        if (Bukkit.getPluginManager().getPlugin("ProjectKorra") != null) {
            ProjectKorraHook.fixFlight(players);
        }
    }

    // Destroy any scripted mob that leaves the training arena or dies.
    private void enforceArena(ProtectedRegion region) {
        for (Iterator<LivingEntity> iterator = spawns.iterator(); iterator.hasNext();) {
            LivingEntity entity = iterator.next();
            Location p = entity.getLocation();

            if (!entity.isValid() || !region.contains(p.getBlockX(), p.getBlockY(), p.getBlockZ())) {
                entity.remove();
                iterator.remove();

                this.nextAllowedSpawnTime = System.currentTimeMillis() + this.spawnDelay;
            }
        }

        com.sk89q.worldedit.BlockVector rmin = region.getMinimumPoint();
        com.sk89q.worldedit.BlockVector rmax = region.getMaximumPoint();

        BlockVector min = new BlockVector(rmin.getX(), rmin.getY(), rmin.getZ());
        BlockVector max = new BlockVector(rmax.getX(), rmax.getY(), rmax.getZ());

        double halfDiffX = Math.abs(max.getX() - min.getX()) / 2.0;
        double halfDiffY = Math.abs(max.getY() - min.getY()) / 2.0;
        double halfDiffZ = Math.abs(max.getZ() - min.getZ()) / 2.0;

        Location mid = new Location(world, min.getX() + halfDiffX, min.getY() + halfDiffY, min.getZ() + halfDiffZ);

        for (Entity entity : world.getNearbyEntities(mid, halfDiffX, halfDiffY, halfDiffZ)) {
            if (!(entity instanceof LivingEntity)) continue;
            if (entity instanceof Player) continue;
            if (entity instanceof ArmorStand) continue;

            if (plugin.getUserService().get(entity) == null) {
                entity.remove();
            }
        }
    }

    private List<Player> getPlayers(ProtectedRegion region) {
        if (region == null) {
            return Collections.emptyList();
        }

        return Bukkit.getOnlinePlayers().stream()
                .filter((player) -> {
                    Vector p = player.getLocation().toVector();

                    GameMode gm = player.getGameMode();

                    boolean isInSurvival = gm == GameMode.SURVIVAL || ProjectKorraHook.hasAbility(player, "Phase");

                    return !player.isDead() && isInSurvival && region.contains(p.getBlockX(), p.getBlockY(), p.getBlockZ());
                }).collect(Collectors.toList());
    }

    private ScriptedUser spawn(EntityType type, ScriptedUserGenerator generator, Random rand) {
        RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        if (manager == null) {
            return null;
        }

        ProtectedRegion region = manager.getRegion(regionName);

        if (region == null) {
            return null;
        }

        com.sk89q.worldedit.BlockVector rmin = region.getMinimumPoint();
        com.sk89q.worldedit.BlockVector rmax = region.getMaximumPoint();

        BlockVector min = new BlockVector(rmin.getX(), rmin.getY(), rmin.getZ());
        BlockVector max = new BlockVector(rmax.getX(), rmax.getY(), rmax.getZ());

        Location location = SpawnUtil.getSpawnLocation(world, min, max, rand);

        if (location == null) {
            return null;
        }

        Entity e = world.spawnEntity(location, type);
        if (!(e instanceof LivingEntity)) {
            e.remove();
            return null;
        }

        LivingEntity entity = (LivingEntity)e;
        spawns.add(entity);

        if (type == EntityType.VILLAGER) {
            // Drop villagers down near player sprint speed.
            entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)
                    .addModifier(new AttributeModifier("generic.movementSpeed", -0.2, AttributeModifier.Operation.ADD_SCALAR));
        }

        return plugin.getUserService().create(entity, generator);
    }
}
