package com.ahmed.everythingsell.manager;

import com.ahmed.everythingsell.EverythingSellPlugin;
import com.ahmed.everythingsell.config.ConfigManager;
import com.ahmed.everythingsell.config.PluginSettings;
import com.ahmed.everythingsell.config.SoundSettings;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Locale;

public final class SoundManager {

    private final EverythingSellPlugin plugin;
    private final ConfigManager configManager;

    public SoundManager(EverythingSellPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void validateConfiguredSounds() {
        PluginSettings settings = configManager.getSettings();
        validateSetting("gui-open", settings.getGuiOpenSound());
        validateSetting("sell-success", settings.getSellSuccessSound());
        validateSetting("confirmation-open", settings.getConfirmationOpenSound());
        validateSetting("confirm", settings.getConfirmSound());
        validateSetting("cancel", settings.getCancelSound());
        validateSetting("error", settings.getErrorSound());
    }

    private void validateSetting(String key, SoundSettings soundSettings) {
        if (soundSettings == null || !soundSettings.isEnabled()) {
            return;
        }
        Sound resolved = resolveSound(soundSettings.getSoundName());
        if (resolved == null && configManager.getSettings().isDebugEnabled()) {
            plugin.getLogger().warning("[Debug] Configured sound for '" + key + "' (" + soundSettings.getSoundName() + ") is not recognized on this server version.");
        }
    }

    public void playGuiOpen(Player player) {
        play(player, configManager.getSettings().getGuiOpenSound());
    }

    public void playSellSuccess(Player player) {
        play(player, configManager.getSettings().getSellSuccessSound());
    }

    public void playConfirmationOpen(Player player) {
        play(player, configManager.getSettings().getConfirmationOpenSound());
    }

    public void playConfirm(Player player) {
        play(player, configManager.getSettings().getConfirmSound());
    }

    public void playCancel(Player player) {
        play(player, configManager.getSettings().getCancelSound());
    }

    public void playError(Player player) {
        play(player, configManager.getSettings().getErrorSound());
    }

    public void play(Player player, SoundSettings soundSettings) {
        if (player == null || !player.isOnline() || soundSettings == null || !soundSettings.isEnabled()) {
            return;
        }
        Sound sound = resolveSound(soundSettings.getSoundName());
        if (sound == null) {
            return;
        }
        try {
            player.playSound(player.getLocation(), sound, soundSettings.getVolume(), soundSettings.getPitch());
        } catch (Throwable ignored) {
        }
    }

    public Sound resolveSound(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            return null;
        }
        String trimmed = rawName.trim();

        // 1. Direct enum / Sound.valueOf lookup
        try {
            return Sound.valueOf(trimmed.toUpperCase(Locale.ROOT).replace('.', '_'));
        } catch (Throwable ignored) {
        }

        // 2. Registry lookup via NamespacedKey (e.g., entity.player.levelup or ENTITY_PLAYER_LEVELUP)
        try {
            if (Registry.SOUNDS != null) {
                String lowerKey = trimmed.toLowerCase(Locale.ROOT);
                if (lowerKey.startsWith("minecraft:")) {
                    lowerKey = lowerKey.substring("minecraft:".length());
                }
                Sound byExactKey = Registry.SOUNDS.get(NamespacedKey.minecraft(lowerKey));
                if (byExactKey != null) {
                    return byExactKey;
                }
                Sound byDottedKey = Registry.SOUNDS.get(NamespacedKey.minecraft(lowerKey.replace('_', '.')));
                if (byDottedKey != null) {
                    return byDottedKey;
                }
            }
        } catch (Throwable ignored) {
        }

        return null;
    }
}
