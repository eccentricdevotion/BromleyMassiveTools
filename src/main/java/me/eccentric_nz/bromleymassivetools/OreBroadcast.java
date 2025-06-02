package me.eccentric_nz.bromleymassivetools;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class OreBroadcast {

    private final BromleyMassiveTools plugin;

    public OreBroadcast(BromleyMassiveTools plugin) {
        this.plugin = plugin;
    }

    public void handleBroadcast(Material mat, int blockTotal, Player player) {
        broadcastFoundBlock(player, mat, blockTotal);
    }

    private void broadcastFoundBlock(Player player, Material mat, int count) {
        String matName = getFormattedName(mat, count);
        ChatColor colour = getBlockColour(mat);
        String message = String.format("You found %s%s %s", colour, ((count) == 500 ? "over 500" : String.valueOf(count)), matName);
        for (Player x : plugin.getServer().getOnlinePlayers()) {
            x.sendMessage(message);
        }
    }

    private ChatColor getBlockColour(Material material) {
        return switch (material) {
            case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE -> ChatColor.AQUA;
            case REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE -> ChatColor.RED;
            case GOLD_ORE, DEEPSLATE_GOLD_ORE, NETHER_GOLD_ORE -> ChatColor.YELLOW;
            case IRON_ORE, DEEPSLATE_IRON_ORE -> ChatColor.GRAY;
            case LAPIS_ORE, DEEPSLATE_LAPIS_ORE -> ChatColor.BLUE;
            case COAL_ORE, DEEPSLATE_COAL_ORE -> ChatColor.DARK_GRAY;
            case EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> ChatColor.GREEN;
            case NETHER_QUARTZ_ORE -> ChatColor.WHITE;
            case COPPER_ORE, DEEPSLATE_COPPER_ORE -> ChatColor.GOLD;
            default -> ChatColor.RESET;
        };
    }

    private String getFormattedName(Material material, int count) {
        String ore = titleCase(material.toString().replace("_", " "));
        return (count > 1) ? ore + "s" : ore;
    }

    public static String titleCase(String s) {
        String[] split = s.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String str : split) {
            builder.append(uppercaseFirst(str)).append(" ");
        }
        return builder.toString().trim();
    }

    public static String uppercaseFirst(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
