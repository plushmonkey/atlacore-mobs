package com.plushnode.atlacoremobs.modules.raid;

import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.*;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.fire.FireJet;
import com.plushnode.atlacore.game.ability.fire.sequences.JetBlast;
import com.plushnode.atlacore.game.ability.fire.sequences.JetBlaze;
import com.plushnode.atlacoremobs.AtlaCoreMobsPlugin;
import com.plushnode.atlacoremobs.ScriptedUser;
import com.plushnode.atlacoremobs.compatibility.DisguiseUtil;
import com.plushnode.atlacoremobs.generator.ScriptedFirebenderGenerator;
import org.bukkit.*;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Raid {
    private static final Material[] TRANSPARENT_MATERIALS = { Material.AIR, Material.SAPLING, Material.WATER,
            Material.STATIONARY_WATER, Material.LAVA, Material.STATIONARY_LAVA, Material.POWERED_RAIL,
            Material.DETECTOR_RAIL, Material.WEB, Material.LONG_GRASS, Material.DEAD_BUSH, Material.YELLOW_FLOWER,
            Material.RED_ROSE, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.TORCH, Material.FIRE,
            Material.REDSTONE_WIRE, Material.CROPS, Material.LADDER, Material.RAILS, Material.SIGN_POST, Material.LEVER,
            Material.STONE_PLATE, Material.WOOD_PLATE, Material.REDSTONE_TORCH_OFF, Material.REDSTONE_TORCH_ON,
            Material.STONE_BUTTON, Material.SNOW, Material.SUGAR_CANE_BLOCK, Material.PORTAL, Material.DIODE_BLOCK_OFF,
            Material.DIODE_BLOCK_ON, Material.PUMPKIN_STEM, Material.MELON_STEM, Material.VINE, Material.WATER_LILY,
            Material.NETHER_STALK, Material.ENDER_PORTAL, Material.COCOA, Material.TRIPWIRE_HOOK, Material.TRIPWIRE,
            Material.FLOWER_POT, Material.CARROT, Material.POTATO, Material.WOOD_BUTTON, Material.GOLD_PLATE,
            Material.IRON_PLATE, Material.REDSTONE_COMPARATOR_OFF, Material.REDSTONE_COMPARATOR_ON,
            Material.DAYLIGHT_DETECTOR, Material.CARPET, Material.DOUBLE_PLANT, Material.STANDING_BANNER,
            Material.WALL_BANNER, Material.DAYLIGHT_DETECTOR_INVERTED, Material.END_ROD, Material.CHORUS_PLANT,
            Material.CHORUS_FLOWER, Material.BEETROOT_BLOCK, Material.END_GATEWAY };

    private AtlaCoreMobsPlugin plugin;
    private Town town;
    private Nation nation;
    private World world;
    private int nearbyTownRange;
    private int spawnsPerWave;
    private int spawnDelay;
    private int waveCount;
    private int spawnsRemaining;
    private double spawnHealth;
    private long nextSpawnTime;
    private int endDelay;
    private double killReward;
    private int kills;
    private List<LivingEntity> spawns = new ArrayList<>();
    private List<WorldCoord> spawnChunks = new ArrayList<>();

    public Raid(AtlaCoreMobsPlugin plugin, Town town) {
        this.plugin = plugin;
        this.town = town;
        this.nextSpawnTime = 0;
        this.kills = 0;

        if (town.hasNation()) {
            try {
                this.nation = town.getNation();
            } catch (Exception e) {
                // pass
            }
        }

        this.waveCount = this.spawnsRemaining = plugin.getConfigRoot().getNode("raid", "spawn", "waves").getInt(30);
        this.spawnDelay = plugin.getConfigRoot().getNode("raid", "spawn", "delay").getInt(10000);
        this.spawnsPerWave = plugin.getConfigRoot().getNode("raid", "spawn", "amount-per-wave").getInt(2);
        this.spawnHealth = plugin.getConfigRoot().getNode("raid", "spawn", "health").getDouble(20.0);
        this.nearbyTownRange = plugin.getConfigRoot().getNode("raid", "nearby-town-range").getInt(2);
        this.endDelay = plugin.getConfigRoot().getNode("raid", "end-delay").getInt(60000);
        this.killReward = plugin.getConfigRoot().getNode("raid", "kill-reward").getDouble(150.0);

        String worldName = town.getWorld().getName();
        this.world = Bukkit.getWorld(worldName);

        if (this.world != null) {
            getSpawnChunks();
        } else {
            plugin.getLogger().warning("Raid town world doesn't exist. World name: " + worldName);
        }
    }

    public Town getTown() {
        return town;
    }

    public boolean isRaidEntity(LivingEntity entity) {
        return spawns.contains(entity);
    }

    private void getSpawnChunks() {
        TownBlock homeBlock = null;
        try {
            homeBlock = town.getHomeBlock();
        } catch (TownyException e) {
            e.printStackTrace();
            return;
        }

        int worldX = homeBlock.getX();
        int worldZ = homeBlock.getZ();

        List<TownBlock> townBlocks = town.getTownBlocks();
        for (TownBlock block : townBlocks) {
            if (block.getWorld() != town.getWorld()) continue;

            double xdiff = block.getX() - worldX;
            double zdiff = block.getZ() - worldZ;

            // Only find town blocks near home block
            if (Math.sqrt(xdiff * xdiff + zdiff * zdiff) > 64) {
                continue;
            }

            // Search around this town block to see if there's any chunks that aren't part of this town.
            for (int x = -nearbyTownRange; x < nearbyTownRange; ++x) {
                for (int z = -nearbyTownRange; z < nearbyTownRange; ++z) {
                    TownBlock checkBlock = new TownBlock(block.getX() + x, block.getZ() + z, town.getWorld());

                    if (!townBlocks.contains(checkBlock)) {
                        WorldCoord coord = new WorldCoord(town.getWorld().getName(), block.getX() + x, block.getZ() + z);
                        if (!spawnChunks.contains(coord)) {
                            // Create a spawn chunk here because it's an unclaimed plot that's near the target town.
                            spawnChunks.add(coord);
                        }
                    }
                }
            }
        }
    }

    public void stop() {
        this.spawnsRemaining = 0;
        onFinish();
    }

    public boolean update() {
        validatePositions();

        if (this.world == null || isFinished()) {
            onFinish();
            return false;
        }

        if (this.spawnsRemaining <= 0) return true;

        long time = System.currentTimeMillis();

        if (time >= this.nextSpawnTime) {
            List<Location> locations = getRandomSpawns();

            if (locations.isEmpty()) {
                return true;
            }

            for (Location location : locations) {
                LivingEntity entity = (LivingEntity) this.world.spawnEntity(location, EntityType.ZOMBIE);

                if (spawnHealth > 0) {
                    entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(spawnHealth);
                    entity.setHealth(spawnHealth);
                }

                entity.setRemoveWhenFarAway(false);
                DisguiseUtil.disguise(entity, ChatColor.DARK_RED + "Fire Raider", "FireBender");
                plugin.getUserService().create(entity, new ScriptedFirebenderGenerator());
                spawns.add(entity);
            }

            --spawnsRemaining;
            this.nextSpawnTime = time + this.spawnDelay;
        }

        return true;
    }

    // Push the raiders out of towny chunks.
    private void validatePositions() {
        for (LivingEntity entity : this.spawns) {
            if (!entity.isValid()) continue;

            Location location = entity.getLocation();

            List<TownBlock> blocks = town.getTownBlocks();

            TownBlock block = TownyRaidGame.getTownBlock(location, town.getWorld());

            // Do a custom lookup instead of TownyUniverse because it throws exceptions for non-town blocks,
            // which is a huge performance hit.
            if (blocks.contains(block)) {
                Location center = new Location(location.getWorld(), block.getX() * 16.0 + 8.0, location.getY(), block.getZ() * 16.0 + 8.0);
                Vector direction = location.clone().subtract(center).toVector();

                ScriptedUser user = plugin.getUserService().get(entity);
                if (user != null) {
                    Game.getAbilityInstanceManager().destroyInstanceType(user, FireJet.class);
                    Game.getAbilityInstanceManager().destroyInstanceType(user, JetBlast.class);
                    Game.getAbilityInstanceManager().destroyInstanceType(user, JetBlaze.class);
                }

                if (direction.lengthSquared() > 0) {
                    direction = direction.normalize();
                    entity.setVelocity(direction);
                }
            }
        }
    }

    private List<Location> getRandomSpawns() {
        List<Location> spawns = new ArrayList<>();

        Random rand = new Random();

        WorldCoord coord = spawnChunks.get(rand.nextInt(spawnChunks.size()));

        for (int i = 0; i < this.spawnsPerWave; ++i) {
            int x = coord.getX() * 16 + rand.nextInt(16);
            int z = coord.getZ() * 16 + rand.nextInt(16);

            int y = 63;

            int highestY = world.getHighestBlockYAt(x, z);

            if (highestY <= 0) {
                return spawns;
            }

            if (y > highestY) {
                spawns.add(new Location(world, x, highestY, z));
                continue;
            }

            for (; y <= highestY; ++y) {
                Location location = new Location(world, x, y, z);

                if (isSpawnableLocation(location)) {
                    spawns.add(location);
                    break;
                }
            }
        }

        return spawns;
    }

    private boolean isSpawnableLocation(Location location) {
        Block below = location.getBlock().getRelative(BlockFace.DOWN);
        Block current = location.getBlock();
        Block above = location.getBlock().getRelative(BlockFace.UP);

        return isSolid(below) && !isSolid(current) && !isSolid(above);
    }

    private boolean isSolid(Block block) {
        return !Arrays.asList(TRANSPARENT_MATERIALS).contains(block.getType());
    }

    private boolean isFinished() {
        if (this.spawnsRemaining > 0) return false;

        // Calculate the end time based on the last spawned time
        long endTime = this.nextSpawnTime - this.spawnDelay + this.endDelay;

        return System.currentTimeMillis() >= endTime;
    }

    private void onFinish() {
        for (LivingEntity entity : spawns) {
            if (entity.isValid()) {
                entity.remove();
            }
        }

        int totalSpawns = this.waveCount * this.spawnsPerWave;
        double killPercent = this.kills / (double)totalSpawns;

        String raidEndMessage;
        if (killPercent > 0.5) {
            raidEndMessage = ChatColor.DARK_AQUA + "The fire nation raid on " + ChatColor.GOLD + town.getName() + ChatColor.DARK_AQUA + " ends as they limp away.";
        } else {
            raidEndMessage = ChatColor.DARK_AQUA + "The fire nation raid on " + ChatColor.GOLD + town.getName() + ChatColor.DARK_AQUA + " has ended.";
        }

        plugin.getLogger().info("Raid on " + town.getName() + " ended with " + kills + " kills.");

        if (this.nation != null) {
            // Send raid end message to entire nation.
            for (Player player : TownyUniverse.getOnlinePlayers(nation)) {
                player.sendMessage(raidEndMessage);
            }
        } else {
            // Send raid end message to town members only.
            for (Player player : TownyUniverse.getOnlinePlayers(town)) {
                player.sendMessage(raidEndMessage);
            }
        }
    }

    public void onKill(Player killer, LivingEntity killed) {
        ++this.kills;

        if (nation != null) {
            // Don't reward non-nation players.
            if (!TownyUniverse.getOnlinePlayers(nation).contains(killer)) {
                return;
            }
        } else {
            // Don't reward non-town players.
            if (!TownyUniverse.getOnlinePlayers(town).contains(killer)) {
                return;
            }
        }

        try {
            town.collect(this.killReward);

            String amount = ChatColor.GREEN + formatCurrency(this.killReward);
            String msg = ChatColor.GREEN + "Earned " + ChatColor.GOLD + town.getName() + " " + amount + ChatColor.GREEN + " for killing " + killed.getCustomName() + ChatColor.GREEN + ".";

            killer.sendMessage(msg);
        } catch (EconomyException e) {
            // pass
        }
    }

    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        String result = formatter.format(amount);
        return result.replace(".00", "");
    }
}
