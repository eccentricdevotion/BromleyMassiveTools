package me.eccentric_nz.bromleymassivetools.brigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.eccentric_nz.bromleymassivetools.BromleyMassiveTools;
import me.eccentric_nz.bromleymassivetools.DeathTracker;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BackCommandNode {

    private final BromleyMassiveTools plugin;

    public BackCommandNode(BromleyMassiveTools plugin) {
        this.plugin = plugin;
    }

    public LiteralCommandNode<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("back")
                .requires(ctx -> ctx.getSender() instanceof Player && ctx.getSender().isOp())
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    UUID uuid = player.getUniqueId();
                    if (DeathTracker.DEATH_LOCATIONS.containsKey(uuid)) {
                        // teleport
                        Location death = DeathTracker.DEATH_LOCATIONS.get(uuid);
                        player.teleport(death);
                        if (death.getWorld().getName().equals("TARDIS_TimeVortex")) {
                            player.performCommand("tardis occupy");
                        }
                    } else {
                        player.sendMessage(plugin.pluginName + "You haven't died yet!");
                    }
                    return Command.SINGLE_SUCCESS;
                });
        return command.build();
    }
}
