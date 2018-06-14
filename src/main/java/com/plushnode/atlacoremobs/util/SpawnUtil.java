package com.plushnode.atlacoremobs.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;

import java.util.Arrays;
import java.util.Random;

public final class SpawnUtil {
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

    private SpawnUtil() {

    }

    public static Location getSpawnLocation(World world, BlockVector min, BlockVector max, Random rand) {
        double xDiff = Math.abs(max.getX() - min.getX());
        double zDiff = Math.abs(max.getZ() - min.getZ());

        int x = (int)Math.floor(xDiff * rand.nextDouble() + min.getX());
        int z = (int)Math.floor(zDiff * rand.nextDouble() + min.getZ());
        int y = 63;

        int highestY = world.getHighestBlockYAt(x, z);

        if (highestY <= 0) {
            return null;
        }

        if (y > highestY) {
            return new Location(world, x, highestY, z);
        }

        for (; y <= highestY; ++y) {
            Location location = new Location(world, x, y, z);

            if (isSpawnableLocation(location)) {
                return location;
            }
        }

        return null;
    }

    public static boolean isSpawnableLocation(Location location) {
        Block below = location.getBlock().getRelative(BlockFace.DOWN);
        Block current = location.getBlock();
        Block above = location.getBlock().getRelative(BlockFace.UP);

        return isSolid(below) && !isSolid(current) && !isSolid(above);
    }

    public static boolean isSolid(Block block) {
        return !Arrays.asList(TRANSPARENT_MATERIALS).contains(block.getType());
    }
}
