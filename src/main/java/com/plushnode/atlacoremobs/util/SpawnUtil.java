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
    private static final Material[] TRANSPARENT_MATERIALS = { Material.AIR, Material.OAK_SAPLING, Material.SPRUCE_SAPLING, Material.BIRCH_SAPLING,
            Material.JUNGLE_SAPLING, Material.ACACIA_SAPLING, Material.DARK_OAK_SAPLING, Material.WATER,
            Material.LAVA, Material.COBWEB, Material.TALL_GRASS, Material.GRASS, Material.FERN, Material.DEAD_BUSH,
            Material.DANDELION, Material.DANDELION_YELLOW, Material.POPPY, Material.BLUE_ORCHID, Material.ALLIUM,
            Material.AZURE_BLUET, Material.RED_TULIP, Material.ORANGE_TULIP, Material.WHITE_TULIP, Material.PINK_TULIP,
            Material.OXEYE_DAISY, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.TORCH, Material.FIRE,
            Material.WHEAT, Material.SNOW, Material.SUGAR_CANE, Material.VINE, Material.SUNFLOWER, Material.LILAC,
            Material.LARGE_FERN, Material.ROSE_BUSH, Material.PEONY};

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
