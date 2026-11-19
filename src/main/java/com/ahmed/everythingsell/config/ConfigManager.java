package com.ahmed.everythingsell.config;

import com.ahmed.everythingsell.EverythingSellPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public final class ConfigManager {

    private final EverythingSellPlugin plugin;
    private PluginSettings settings;
    private MessagesConfig messages;
    private CommandsConfig commandsConfig;
    private GuiConfig guiConfig;

    public ConfigManager(EverythingSellPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.settings = new PluginSettings(plugin.getConfig());

        FileConfiguration messagesYaml = loadOrCreateYaml("messages.yml");
        this.messages = new MessagesConfig(messagesYaml);

        FileConfiguration commandsYaml = loadOrCreateYaml("commands.yml");
        this.commandsConfig = new CommandsConfig(commandsYaml);

        FileConfiguration guiYaml = loadOrCreateYaml("gui.yml");
        this.guiConfig = new GuiConfig(guiYaml);
    }

    public FileConfiguration loadOrCreateYaml(String resourcePath) {
        File targetFile = new File(plugin.getDataFolder(), resourcePath);
        if (!targetFile.exists()) {
            File parent = targetFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            plugin.saveResource(resourcePath, false);
        }
        return YamlConfiguration.loadConfiguration(targetFile);
    }

    public PluginSettings getSettings() {
        return settings;
    }

    public MessagesConfig getMessages() {
        return messages;
    }

    public CommandsConfig getCommandsConfig() {
        return commandsConfig;
    }

    public GuiConfig getGuiConfig() {
        return guiConfig;
    }
}
