package com.ahmed.everythingsell.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class PluginSettings {

    private final boolean confirmationEnabled;
    private final double defaultSellPrice;
    private final String currencySymbol;
    private final boolean economyEnabled;
    private final boolean autoDetectRegistry;
    private final boolean excludeTechnicalMaterials;
    private final Set<String> excludedMaterials;

    private final boolean sellResultEnabled;
    private final String sellResultTitle;
    private final String sellResultSubtitle;
    private final String sellResultIcon;
    private final String sellResultIconColor;
    private final String sellResultAmountColor;
    private final int sellResultFadeIn;
    private final int sellResultStay;
    private final int sellResultFadeOut;

    private final SoundSettings guiOpenSound;
    private final SoundSettings sellSuccessSound;
    private final SoundSettings confirmationOpenSound;
    private final SoundSettings confirmSound;
    private final SoundSettings cancelSound;
    private final SoundSettings errorSound;

    private final boolean debugEnabled;

    public PluginSettings(FileConfiguration config) {
        this.confirmationEnabled = config.getBoolean("confirmation.enabled", true);
        this.defaultSellPrice = Math.max(0.0D, config.getDouble("prices.default-sell-price", 0.0D));
        this.currencySymbol = config.getString("prices.currency-symbol", "$");
        this.economyEnabled = config.getBoolean("economy.enabled", true);
        this.autoDetectRegistry = config.getBoolean("registry.auto-detect", true);
        this.excludeTechnicalMaterials = config.getBoolean("registry.exclude-technical-materials", true);

        Set<String> excluded = new HashSet<>();
        List<String> rawExcluded = config.getStringList("registry.excluded-materials");
        for (String entry : rawExcluded) {
            if (entry != null && !entry.isBlank()) {
                excluded.add(entry.trim().toLowerCase(Locale.ROOT));
            }
        }
        this.excludedMaterials = Collections.unmodifiableSet(excluded);

        this.sellResultEnabled = config.getBoolean("sell-result.enabled", true);
        this.sellResultTitle = config.getString("sell-result.title", "&fᴠɪᴇᴡ sᴇʟʟ");
        this.sellResultSubtitle = config.getString("sell-result.subtitle", "{icon} {amount} &fsᴇʟᴅ");
        this.sellResultIcon = config.getString("sell-result.icon", "✓");
        this.sellResultIconColor = config.getString("sell-result.icon-color", "#55FF55");
        this.sellResultAmountColor = config.getString("sell-result.amount-color", "#55FF55");
        this.sellResultFadeIn = Math.max(0, config.getInt("sell-result.fade-in", 10));
        this.sellResultStay = Math.max(1, config.getInt("sell-result.stay", 40));
        this.sellResultFadeOut = Math.max(0, config.getInt("sell-result.fade-out", 10));

        this.guiOpenSound = readSound(config, "sounds.gui-open", "BLOCK_CHEST_OPEN");
        this.sellSuccessSound = readSound(config, "sounds.sell-success", "ENTITY_PLAYER_LEVELUP");
        this.confirmationOpenSound = readSound(config, "sounds.confirmation-open", "BLOCK_NOTE_BLOCK_PLING");
        this.confirmSound = readSound(config, "sounds.confirm", "ENTITY_EXPERIENCE_ORB_PICKUP");
        this.cancelSound = readSound(config, "sounds.cancel", "BLOCK_NOTE_BLOCK_BASS");
        this.errorSound = readSound(config, "sounds.error", "ENTITY_VILLAGER_NO");

        this.debugEnabled = config.getBoolean("debug.enabled", false);
    }

    private SoundSettings readSound(FileConfiguration config, String path, String defaultSound) {
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) {
            return new SoundSettings(true, defaultSound, 1.0F, 1.0F);
        }
        boolean enabled = section.getBoolean("enabled", true);
        String sound = section.getString("sound", defaultSound);
        float volume = (float) section.getDouble("volume", 1.0D);
        float pitch = (float) section.getDouble("pitch", 1.0D);
        return new SoundSettings(enabled, sound, volume, pitch);
    }

    public boolean isConfirmationEnabled() {
        return confirmationEnabled;
    }

    public double getDefaultSellPrice() {
        return defaultSellPrice;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public boolean isEconomyEnabled() {
        return economyEnabled;
    }

    public boolean isAutoDetectRegistry() {
        return autoDetectRegistry;
    }

    public boolean isExcludeTechnicalMaterials() {
        return excludeTechnicalMaterials;
    }

    public Set<String> getExcludedMaterials() {
        return excludedMaterials;
    }

    public boolean isSellResultEnabled() {
        return sellResultEnabled;
    }

    public String getSellResultTitle() {
        return sellResultTitle;
    }

    public String getSellResultSubtitle() {
        return sellResultSubtitle;
    }

    public String getSellResultIcon() {
        return sellResultIcon;
    }

    public String getSellResultIconColor() {
        return sellResultIconColor;
    }

    public String getSellResultAmountColor() {
        return sellResultAmountColor;
    }

    public int getSellResultFadeIn() {
        return sellResultFadeIn;
    }

    public int getSellResultStay() {
        return sellResultStay;
    }

    public int getSellResultFadeOut() {
        return sellResultFadeOut;
    }

    public SoundSettings getGuiOpenSound() {
        return guiOpenSound;
    }

    public SoundSettings getSellSuccessSound() {
        return sellSuccessSound;
    }

    public SoundSettings getConfirmationOpenSound() {
        return confirmationOpenSound;
    }

    public SoundSettings getConfirmSound() {
        return confirmSound;
    }

    public SoundSettings getCancelSound() {
        return cancelSound;
    }

    public SoundSettings getErrorSound() {
        return errorSound;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }
}
