package me.eccentric_nz.bromleymassivetools.brigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class UpCommandNode {

    public LiteralCommandNode<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("up")
                .requires(ctx -> ctx.getSender() instanceof Player && ctx.getSender().isOp())
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    // restore full health & hunger
                    player.getWorld().getBlockAt(player.getLocation()).getRelative(BlockFace.DOWN).setType(Material.GLASS);
                    return Command.SINGLE_SUCCESS;
                });
        return command.build();
    }
}
