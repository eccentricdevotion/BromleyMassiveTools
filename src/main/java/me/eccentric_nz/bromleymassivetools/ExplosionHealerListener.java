package me.eccentric_nz.bromleymassivetools;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class ExplosionHealerListener implements Listener {

    private final BromleyMassiveTools plugin;
    private final Random random = new Random();
    private boolean isRestoring = false;

    ExplosionHealerListener(BromleyMassiveTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        if ((event.getEntityType() == EntityType.TNT || event.getEntityType() == EntityType.TNT_MINECART) && !plugin.getConfig().getBoolean("restore.tnt")) {
            return;
        }
        if (entity instanceof Creeper && !plugin.getConfig().getBoolean("explosions.restore.creeper")) {
            return;
        }
        if ((entity instanceof Wither || entity instanceof WitherSkull) && !plugin.getConfig().getBoolean("explosions.restore.wither")) {
            return;
        }
        isRestoring = true;
        for (Block block : event.blockList()) {
            if (block.getType() != Material.AIR) {
                restoreBlock(block);
            }
        }
        event.setYield(plugin.getConfig().getInt("explosions.yield"));
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            isRestoring = false;
            // remove dropped items
            for (Entity e : event.getLocation().getWorld().getNearbyEntities(event.getLocation(), 16, 16, 16, (d) -> d.getType() == EntityType.ITEM)) {
                e.remove();
            }
        }, plugin.getConfig().getInt("explosions.delay.max"));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        Block block = event.getBlock();
        if (block == null) {
            return;
        }
        if (!(block.getBlockData() instanceof Bed) || !plugin.getConfig().getBoolean("explosions.restore.bed")) {
            return;
        }
        isRestoring = true;
        for (Block next : event.blockList()) {
            if (next.getType() != Material.AIR) {
                restoreBlock(next);
            }
        }
        event.setYield(plugin.getConfig().getInt("explosions.yield"));
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> isRestoring = false, plugin.getConfig().getInt("explosions.delay.max"));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onSandFall(EntityChangeBlockEvent event) {
        if (isRestoring && event.getEntityType() == EntityType.FALLING_BLOCK && event.getTo() == Material.AIR) {
            event.setCancelled(true);
            // update the block to fix a visual client bug, but don't apply physics
            event.getBlock().getState().update(false, false);
        }
    }

    private void restoreBlock(Block block) {
        int min = plugin.getConfig().getInt("explosions.delay.min");
        int max = plugin.getConfig().getInt("explosions.delay.max");
        BlockState state = block.getState();
        String[] signLinesFront = state instanceof Sign sign ? sign.getSide(Side.FRONT).getLines() : null;
        String[] signLinesBack = state instanceof Sign sign ? sign.getSide(Side.BACK).getLines() : null;
        ItemStack[] items = state instanceof InventoryHolder holder ? holder.getInventory().getContents() : null;
        if (items != null) {
            for (int i = 0; i < items.length; i++) {
                if (items[i] != null) {
                    items[i] = items[i].clone();
                }
            }
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!state.getBlockData().isSupported(state.getBlock())) {
                // restore later
                plugin.getServer().getConsoleSender().sendMessage("BlockState [" + state.getType() + "] would not be supported");
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, ()-> {
                    state.update(true);
                    BlockState newState = block.getState();
                    // restore inventory contents & sign lines
                    if (signLinesFront != null && newState instanceof Sign sign) {
                        for (int i = 0; i < 4; i++) {
                            sign.getSide(Side.FRONT).setLine(i, signLinesFront[i]);
                            sign.getSide(Side.BACK).setLine(i, signLinesBack[i]);
                        }
                        sign.update();
                    }
                }, max);
            } else {
                // restore it now
                state.update(true);
                BlockState newState = block.getState();
                // restore inventory contents & sign lines
                if (signLinesFront != null && newState instanceof Sign sign) {
                    for (int i = 0; i < 4; i++) {
                        sign.getSide(Side.FRONT).setLine(i, signLinesFront[i]);
                        sign.getSide(Side.BACK).setLine(i, signLinesBack[i]);
                    }
                    sign.update();
                } else if (items != null && newState instanceof InventoryHolder holder) {
                    holder.getInventory().setContents(items);
                }
            }
        }, min + random.nextInt(max - min));
    }
}
