package me.eccentric_nz.bromleymassivetools;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class BromleyMassiveToolsCommand implements CommandExecutor {

    private final BromleyMassiveTools plugin;

    public BromleyMassiveToolsCommand(BromleyMassiveTools plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("heal")) {
            if (sender instanceof Player player) {
                // restore full health & hunger
                player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getDefaultValue());
                player.setFoodLevel(20);
            } else {
                sender.sendMessage(plugin.pluginName + "Command can only be used by a player!");
            }
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("home")) {
            if (sender instanceof Player player) {
                Location bed = player.getBedSpawnLocation();
                if (bed == null) {
                    sender.sendMessage(plugin.pluginName + "You haven't placed a bed yet!");
                    return true;
                }
                // teleport
                player.teleport(bed);
            } else {
                sender.sendMessage(plugin.pluginName + "Command can only be used by a player!");
            }
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("back")) {
            if (sender instanceof Player player) {
                UUID uuid = player.getUniqueId();
                if (DeathTracker.DEATH_LOCATIONS.containsKey(uuid)) {
                    // teleport
                    player.teleport(DeathTracker.DEATH_LOCATIONS.get(uuid));
                } else {
                    sender.sendMessage(plugin.pluginName + "You haven't died yet!");
                    return true;
                }
            } else {
                sender.sendMessage(plugin.pluginName + "Command can only be used by a player!");
            }
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("light")) {
            if (sender instanceof Player player) {
                // give a light block
                int level = (args.length > 0) ? Integer.parseInt(args[0]) : 15;
                String give = "minecraft:give " + player.getName() + " minecraft:light{BlockStateTag: {level:\"" + level + "\"}}";
                player.performCommand(give);
            } else {
                sender.sendMessage(plugin.pluginName + "Command can only be used by a player!");
            }
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("top")) {
            if (sender instanceof Player player) {
                final int topX = player.getLocation().getBlockX();
                final int topZ = player.getLocation().getBlockZ();
                final float pitch = player.getLocation().getPitch();
                final float yaw = player.getLocation().getYaw();
                final Location unsafe = new Location(player.getWorld(), topX, player.getWorld().getMaxHeight(), topZ, yaw, pitch);
                try {
                    final Location safe = LocationUtility.getSafeDestination(unsafe);
                    // teleport
                    player.teleport(safe);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                sender.sendMessage(plugin.pluginName + "Command can only be used by a player!");
            }
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("exit")) {
            if (sender instanceof ConsoleCommandSender console) {
                Bukkit.dispatchCommand(sender, "stop");
            }
        }
        return false;
    }
}
