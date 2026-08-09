package me.eccentric_nz.bromleymassivetools;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import me.eccentric_nz.bromleymassivetools.brigadier.*;

import java.util.List;

public class CommandRegister {

    private final ReloadableRegistrarEvent<Commands> commands;
    private final BromleyMassiveTools plugin;

    public CommandRegister(ReloadableRegistrarEvent<Commands> commands, BromleyMassiveTools plugin) {
        this.commands = commands;
        this.plugin = plugin;
    }

    public void addAll() {
        commands.registrar().register(new HealCommandNode().build(), List.of("heal"));
        commands.registrar().register(new HomeCommandNode(plugin).build(), List.of("home", "bed"));
        commands.registrar().register(new BackCommandNode(plugin).build(), List.of("back"));
        commands.registrar().register(new LightCommandNode().build(), List.of("light"));
        commands.registrar().register(new TopCommandNode().build(), List.of("top"));
        commands.registrar().register(new ExitCommandNode().build(), List.of("exit", "quit"));
        commands.registrar().register(new UpCommandNode().build(), List.of("up"));


    }
}
