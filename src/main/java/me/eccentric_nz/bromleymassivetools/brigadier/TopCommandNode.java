package me.eccentric_nz.bromleymassivetools.brigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.eccentric_nz.bromleymassivetools.LocationUtility;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public class TopCommandNode {

    public LiteralCommandNode<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("top")
                .requires(ctx -> ctx.getSender() instanceof Player && ctx.getSender().isOp())
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
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
                    return Command.SINGLE_SUCCESS;
                });
        return command.build();
    }
}
