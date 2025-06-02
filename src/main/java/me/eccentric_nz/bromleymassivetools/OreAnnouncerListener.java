package me.eccentric_nz.bromleymassivetools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashSet;
import java.util.Set;

public class OreAnnouncerListener implements Listener {

    private final BromleyMassiveTools plugin;
    private final Set<Material> ORES = new HashSet<>();

    public OreAnnouncerListener(BromleyMassiveTools plugin) {
        this.plugin = plugin;
        this.ORES.addAll(Tag.COAL_ORES.getValues());
        this.ORES.addAll(Tag.COPPER_ORES.getValues());
        this.ORES.addAll(Tag.DIAMOND_ORES.getValues());
        this.ORES.addAll(Tag.EMERALD_ORES.getValues());
        this.ORES.addAll(Tag.GOLD_ORES.getValues());
        this.ORES.addAll(Tag.IRON_ORES.getValues());
        this.ORES.addAll(Tag.LAPIS_ORES.getValues());
        this.ORES.addAll(Tag.REDSTONE_ORES.getValues());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onOreBreak(BlockBreakEvent event) {
        if (ORES.contains(event.getBlock().getType())) {
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
}
