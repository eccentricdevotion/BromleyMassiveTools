package me.eccentric_nz.bromleymassivetools;

import com.google.common.primitives.Ints;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.*;

public final class LocationUtility {

    public static final int RADIUS = 3;
    public static final Vector3D[] VOLUME;
    // Types checked by isBlockDamaging
    private static final List<Material> DAMAGING_TYPES = Arrays.asList(Material.CACTUS, Material.CAMPFIRE, Material.FIRE, Material.MAGMA_BLOCK, Material.SOUL_CAMPFIRE, Material.SOUL_FIRE, Material.SWEET_BERRY_BUSH, Material.WITHER_ROSE);
    // The player can stand inside these materials
    private static final Set<Material> HOLLOW_MATERIALS = EnumSet.noneOf(Material.class);

    static {
        // Materials from Material.isTransparent()
        Registry.MATERIAL.stream().forEach(material -> {
            if (isTransparent(material)) {
                HOLLOW_MATERIALS.add(material);
            }
        });
        // Barrier is transparent, but solid
        HOLLOW_MATERIALS.remove(Material.BARRIER);
        // Light blocks can be passed through and are not considered transparent for some reason
        HOLLOW_MATERIALS.add(Material.LIGHT);
    }

    static {
        final List<Vector3D> pos = new ArrayList<>();
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int y = -RADIUS; y <= RADIUS; y++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    pos.add(new Vector3D(x, y, z));
                }
            }
        }
        pos.sort(Comparator.comparingInt(a -> a.x * a.x + a.y * a.y + a.z * a.z));
        VOLUME = pos.toArray(new Vector3D[0]);
    }

    public static boolean isBlockAboveAir(final World world, final int x, final int y, final int z) {
        return y > world.getMaxHeight() || HOLLOW_MATERIALS.contains(world.getBlockAt(x, y - 1, z).getType());
    }

    public static boolean isBlockOutsideWorldBorder(final World world, final int x, final int z) {
        final Location center = world.getWorldBorder().getCenter();
        final int radius = (int) world.getWorldBorder().getSize() / 2;
        final int x1 = center.getBlockX() - radius, x2 = center.getBlockX() + radius;
        final int z1 = center.getBlockZ() - radius, z2 = center.getBlockZ() + radius;
        return x < x1 || x > x2 || z < z1 || z > z2;
    }

    public static int getXInsideWorldBorder(final World world, final int x) {
        final Location center = world.getWorldBorder().getCenter();
        final int radius = (int) world.getWorldBorder().getSize() / 2;
        final int x1 = center.getBlockX() - radius, x2 = center.getBlockX() + radius;
        if (x < x1) {
            return x1;
        } else if (x > x2) {
            return x2;
        }
        return x;
    }

    public static int getZInsideWorldBorder(final World world, final int z) {
        final Location center = world.getWorldBorder().getCenter();
        final int radius = (int) world.getWorldBorder().getSize() / 2;
        final int z1 = center.getBlockZ() - radius, z2 = center.getBlockZ() + radius;
        if (z < z1) {
            return z1;
        } else if (z > z2) {
            return z2;
        }
        return z;
    }

    public static boolean isBlockUnsafeForUser(final Player user, final World world, final int x, final int y, final int z) {
        if (user.isOnline() && world.equals(user.getWorld()) && (user.getGameMode() == GameMode.CREATIVE || user.getGameMode() == GameMode.SPECTATOR) && user.getAllowFlight()) {
            return false;
        }

        if (isBlockDamaging(world, x, y, z)) {
            return true;
        }
        if (isBlockAboveAir(world, x, y, z)) {
            return true;
        }
        return isBlockOutsideWorldBorder(world, x, z);
    }

    public static boolean isBlockUnsafe(final World world, final int x, final int y, final int z) {
        return isBlockDamaging(world, x, y, z) || isBlockAboveAir(world, x, y, z);
    }

    public static boolean isBlockDamaging(final World world, final int x, final int y, final int z) {
        final Material block = world.getBlockAt(x, y, z).getType();
        final Material below = world.getBlockAt(x, y - 1, z).getType();
        final Material above = world.getBlockAt(x, y + 1, z).getType();

        if (DAMAGING_TYPES.contains(below) || below.equals(Material.LAVA) || Tag.BEDS.isTagged(below)) {
            return true;
        }

        if (block == Material.NETHER_PORTAL || block == Material.END_PORTAL) {
            return true;
        }

        return !HOLLOW_MATERIALS.contains(block) || !HOLLOW_MATERIALS.contains(above);
    }

    // Not needed if using getSafeDestination(loc)
    public static Location getRoundedDestination(final Location loc) {
        final World world = loc.getWorld();
        final int x = loc.getBlockX();
        final int y = (int) Math.round(loc.getY());
        final int z = loc.getBlockZ();
        return new Location(world, x + 0.5, y, z + 0.5, loc.getYaw(), loc.getPitch());
    }

    public static Location getSafeDestination(final Player user, final Location loc) throws Exception {
        if (user.isOnline() && (user.getGameMode() == GameMode.CREATIVE || user.getGameMode() == GameMode.SPECTATOR)) {
            if (shouldFly(loc) && user.getAllowFlight()) {
                user.setFlying(true);
            }
            return getRoundedDestination(loc);
        }
        return getSafeDestination(loc);
    }

    public static Location getSafeDestination(final Location loc) throws Exception {
        if (loc == null || loc.getWorld() == null) {
            throw new Exception("destinationNotSet");
        }
        final World world = loc.getWorld();
        final int worldMinY = world.getMinHeight();
        final int worldLogicalY = world.getLogicalHeight();
        final int worldMaxY = loc.getBlockY() < worldLogicalY ? worldLogicalY : world.getMaxHeight();
        int x = loc.getBlockX();
        int y = (int) Math.round(loc.getY());
        int z = loc.getBlockZ();
        if (isBlockOutsideWorldBorder(world, x, z)) {
            x = getXInsideWorldBorder(world, x);
            z = getZInsideWorldBorder(world, z);
        }
        final int origX = x;
        final int origY = y;
        final int origZ = z;
        while (isBlockAboveAir(world, x, y, z)) {
            y -= 1;
            if (y < 0) {
                y = origY;
                break;
            }
        }
        if (isBlockUnsafe(world, x, y, z)) {
            x = Math.round(loc.getX()) == origX ? x - 1 : x + 1;
            z = Math.round(loc.getZ()) == origZ ? z - 1 : z + 1;
        }
        int i = 0;
        while (isBlockUnsafe(world, x, y, z)) {
            i++;
            if (i >= VOLUME.length) {
                x = origX;
                y = Ints.constrainToRange(origY + RADIUS, worldMinY, worldMaxY);
                z = origZ;
                break;
            }
            x = origX + VOLUME[i].x;
            y = Ints.constrainToRange(origY + VOLUME[i].y, worldMinY, worldMaxY);
            z = origZ + VOLUME[i].z;
        }
        while (isBlockUnsafe(world, x, y, z)) {
            y += 1;
            if (y >= worldMaxY) {
                x += 1;
                break;
            }
        }
        while (isBlockUnsafe(world, x, y, z)) {
            y -= 1;
            if (y <= worldMinY + 1) {
                x += 1;
                // Allow spawning at the top of the world, but not above the nether roof
                y = Math.min(world.getHighestBlockYAt(x, z) + 1, worldMaxY);
                if (x - 48 > loc.getBlockX()) {
                    throw new Exception("holeInFloor");
                }
            }
        }
        return new Location(world, x + 0.5, y, z + 0.5, loc.getYaw(), loc.getPitch());
    }

    public static boolean shouldFly(final Location loc) {
        final World world = loc.getWorld();
        final int x = loc.getBlockX();
        int y = (int) Math.round(loc.getY());
        final int z = loc.getBlockZ();
        int count = 0;
        while (LocationUtility.isBlockUnsafe(world, x, y, z) && y >= world.getMinHeight()) {
            y--;
            count++;
            if (count > 2) {
                return true;
            }
        }
        return y < 0;
    }

    public static class Vector3D {
        public final int x;
        public final int y;
        public final int z;

        Vector3D(final int x, final int y, final int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private static boolean isTransparent(Material material) {
        return switch (material) {
            case ACACIA_BUTTON, ACACIA_SAPLING, ACTIVATOR_RAIL, AIR, ALLIUM, ATTACHED_MELON_STEM, ATTACHED_PUMPKIN_STEM,
            AZURE_BLUET, BAMBOO_BUTTON, BAMBOO_SAPLING, BAMBOO, BARRIER, BEETROOTS, BIG_DRIPLEAF_STEM, BIG_DRIPLEAF,
            BIRCH_BUTTON, BIRCH_SAPLING, BLACK_CANDLE, BLACK_CARPET, BLUE_CANDLE, BLUE_CARPET, BLUE_ORCHID,
            BROWN_CANDLE, BROWN_CARPET, BROWN_MUSHROOM, CACTUS, CARROTS, CAVE_AIR, CHERRY_BUTTON, CHERRY_SAPLING,
            CHORUS_FLOWER, CHORUS_PLANT, CLOSED_EYEBLOSSOM, COCOA, COMPARATOR, CORNFLOWER, CREEPER_HEAD,
            CREEPER_WALL_HEAD, CRIMSON_BUTTON, CRIMSON_FUNGUS, CRIMSON_ROOTS, CYAN_CANDLE, CYAN_CARPET, DANDELION,
            DARK_OAK_BUTTON, DARK_OAK_SAPLING, DEAD_BUSH, DETECTOR_RAIL, DRAGON_HEAD, DRAGON_WALL_HEAD, END_GATEWAY,
            END_PORTAL, END_ROD, FERN, FIRE, FLOWER_POT, GLOW_LICHEN, GOLDEN_DANDELION, GRAY_CANDLE, GRAY_CARPET,
            GREEN_CANDLE, GREEN_CARPET, JUNGLE_BUTTON, JUNGLE_SAPLING, LADDER, LARGE_FERN, LEVER, LIGHT_BLUE_CANDLE,
            LIGHT_BLUE_CARPET, LIGHT_GRAY_CANDLE, LIGHT_GRAY_CARPET, LILAC, LILY_OF_THE_VALLEY, LILY_PAD, LIME_CANDLE,
            LIME_CARPET, MAGENTA_CANDLE, MAGENTA_CARPET, MANGROVE_BUTTON, MANGROVE_PROPAGULE, MELON_STEM, MOSS_CARPET,
            NETHER_PORTAL, NETHER_WART, OAK_BUTTON, OAK_SAPLING, OPEN_EYEBLOSSOM, ORANGE_CANDLE, ORANGE_CARPET,
            ORANGE_TULIP, OXEYE_DAISY, PALE_MOSS_CARPET, PALE_OAK_BUTTON, PALE_OAK_SAPLING, PEONY, PIGLIN_HEAD,
            PIGLIN_WALL_HEAD, PINK_CANDLE, PINK_CARPET, PINK_TULIP, PLAYER_HEAD, PLAYER_WALL_HEAD,
            POLISHED_BLACKSTONE_BUTTON, POPLAR_BUTTON, POPLAR_SAPLING, POPPY, POTATOES, POTTED_ACACIA_SAPLING,
            POTTED_ALLIUM, POTTED_AZALEA_BUSH, POTTED_AZURE_BLUET, POTTED_BAMBOO, POTTED_BIRCH_SAPLING,
            POTTED_BLUE_ORCHID, POTTED_BROWN_MUSHROOM, POTTED_CACTUS, POTTED_CHERRY_SAPLING, POTTED_CLOSED_EYEBLOSSOM,
            POTTED_CORNFLOWER, POTTED_CRIMSON_FUNGUS, POTTED_CRIMSON_ROOTS, POTTED_DANDELION, POTTED_DARK_OAK_SAPLING,
            POTTED_DEAD_BUSH, POTTED_FERN, POTTED_FLOWERING_AZALEA_BUSH, POTTED_GOLDEN_DANDELION, POTTED_JUNGLE_SAPLING,
            POTTED_LILY_OF_THE_VALLEY, POTTED_MANGROVE_PROPAGULE, POTTED_OAK_SAPLING, POTTED_OPEN_EYEBLOSSOM,
            POTTED_ORANGE_TULIP, POTTED_OXEYE_DAISY, POTTED_PALE_OAK_SAPLING, POTTED_PINK_TULIP, POTTED_POPLAR_SAPLING,
            POTTED_POPPY, POTTED_RED_MUSHROOM, POTTED_RED_TULIP, POTTED_SPRUCE_SAPLING, POTTED_TORCHFLOWER,
            POTTED_WARPED_FUNGUS, POTTED_WARPED_ROOTS, POTTED_WHITE_TULIP, POTTED_WITHER_ROSE, POWERED_RAIL,
            PUMPKIN_STEM, PURPLE_CANDLE, PURPLE_CARPET, RAIL, RED_CANDLE, RED_CARPET, RED_MUSHROOM, RED_TULIP,
            REDSTONE_TORCH, REDSTONE_WALL_TORCH, REDSTONE_WIRE, REPEATER, ROSE_BUSH, SHORT_GRASS, SKELETON_SKULL,
            SKELETON_WALL_SKULL, SMALL_DRIPLEAF, SNOW, SPRUCE_BUTTON, SPRUCE_SAPLING, STONE_BUTTON, STRUCTURE_VOID,
            SUGAR_CANE, SUNFLOWER, TALL_GRASS, TORCH, TORCHFLOWER, TRIPWIRE_HOOK, TRIPWIRE, VINE, VOID_AIR, WALL_TORCH,
            WARPED_BUTTON, WARPED_FUNGUS, WARPED_ROOTS, WHEAT, WHITE_CANDLE, WHITE_CARPET, WHITE_TULIP, WITHER_ROSE,
            WITHER_SKELETON_SKULL, WITHER_SKELETON_WALL_SKULL, YELLOW_CANDLE, YELLOW_CARPET, ZOMBIE_HEAD,
            ZOMBIE_WALL_HEAD -> true;
            default -> false;
        };
    }
}
