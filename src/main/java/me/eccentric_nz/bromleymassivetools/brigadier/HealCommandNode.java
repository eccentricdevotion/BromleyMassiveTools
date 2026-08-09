package me.eccentric_nz.bromleymassivetools.brigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.eccentric_nz.bromleymassivetools.BromleyMassiveTools;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public class HealCommandNode {

    public LiteralCommandNode<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("heal")
                .requires(ctx -> ctx.getSender() instanceof Player && ctx.getSender().isOp())
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    // restore full health & hunger
                    player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getDefaultValue());
                    player.setFoodLevel(20);
                    player.setSaturation(5.0f);
                    return Command.SINGLE_SUCCESS;
                });
        return command.build();
    }
}
