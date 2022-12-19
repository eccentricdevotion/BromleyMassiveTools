package me.eccentric_nz.bromleymassivetools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.HashSet;
import java.util.Set;

public class OreCounter {

    private final BromleyMassiveTools plugin;
    private final HashSet<Location> counted = new HashSet<>();
    private final BlockFace[] horizontalFaces = {BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH,
            BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.DOWN,
            BlockFace.UP};
    private final BlockFace[] upperFaces = {BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH,
            BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.UP};
    private final BlockFace[] LowerFaces = {BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH,
            BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.DOWN};

    public OreCounter(BromleyMassiveTools plugin) {
        this.plugin = plugin;
    }

    public int getTotalBlocks(Block original) {
        HashSet<Location> blocks = new HashSet<>();
        blocks.add(original.getLocation());
        cycleHorizontalFaces(original.getType(), original, blocks, true);
        return blocks.size() >= 500 ? 500 : blocks.size();
    }

    public HashSet<Location> getAllLikeBlockLocations(Block original) {
        HashSet<Location> blocks = new HashSet<>();
        blocks.add(original.getLocation());
        cycleHorizontalFaces(original.getType(), original, blocks, false);
        return blocks;
    }

    private void cycleHorizontalFaces(Material mat, Block original, Set<Location> blocks, boolean counting) {
        if (blocks.size() >= 500) {
            return;
        }
        findLikeBlocks(horizontalFaces, original, mat, blocks, counting);
        if (blocks.size() >= 500) {
            return;
        }
        Block upper = original.getRelative(BlockFace.UP);
        findLikeBlocks(upperFaces, upper, mat, blocks, counting);
        if (blocks.size() >= 500) {
            return;
        }
        Block lower = original.getRelative(BlockFace.DOWN);
        findLikeBlocks(LowerFaces, lower, mat, blocks, counting);
    }

    private void findLikeBlocks(BlockFace[] faces, Block passed, Material originalMaterial, Set<Location> blocks,
                                boolean counting) {
        for (BlockFace y : faces) {
            Block var = passed.getRelative(y);
            if (var.getType() == originalMaterial && !blocks.contains(var.getLocation())
                    && isAnnounceable(var.getLocation())
                    || var.getType() == Material.REDSTONE_ORE && originalMaterial == Material.REDSTONE_ORE
                    && isAnnounceable(var.getLocation()) && !blocks.contains(var.getLocation())) {
                if (counting) {
                    counted.add(var.getLocation());
                }
                blocks.add(var.getLocation());
                if (blocks.size() >= 500) {
                    return;
                }
                cycleHorizontalFaces(originalMaterial, var, blocks, counting);
            }
        }
    }

    public boolean wasCounted(Location loc) {
        return counted.contains(loc);
    }

    public boolean isAnnounceable(Location loc) {
        return !wasCounted(loc);
    }

    public void removeAnnouncedOrPlacedBlock(Location loc) {
        if (counted.contains(loc)) {
            counted.remove(loc);
        }
    }
}

