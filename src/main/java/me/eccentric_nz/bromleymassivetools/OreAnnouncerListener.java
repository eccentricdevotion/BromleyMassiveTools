package me.eccentric_nz.bromleymassivetools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class OreAnnouncerListener implements Listener {

    private final BromleyMassiveTools plugin;

    public OreAnnouncerListener(BromleyMassiveTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onOreBreak(BlockBreakEvent event) {
        if (isOre(event.getBlock().getType())) {
            Location loc = event.getBlock().getLocation();
            if (!plugin.getOreCounter().isAnnounceable(loc)) {
                plugin.getOreCounter().removeAnnouncedOrPlacedBlock(loc);
                return;
            }
            Player player = event.getPlayer();
            Material mat = event.getBlock().getType();
            int blockTotal = 0;
            if (blockTotal == 0) {
                blockTotal = plugin.getOreCounter().getTotalBlocks(event.getBlock());
            }
            plugin.getOreBroadcast().handleBroadcast(mat, blockTotal, player);
        }
    }

    private boolean isOre(Material material) {
        switch (material) {
            case COAL_ORE,
                    COPPER_ORE,
                    DEEPSLATE_COAL_ORE,
                    DEEPSLATE_COPPER_ORE,
                    DEEPSLATE_DIAMOND_ORE,
                    DEEPSLATE_EMERALD_ORE,
                    DEEPSLATE_GOLD_ORE,
                    DEEPSLATE_IRON_ORE,
                    DEEPSLATE_LAPIS_ORE,
                    DEEPSLATE_REDSTONE_ORE,
                    DIAMOND_ORE,
                    EMERALD_ORE,
                    GOLD_ORE,
                    IRON_ORE,
                    LAPIS_ORE,
                    NETHER_GOLD_ORE,
                    NETHER_QUARTZ_ORE,
                    REDSTONE_ORE -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }
}
