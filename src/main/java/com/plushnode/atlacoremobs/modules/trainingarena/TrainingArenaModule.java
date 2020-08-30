package com.plushnode.atlacoremobs.modules.trainingarena;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.util.Task;
import com.plushnode.atlacoremobs.AtlaCoreMobsPlugin;
import com.plushnode.atlacoremobs.GaussianAimPolicy;
import com.plushnode.atlacoremobs.ScriptedUser;
import com.plushnode.atlacoremobs.compatibility.projectkorra.ProjectKorraHook;
import com.plushnode.atlacoremobs.generator.ScriptedAirbenderGenerator;
import com.plushnode.atlacoremobs.generator.ScriptedFirebenderGenerator;
import com.plushnode.atlacoremobs.generator.ScriptedUserGenerator;
import com.plushnode.atlacoremobs.modules.trainingarena.listeners.TrainingArenaListener;
import com.plushnode.atlacoremobs.util.SpawnUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
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
    private String worldName;
    private Set<LivingEntity> spawns = new HashSet<>();
    private long nextUpdateTime;
    private long updateDelay;
    private long nextAllowedSpawnTime;
    private long spawnDelay;
    private boolean hasPK;
    private double spawnRadius;
    private double ySpawnMin;
    private double ySpawnMax;
    private KillTracker killTracker;
    private boolean predictiveAiming;

    public TrainingArenaModule(AtlaCoreMobsPlugin plugin) {
        this.plugin = plugin;
        this.killTracker = new KillTracker();

        boolean trainingArena = plugin.getConfigRoot().getNode("training-arena", "enabled").getBoolean(false);

        if (!trainingArena) {
            return;
        }

        worldName = plugin.getConfigRoot().getNode("training-arena", "region", "world").getString("world");
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            return;
        }

        this.updateTask = Game.plugin.createTaskTimer(this::update, 1, 1);
        this.regionName = plugin.getConfigRoot().getNode("training-arena", "region", "name").getString("ai-training");
        this.updateDelay = plugin.getConfigRoot().getNode("training-arena", "update-delay").getLong(1000);
        this.spawnDelay = plugin.getConfigRoot().getNode("training-arena", "spawn-delay").getLong(5000);
        this.spawnRadius = plugin.getConfigRoot().getNode("training-arena", "spawn-radius").getDouble(20.0);
        this.ySpawnMin = plugin.getConfigRoot().getNode("training-arena", "spawn-y-min").getDouble(50.0);
        this.ySpawnMax = plugin.getConfigRoot().getNode("training-arena", "spawn-y-max").getDouble(90.0);
        this.predictiveAiming = plugin.getConfigRoot().getNode("training-arena", "predictive-aiming").getBoolean(true);

        this.hasPK = Bukkit.getPluginManager().getPlugin("ProjectKorra") != null;

        Bukkit.getPluginManager().registerEvents(new TrainingArenaListener(this), plugin);
    }

    public KillTracker getKillTracker() {
        return killTracker;
    }

    public boolean isSpawn(LivingEntity entity) {
        return spawns.contains(entity);
    }

    public boolean inArena(Location location) {
        ProtectedRegion region = getRegion();
        if (region == null) return false;

        if (!location.getWorld().getName().equals(this.worldName)) return false;
        return region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
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

        World world = Bukkit.getWorld(this.worldName);

        if (world == null) {
            return;
        }

        RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        if (manager == null) {
            return;
        }

        ProtectedRegion region = manager.getRegion(regionName);

        if (region == null) {
            return;
        }

        Random rand = new Random();

        List<Player> players = getPlayers(region, world);

        killTracker.acceptOnly(players);

        if (players.size() > spawns.size() && time >= this.nextAllowedSpawnTime) {
            EntityType type = ENTITY_TYPES.get(rand.nextInt(ENTITY_TYPES.size()));
            ScriptedUserGenerator generator = new ScriptedAirbenderGenerator();

            if (rand.nextFloat() < 0.5) {
                generator = new ScriptedFirebenderGenerator();
            }

            ScriptedUser spawnedUser = spawn(world, type, generator, rand, players);

            if (spawnedUser != null) {
                // Spawn them in with a random aiming difficulty.
                double sd = 0.15 + rand.nextDouble() * 0.85;

                spawnedUser.setAimPolicy(new GaussianAimPolicy(0.0, sd, 20.0, this.predictiveAiming));
            }
        } else if (players.size() < spawns.size()) {
            if (!removeNonCombatSpawn()) {
                removeRandomSpawn();
            }
        }

        enforceArena(world, region);

        if (hasPK) {
            //ProjectKorraHook.fixFlight(players);
        }
    }

    // Tries to remove a spawn that last took damage from a player that is currently out of the arena.
    private boolean removeNonCombatSpawn() {
        for (Iterator<LivingEntity> iter = spawns.iterator(); iter.hasNext();) {
            LivingEntity entity = iter.next();

            EntityDamageEvent event = entity.getLastDamageCause();
            if (event instanceof EntityDamageByEntityEvent) {
                Entity damager = ((EntityDamageByEntityEvent) event).getDamager();

                if (damager instanceof Player) {
                    if (!inArena(damager.getLocation())) {
                        entity.remove();
                        iter.remove();
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void removeRandomSpawn() {
        Iterator<LivingEntity> iterator = spawns.iterator();

        if (iterator.hasNext()) {
            LivingEntity entity = iterator.next();
            entity.remove();
            iterator.remove();
        }
    }

    private ProtectedRegion getRegion() {
        World world = Bukkit.getWorld(this.worldName);

        if (world == null) {
            return null;
        }

        RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        if (manager == null) {
            return null;
        }

        return manager.getRegion(regionName);
    }

    // Destroy any scripted mob that leaves the training arena or dies.
    private void enforceArena(World world, ProtectedRegion region) {
        for (Iterator<LivingEntity> iterator = spawns.iterator(); iterator.hasNext();) {
            LivingEntity entity = iterator.next();
            Location p = entity.getLocation();

            if (!entity.isValid() || !region.contains(p.getBlockX(), p.getBlockY(), p.getBlockZ())) {
                entity.remove();
                iterator.remove();

                this.nextAllowedSpawnTime = System.currentTimeMillis() + this.spawnDelay;
            }
        }

        BlockVector3 rmin = region.getMinimumPoint();
        BlockVector3 rmax = region.getMaximumPoint();

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

    private List<Player> getPlayers(ProtectedRegion region, World world) {
        if (region == null) {
            return Collections.emptyList();
        }

        return Bukkit.getOnlinePlayers().stream()
                .filter((player) -> {
                    Vector p = player.getLocation().toVector();

                    if (!player.getWorld().getName().equals(world.getName())) {
                        return false;
                    }

                    GameMode gm = player.getGameMode();

                    boolean isInSurvival = gm == GameMode.SURVIVAL || (hasPK && ProjectKorraHook.hasAbility(player, "Phase"));

                    return !player.isDead() && isInSurvival && region.contains(p.getBlockX(), p.getBlockY(), p.getBlockZ());
                }).collect(Collectors.toList());
    }

    private ScriptedUser spawn(World world, EntityType type, ScriptedUserGenerator generator, Random rand, List<Player> regionPlayers) {
        RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        if (manager == null) {
            return null;
        }

        ProtectedRegion region = manager.getRegion(regionName);

        if (region == null) {
            return null;
        }

        double maxRadius = spawnRadius;
        double minRadius = 0.0;

        if (regionPlayers.size() == 1) {
            // Don't spawn the mob near players that are just entering.
            minRadius = 20.0;
            maxRadius = 40.0;
        }

        Player target = regionPlayers.get(rand.nextInt(regionPlayers.size()));

        Location rmin = target.getLocation().clone().subtract(maxRadius, 0, maxRadius);
        Location rmax = target.getLocation().clone().add(maxRadius, 0, maxRadius);

        // Clamp the spawnable region to the training region.
        if (rmin.getX() < region.getMinimumPoint().getX()) rmin.setX(region.getMinimumPoint().getX());
        if (rmin.getZ() < region.getMinimumPoint().getZ()) rmin.setZ(region.getMinimumPoint().getZ());
        if (rmin.getX() > region.getMaximumPoint().getX()) rmin.setX(region.getMaximumPoint().getX());
        if (rmin.getZ() > region.getMaximumPoint().getZ()) rmin.setZ(region.getMaximumPoint().getZ());

        if (rmax.getX() < region.getMinimumPoint().getX()) rmax.setX(region.getMinimumPoint().getX());
        if (rmax.getZ() < region.getMinimumPoint().getZ()) rmax.setZ(region.getMinimumPoint().getZ());
        if (rmax.getX() > region.getMaximumPoint().getX()) rmax.setX(region.getMaximumPoint().getX());
        if (rmax.getZ() > region.getMaximumPoint().getZ()) rmax.setZ(region.getMaximumPoint().getZ());

        // Clamp spawnable region to y spawn range.
        // This is to prevent spawns in player spawn areas that are placed within the region.
        if (rmin.getY() < this.ySpawnMin) rmin.setY(ySpawnMin);
        if (rmin.getY() > this.ySpawnMax) rmin.setY(ySpawnMax);

        if (rmax.getY() < this.ySpawnMin) rmax.setY(ySpawnMin);
        if (rmax.getY() > this.ySpawnMax) rmax.setY(ySpawnMax);

        BlockVector min = new BlockVector(rmin.getX(), rmin.getY(), rmin.getZ());
        BlockVector max = new BlockVector(rmax.getX(), rmax.getY(), rmax.getZ());

        Location location = SpawnUtil.getSpawnLocation(world, min, max, rand);

        if (location == null) {
            return null;
        }

        if (location.distanceSquared(target.getLocation()) < minRadius) {
            return null;
        }

        if (!region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
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
