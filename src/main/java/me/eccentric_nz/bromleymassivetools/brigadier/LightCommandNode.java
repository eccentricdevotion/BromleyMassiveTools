package me.eccentric_nz.bromleymassivetools.brigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;

public class LightCommandNode {

    public LiteralCommandNode<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("light")
                .requires(ctx -> ctx.getSender() instanceof Player && ctx.getSender().isOp())
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    // give a light block
                    String give = "minecraft:give " + player.getName() + " minecraft:light{BlockStateTag: {level:\"15\"}}";
                    player.performCommand(give);
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.argument("level", IntegerArgumentType.integer(1, 15))
                        .executes(ctx -> {
                            Player player = (Player) ctx.getSource().getSender();
                            // give a light block
                            int level = ctx.getArgument("level", Integer.class);
                            String give = "minecraft:give " + player.getName() + " minecraft:light{BlockStateTag: {level:\"" + level + "\"}}";
                            player.performCommand(give);
                            return Command.SINGLE_SUCCESS;
                        })
                );
        return command.build();
    }
}
