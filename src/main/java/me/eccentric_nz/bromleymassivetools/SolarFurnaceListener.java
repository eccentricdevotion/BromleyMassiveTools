package me.eccentric_nz.bromleymassivetools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.FurnaceInventory;

public class SolarFurnaceListener implements Listener {

    private final BromleyMassiveTools plugin;

    public SolarFurnaceListener(BromleyMassiveTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block detector = event.getBlockPlaced();
        if (detector.getType() != Material.DAYLIGHT_DETECTOR) {
            return;
        }
        Block furnace = event.getBlockAgainst();
        if (furnace.getType() != Material.FURNACE) {
            return;
        }
        if (!furnace.getRelative(BlockFace.UP).getLocation().equals(detector.getLocation())) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.hasPermission("solarfurnace.place")) {
            return;
        }
        Location location = furnace.getLocation();
        new SolarFurnaceInsert(plugin).addFurnace(detector.getLocation().toString(), location.toString());
        plugin.getFurnaces().add(furnace.getLocation());
        Furnace cooker = (Furnace) furnace.getState();
        if (detector.getLightFromSky() > 14) {
            cooker.setBurnTime((short) 200);
        }
        player.sendMessage(plugin.getPluginName() + "Furnace activated!");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.DAYLIGHT_DETECTOR && block.getType() != Material.FURNACE) {
            return;
        }
        Location location = block.getLocation();
        if (plugin.getDetectors().contains(location)) {
            plugin.getDetectors().remove(location);
            plugin.getFurnaces().remove(block.getRelative(BlockFace.DOWN).getLocation());
            new SolarFurnaceDelete(plugin).removeByLocation(location.toString(), "detector_location");
        }
        if (plugin.getFurnaces().contains(location)) {
            plugin.getFurnaces().remove(location);
            plugin.getDetectors().remove(block.getRelative(BlockFace.UP).getLocation());
            new SolarFurnaceDelete(plugin).removeByLocation(location.toString(), "furnace_location");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFurnaceBurn(FurnaceBurnEvent event) {
        Block block = event.getBlock();
        if (isSolarFurnace(block)) {
            event.setBurnTime(200);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof FurnaceInventory)) {
            return;
        }
        Furnace furnace = (Furnace) event.getInventory().getHolder();
        // Setting cookTime when the furnace is empty but already burning
        if (isSolarFurnace(furnace.getBlock())
                && (event.getSlot() == 0 || event.getSlot() == 1) // Click in one of the two slots
                && event.getCursor().getType() != Material.AIR    // With an item
        ) {
            furnace.setBurnTime((short) 200);
            furnace.update(true);
        }
    }

    private boolean isSolarFurnace(Block block) {
        // is it a furnace?
        if (block.getType() != Material.FURNACE) {
            return false;
        }
        // check for daylight detector
        Block detector = block.getRelative(BlockFace.UP);
        if (detector.getType() != Material.DAYLIGHT_DETECTOR) {
            return false;
        }
        int light = detector.getLightFromSky();
        if (light < 15) {
            return false;
        }
        return plugin.getFurnaces().contains(block.getLocation());
    }
}
