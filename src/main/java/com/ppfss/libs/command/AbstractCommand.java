// PPFSS_Libs Plugin
// Авторские права (c) 2025 PPFSS
// Лицензия: MIT

package com.ppfss.libs.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("unused")
public abstract class AbstractCommand extends Command {


    protected final Map<String, SubCommand> subCommands = new HashMap<>();

    public AbstractCommand(String name, Plugin plugin) {
        super(name);

        registerCommand(plugin);
    }

    public AbstractCommand(String name, String description, String usage, List<String> aliases, Plugin plugin) {
        super(name, description, usage, aliases);

        registerCommand(plugin);
    }

    public AbstractCommand(String name, List<String> aliases, Plugin plugin) {
        super(name, "", "", aliases);

        registerCommand(plugin);
    }

    public void registerSubCommand(SubCommand subCommand) {
        String name = subCommand.getName().toLowerCase();

        subCommands.put(name, subCommand);
    }

    private void registerCommand(Plugin plugin) {
        Bukkit.getCommandMap().register(plugin.getName(), this);
    }


    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (args.length > 0 && !subCommands.isEmpty()) {
            SubCommand subCommand = subCommands.get(args[0].toLowerCase());
            if (subCommand != null) {
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

                if (!subCommand.hasPermission(sender, this, "", subArgs)) {
                    subCommand.noPermission(sender, this, commandLabel, subArgs);
                    return true;
                }

                subCommand.execute(sender, this, commandLabel, subArgs);
                return true;
            }
        }
        handle(sender, this, commandLabel, args);
        return true;
    }

    protected void handle(CommandSender sender, Command command, String commandLabel, String[] args) {}

    private List<String> filter(List<String> strings, String... args) {
        if (strings == null || strings.isEmpty()) return new ArrayList<>();
        String lastArg = args[args.length - 1].toLowerCase().trim();
        List<String> filtered = new ArrayList<>();
        for (String string : strings) {
            if (string.toLowerCase().startsWith(lastArg.toLowerCase())) filtered.add(string);
        }
        return filtered;
    }

    public List<String> complete(CommandSender sender, String label, String... args) {

        if (args.length == 1) {
            List<String> result = new ArrayList<>();

            for (Map.Entry<String, SubCommand> entry : subCommands.entrySet()) {
                SubCommand subCommand = entry.getValue();
                String name = entry.getKey();
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

                if (!subCommand.hasPermission(sender, this, label, subArgs)) {
                    continue;
                }
                result.add(name);
            }
            return result;

        } else if (args.length > 1) {
            SubCommand subCommand = subCommands.get(args[0].toLowerCase());
            if (subCommand != null) {
                return subCommand.complete(sender, Arrays.copyOfRange(args, 1, args.length));
            }
        }
        return Collections.emptyList();
    }


    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        return filter(complete(sender, alias, args), args);
    }
}
