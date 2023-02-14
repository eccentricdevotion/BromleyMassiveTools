package me.eccentric_nz.bromleymassivetools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Furnace;

public class SolarFurnaceRunnable implements Runnable {

    private final BromleyMassiveTools plugin;

    public SolarFurnaceRunnable(BromleyMassiveTools plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Location location : plugin.getFurnaces()) {
            try {
                long time = location.getWorld().getTime();
                if (time < 12000) {
                    Block block = location.getBlock();
                    if (isSolarFurnace(block)) {
                        Furnace furnace = (Furnace) block.getState();
                        if (furnace.getInventory().getSmelting() != null) {
                            furnace.setBurnTime((short) 200);
                            furnace.update(true);
                        }
                    }
                }
            } catch (Exception e) {
                plugin.debug("Exception running: " + e.getMessage());
            }
        }
    }

    private boolean isSolarFurnace(Block block) {
        // is it a furnace?
        if (block.getType() != Material.FURNACE) {
            return false;
        }
        // check for daylight detector
        if (block.getRelative(BlockFace.UP).getType() != Material.DAYLIGHT_DETECTOR) {
            return false;
        }
        return true;
    }
}
