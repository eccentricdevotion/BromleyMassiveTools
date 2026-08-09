package me.eccentric_nz.bromleymassivetools.brigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

public class ExitCommandNode {

    public LiteralCommandNode<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("exit")
                .requires(ctx -> ctx.getSender() instanceof ConsoleCommandSender)
                .executes(ctx -> {
                    Bukkit.dispatchCommand(ctx.getSource().getSender(), "stop");
                    return Command.SINGLE_SUCCESS;
                });
        return command.build();
    }
}
