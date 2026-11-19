package com.ahmed.everythingsell.command;

import com.ahmed.everythingsell.EverythingSellPlugin;
import com.ahmed.everythingsell.config.CommandsConfig;
import com.ahmed.everythingsell.config.ConfigManager;
import com.ahmed.everythingsell.config.MessagesConfig;
import com.ahmed.everythingsell.gui.GuiManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class EverythingSellCommand implements CommandExecutor, TabCompleter {

    public static final String PERMISSION_ADMIN = "everythingsell.admin";
    public static final String PERMISSION_RELOAD = "everythingsell.reload";

    private final EverythingSellPlugin plugin;
    private final ConfigManager configManager;
    private final GuiManager guiManager;

    public EverythingSellCommand(
            EverythingSellPlugin plugin,
            ConfigManager configManager,
            GuiManager guiManager
    ) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.guiManager = guiManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        MessagesConfig messages = configManager.getMessages();
        CommandsConfig commandsConfig = configManager.getCommandsConfig();

        if (args.length > 0 && args[0].equalsIgnoreCase(commandsConfig.getReloadArgument())) {
            if (!hasReloadPermission(sender)) {
                messages.send(sender, messages.getNoPermission());
                return true;
            }
            plugin.reloadEverythingSell();
            configManager.getMessages().send(sender, configManager.getMessages().getReloadSuccess());
            return true;
        }

        // Normal usage: NO permission required for opening the Everything Sell GUI
        if (!(sender instanceof Player player)) {
            messages.send(sender, messages.getPlayerOnly());
            return true;
        }

        guiManager.openSellGui(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && hasReloadPermission(sender)) {
            String reloadArg = configManager.getCommandsConfig().getReloadArgument();
            if (reloadArg.startsWith(args[0].toLowerCase(Locale.ROOT))) {
                return List.of(reloadArg);
            }
        }
        return Collections.emptyList();
    }

    private boolean hasReloadPermission(CommandSender sender) {
        return sender.isOp()
                || sender.hasPermission(PERMISSION_RELOAD)
                || sender.hasPermission(PERMISSION_ADMIN);
    }
}
