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
        for (final Material mat : Material.values()) {
            if (mat.isTransparent()) {
                HOLLOW_MATERIALS.add(mat);
            }
        }
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
}
