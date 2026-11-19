package com.ahmed.everythingsell.price;

import com.ahmed.everythingsell.EverythingSellPlugin;
import com.ahmed.everythingsell.config.ConfigManager;
import com.ahmed.everythingsell.registry.RegistryScanResult;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

public final class PriceManager {

    private final EverythingSellPlugin plugin;
    private final ConfigManager configManager;
    private final Map<Material, Double> priceTable = new EnumMap<>(Material.class);
    private int totalLoadedPrices;
    private int activeSellableCount;

    public PriceManager(EverythingSellPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public synchronized void synchronizeAndLoad(RegistryScanResult scanResult) {
        File pricesDir = new File(plugin.getDataFolder(), "prices");
        if (!pricesDir.exists()) {
            pricesDir.mkdirs();
        }

        File blocksFile = ensureDefaultFile("prices/blocks.yml");
        File itemsFile = ensureDefaultFile("prices/items.yml");

        YamlConfiguration blocksConfig = YamlConfiguration.loadConfiguration(blocksFile);
        YamlConfiguration itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);

        double defaultPrice = configManager.getSettings().getDefaultSellPrice();
        boolean autoDetect = configManager.getSettings().isAutoDetectRegistry();

        if (autoDetect && scanResult != null) {
            boolean blocksAdded = synchronizeSection(blocksConfig, "blocks", scanResult.getBlocks(), defaultPrice);
            if (blocksAdded) {
                saveSafely(blocksConfig, blocksFile);
            }

            boolean itemsAdded = synchronizeSection(itemsConfig, "items", scanResult.getItems(), defaultPrice);
            if (itemsAdded) {
                saveSafely(itemsConfig, itemsFile);
            }
        }

        priceTable.clear();
        totalLoadedPrices = 0;
        activeSellableCount = 0;

        loadSectionIntoMemory(blocksConfig, "blocks");
        loadSectionIntoMemory(itemsConfig, "items");
    }

    public static boolean synchronizeSection(
            YamlConfiguration yaml,
            String rootKey,
            Map<String, Material> detectedMaterials,
            double defaultPrice
    ) {
        boolean modified = false;
        ConfigurationSection section = yaml.getConfigurationSection(rootKey);
        if (section == null) {
            section = yaml.createSection(rootKey);
            modified = true;
        }

        for (String materialKey : detectedMaterials.keySet()) {
            String normalizedKey = materialKey.toLowerCase(Locale.ROOT);
            String pricePath = normalizedKey + ".sell-price";
            if (!section.contains(normalizedKey) && !section.contains(pricePath)) {
                section.set(pricePath, defaultPrice);
                modified = true;
            }
        }
        return modified;
    }

    private void loadSectionIntoMemory(YamlConfiguration yaml, String rootKey) {
        ConfigurationSection section = yaml.getConfigurationSection(rootKey);
        if (section == null) {
            return;
        }

        boolean debug = configManager.getSettings().isDebugEnabled();

        for (String key : section.getKeys(false)) {
            Material material = resolveMaterial(key);
            if (material == null || material.isAir() || !material.isItem()) {
                if (debug) {
                    plugin.getLogger().info("[Debug] Skipping unsupported or non-item material in " + rootKey + ".yml: " + key);
                }
                continue;
            }

            double price;
            if (section.isConfigurationSection(key)) {
                price = section.getDouble(key + ".sell-price", 0.0D);
            } else {
                price = section.getDouble(key, 0.0D);
            }

            if (!Double.isFinite(price) || price < 0.0D) {
                if (debug) {
                    plugin.getLogger().warning("[Debug] Invalid negative price for " + key + ", treating as 0.0");
                }
                price = 0.0D;
            }

            priceTable.put(material, price);
            totalLoadedPrices++;
            if (price > 0.0D) {
                activeSellableCount++;
            }
        }
    }

    private Material resolveMaterial(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        Material direct = Material.matchMaterial(key.trim());
        if (direct != null && !direct.isLegacy()) {
            return direct;
        }
        try {
            return Material.valueOf(key.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private File ensureDefaultFile(String resourcePath) {
        File file = new File(plugin.getDataFolder(), resourcePath);
        if (!file.exists()) {
            plugin.saveResource(resourcePath, false);
        }
        return file;
    }

    private void saveSafely(YamlConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save price configuration file: " + file.getName(), e);
        }
    }

    public double getSellPrice(Material material) {
        if (material == null) {
            return 0.0D;
        }
        return priceTable.getOrDefault(material, 0.0D);
    }

    public boolean isSellable(ItemStack item) {
        if (item == null || item.getType().isAir() || item.getAmount() <= 0) {
            return false;
        }
        return getSellPrice(item.getType()) > 0.0D;
    }

    public SellCalculation calculate(Collection<ItemStack> items) {
        return calculateWithPrices(items, this.priceTable);
    }

    public static SellCalculation calculateWithPrices(Collection<ItemStack> items, Map<Material, Double> prices) {
        List<ItemStack> sellable = new ArrayList<>();
        List<ItemStack> unsellable = new ArrayList<>();
        int totalQuantity = 0;
        double totalValue = 0.0D;

        if (items != null) {
            for (ItemStack item : items) {
                if (item == null || item.getType().isAir() || item.getAmount() <= 0) {
                    continue;
                }
                double unitPrice = prices.getOrDefault(item.getType(), 0.0D);
                if (Double.isFinite(unitPrice) && unitPrice > 0.0D) {
                    int amount = item.getAmount();
                    totalQuantity += amount;
                    totalValue += (unitPrice * amount);
                    sellable.add(item.clone());
                } else {
                    unsellable.add(item.clone());
                }
            }
        }

        totalValue = Math.round(totalValue * 100.0D) / 100.0D;
        return new SellCalculation(sellable, unsellable, totalQuantity, totalValue);
    }

    public int getTotalLoadedPrices() {
        return totalLoadedPrices;
    }

    public int getActiveSellableCount() {
        return activeSellableCount;
    }
}
