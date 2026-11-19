package com.ahmed.everythingsell.command;

import com.ahmed.everythingsell.EverythingSellPlugin;
import com.ahmed.everythingsell.config.CommandsConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

public final class CommandRegistrar {

    private final EverythingSellPlugin plugin;
    private final EverythingSellCommand executor;

    public CommandRegistrar(EverythingSellPlugin plugin, EverythingSellCommand executor) {
        this.plugin = plugin;
        this.executor = executor;
    }

    public void registerConfiguredCommands(CommandsConfig commandsConfig) {
        // 1. Bind default plugin.yml command if present
        PluginCommand baseCommand = plugin.getCommand("sell");
        if (baseCommand != null) {
            baseCommand.setExecutor(executor);
            baseCommand.setTabCompleter(executor);
            baseCommand.setPermission(null);
        }

        // 2. Dynamically register the primary command & aliases from commands.yml into the server CommandMap
        try {
            CommandMap commandMap = resolveCommandMap();
            if (commandMap == null) {
                return;
            }

            String primary = commandsConfig.getPrimaryCommand();
            PluginCommand existing = plugin.getCommand(primary);
            if (existing != null) {
                existing.setExecutor(executor);
                existing.setTabCompleter(executor);
                existing.setDescription(commandsConfig.getDescription());
                existing.setPermission(null);
            } else {
                PluginCommand dynamicCommand = createPluginCommand(primary, plugin);
                if (dynamicCommand != null) {
                    dynamicCommand.setDescription(commandsConfig.getDescription());
                    dynamicCommand.setAliases(new ArrayList<>(commandsConfig.getAliases()));
                    dynamicCommand.setExecutor(executor);
                    dynamicCommand.setTabCompleter(executor);
                    dynamicCommand.setPermission(null);
                    commandMap.register(plugin.getName().toLowerCase(Locale.ROOT), dynamicCommand);
                }
            }

            for (String alias : commandsConfig.getAliases()) {
                Command alreadyRegistered = commandMap.getCommand(alias);
                if (alreadyRegistered == null) {
                    PluginCommand aliasCmd = createPluginCommand(alias, plugin);
                    if (aliasCmd != null) {
                        aliasCmd.setDescription(commandsConfig.getDescription());
                        aliasCmd.setExecutor(executor);
                        aliasCmd.setTabCompleter(executor);
                        aliasCmd.setPermission(null);
                        commandMap.register(plugin.getName().toLowerCase(Locale.ROOT), aliasCmd);
                    }
                }
            }

            syncServerCommands();
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING, "Could not dynamically register custom command from commands.yml", t);
        }
    }

    private CommandMap resolveCommandMap() throws Exception {
        Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        commandMapField.setAccessible(true);
        return (CommandMap) commandMapField.get(Bukkit.getServer());
    }

    private PluginCommand createPluginCommand(String name, Plugin owner) throws Exception {
        Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
        constructor.setAccessible(true);
        return constructor.newInstance(name, owner);
    }

    private void syncServerCommands() {
        try {
            Method syncMethod = Bukkit.getServer().getClass().getDeclaredMethod("syncCommands");
            syncMethod.setAccessible(true);
            syncMethod.invoke(Bukkit.getServer());
        } catch (Throwable ignored) {
        }
    }
}
