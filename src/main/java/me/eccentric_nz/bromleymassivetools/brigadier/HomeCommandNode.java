package me.eccentric_nz.bromleymassivetools.brigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.eccentric_nz.bromleymassivetools.BromleyMassiveTools;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public class HomeCommandNode {

    private final BromleyMassiveTools plugin;

    public HomeCommandNode(BromleyMassiveTools plugin) {
        this.plugin = plugin;
    }

    public LiteralCommandNode<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("home")
                .requires(ctx -> ctx.getSender() instanceof Player && ctx.getSender().isOp())
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    Location bed = player.getBedSpawnLocation();
                    if (bed == null) {
                        player.sendMessage(plugin.pluginName + "You haven't placed a bed yet!");
                        return Command.SINGLE_SUCCESS;
                    }
                    // teleport
                    player.teleport(bed);
                    return Command.SINGLE_SUCCESS;
                });
        return command.build();
    }
}
