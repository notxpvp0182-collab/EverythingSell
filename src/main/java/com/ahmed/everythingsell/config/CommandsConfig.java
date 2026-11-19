package com.ahmed.everythingsell.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class CommandsConfig {

    private final String primaryCommand;
    private final List<String> aliases;
    private final String reloadArgument;
    private final String description;

    public CommandsConfig(FileConfiguration config) {
        String rawPrimary = config.getString("command", "sell");
        if (rawPrimary == null || rawPrimary.isBlank()) {
            rawPrimary = "sell";
        }
        this.primaryCommand = sanitizeCommandName(rawPrimary);

        List<String> rawAliases = config.getStringList("aliases");
        List<String> cleanedAliases = new ArrayList<>();
        for (String alias : rawAliases) {
            if (alias != null && !alias.isBlank()) {
                String clean = sanitizeCommandName(alias);
                if (!clean.isEmpty() && !clean.equalsIgnoreCase(this.primaryCommand)) {
                    cleanedAliases.add(clean);
                }
            }
        }
        this.aliases = Collections.unmodifiableList(cleanedAliases);

        String rawReload = config.getString("reload-argument", "reload");
        this.reloadArgument = (rawReload == null || rawReload.isBlank()) ? "reload" : rawReload.trim().toLowerCase(Locale.ROOT);
        this.description = config.getString("description", "Open the Everything Sell GUI");
    }

    private String sanitizeCommandName(String name) {
        String clean = name.trim().toLowerCase(Locale.ROOT);
        if (clean.startsWith("/")) {
            clean = clean.substring(1);
        }
        return clean.replaceAll("[^a-z0-9_-]", "");
    }

    public String getPrimaryCommand() {
        return primaryCommand;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public String getReloadArgument() {
        return reloadArgument;
    }

    public String getDescription() {
        return description;
    }
}
