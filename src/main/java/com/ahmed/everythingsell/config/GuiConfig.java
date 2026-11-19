package com.ahmed.everythingsell.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class GuiConfig {

    private final String sellTitle;
    private final int sellSize;
    private final boolean sellFillerEnabled;
    private final Material sellFillerMaterial;
    private final String sellFillerName;
    private final Set<Integer> sellFillerSlots;

    private final boolean sellButtonEnabled;
    private final int sellButtonSlot;
    private final Material sellButtonMaterial;
    private final String sellButtonName;
    private final List<String> sellButtonLore;

    private final String confirmTitle;
    private final int confirmSize;
    private final boolean confirmFillerEnabled;
    private final Material confirmFillerMaterial;
    private final String confirmFillerName;
    private final Set<Integer> confirmFillerSlots;

    private final Set<Integer> confirmButtonSlots;
    private final Material confirmButtonMaterial;
    private final String confirmButtonName;
    private final List<String> confirmButtonLore;

    private final boolean summaryItemEnabled;
    private final int summaryItemSlot;
    private final Material summaryItemMaterial;
    private final String summaryItemName;
    private final List<String> summaryItemLore;

    private final Set<Integer> cancelButtonSlots;
    private final Material cancelButtonMaterial;
    private final String cancelButtonName;
    private final List<String> cancelButtonLore;

    public GuiConfig(FileConfiguration config) {
        this.sellTitle = config.getString("sell-gui.title", "&8ᴇᴠᴇʀʏᴛʜɪɴɢ sᴇʟʟ");
        this.sellSize = normalizeSize(config.getInt("sell-gui.size", 54), 54);
        this.sellFillerEnabled = config.getBoolean("sell-gui.filler.enabled", true);
        this.sellFillerMaterial = parseMaterial(config.getString("sell-gui.filler.material"), Material.GRAY_STAINED_GLASS_PANE);
        this.sellFillerName = config.getString("sell-gui.filler.name", " ");
        this.sellFillerSlots = readSlots(config.getIntegerList("sell-gui.filler.slots"), this.sellSize, List.of(45, 46, 47, 48, 50, 51, 52, 53));

        this.sellButtonEnabled = config.getBoolean("sell-gui.sell-button.enabled", true);
        int rawSellBtnSlot = config.getInt("sell-gui.sell-button.slot", 49);
        this.sellButtonSlot = (rawSellBtnSlot >= 0 && rawSellBtnSlot < this.sellSize) ? rawSellBtnSlot : (this.sellSize - 5);
        this.sellButtonMaterial = parseMaterial(config.getString("sell-gui.sell-button.material"), Material.EMERALD);
        this.sellButtonName = config.getString("sell-gui.sell-button.name", "&#55FF55sᴇʟʟ ɪᴛᴇᴍs");
        this.sellButtonLore = config.getStringList("sell-gui.sell-button.lore");

        this.confirmTitle = config.getString("confirmation-gui.title", "&8ᴄᴏɴғɪʀᴍ sᴇʟʟ");
        this.confirmSize = normalizeSize(config.getInt("confirmation-gui.size", 27), 27);
        this.confirmFillerEnabled = config.getBoolean("confirmation-gui.filler.enabled", true);
        this.confirmFillerMaterial = parseMaterial(config.getString("confirmation-gui.filler.material"), Material.GRAY_STAINED_GLASS_PANE);
        this.confirmFillerName = config.getString("confirmation-gui.filler.name", " ");
        this.confirmFillerSlots = readSlots(
                config.getIntegerList("confirmation-gui.filler.slots"),
                this.confirmSize,
                List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26)
        );

        this.confirmButtonSlots = readSlots(config.getIntegerList("confirmation-gui.confirm-button.slots"), this.confirmSize, List.of(11));
        this.confirmButtonMaterial = parseMaterial(config.getString("confirmation-gui.confirm-button.material"), Material.LIME_TERRACOTTA);
        this.confirmButtonName = config.getString("confirmation-gui.confirm-button.name", "&#55FF55ᴄᴏɴғɪʀᴍ sᴇʟʟ");
        this.confirmButtonLore = config.getStringList("confirmation-gui.confirm-button.lore");

        this.summaryItemEnabled = config.getBoolean("confirmation-gui.summary-item.enabled", true);
        int rawSummarySlot = config.getInt("confirmation-gui.summary-item.slot", 13);
        this.summaryItemSlot = (rawSummarySlot >= 0 && rawSummarySlot < this.confirmSize) ? rawSummarySlot : 13;
        this.summaryItemMaterial = parseMaterial(config.getString("confirmation-gui.summary-item.material"), Material.GOLD_INGOT);
        this.summaryItemName = config.getString("confirmation-gui.summary-item.name", "&fsᴇʟʟ sᴜᴍᴍᴀʀʏ");
        this.summaryItemLore = config.getStringList("confirmation-gui.summary-item.lore");

        this.cancelButtonSlots = readSlots(config.getIntegerList("confirmation-gui.cancel-button.slots"), this.confirmSize, List.of(15));
        this.cancelButtonMaterial = parseMaterial(config.getString("confirmation-gui.cancel-button.material"), Material.RED_TERRACOTTA);
        this.cancelButtonName = config.getString("confirmation-gui.cancel-button.name", "&cᴄᴀɴᴄᴇʟ");
        this.cancelButtonLore = config.getStringList("confirmation-gui.cancel-button.lore");
    }

    private int normalizeSize(int size, int fallback) {
        if (size < 9 || size > 54 || size % 9 != 0) {
            return fallback;
        }
        return size;
    }

    private Material parseMaterial(String name, Material fallback) {
        if (name == null || name.isBlank()) {
            return fallback;
        }
        Material mat = Material.matchMaterial(name.trim());
        if (mat == null || mat.isAir() || !mat.isItem()) {
            return fallback;
        }
        return mat;
    }

    private Set<Integer> readSlots(List<Integer> configured, int maxSize, List<Integer> defaults) {
        List<Integer> source = (configured == null || configured.isEmpty()) ? defaults : configured;
        Set<Integer> valid = new HashSet<>();
        for (Integer slot : source) {
            if (slot != null && slot >= 0 && slot < maxSize) {
                valid.add(slot);
            }
        }
        return Collections.unmodifiableSet(valid);
    }

    public boolean isSellableInputSlot(int slot) {
        if (slot < 0 || slot >= sellSize) {
            return false;
        }
        if (sellButtonEnabled && slot == sellButtonSlot) {
            return false;
        }
        if (sellFillerEnabled && sellFillerSlots.contains(slot)) {
            return false;
        }
        return true;
    }

    public String getSellTitle() {
        return sellTitle;
    }

    public int getSellSize() {
        return sellSize;
    }

    public boolean isSellFillerEnabled() {
        return sellFillerEnabled;
    }

    public Material getSellFillerMaterial() {
        return sellFillerMaterial;
    }

    public String getSellFillerName() {
        return sellFillerName;
    }

    public Set<Integer> getSellFillerSlots() {
        return sellFillerSlots;
    }

    public boolean isSellButtonEnabled() {
        return sellButtonEnabled;
    }

    public int getSellButtonSlot() {
        return sellButtonSlot;
    }

    public Material getSellButtonMaterial() {
        return sellButtonMaterial;
    }

    public String getSellButtonName() {
        return sellButtonName;
    }

    public List<String> getSellButtonLore() {
        return sellButtonLore;
    }

    public String getConfirmTitle() {
        return confirmTitle;
    }

    public int getConfirmSize() {
        return confirmSize;
    }

    public boolean isConfirmFillerEnabled() {
        return confirmFillerEnabled;
    }

    public Material getConfirmFillerMaterial() {
        return confirmFillerMaterial;
    }

    public String getConfirmFillerName() {
        return confirmFillerName;
    }

    public Set<Integer> getConfirmFillerSlots() {
        return confirmFillerSlots;
    }

    public Set<Integer> getConfirmButtonSlots() {
        return confirmButtonSlots;
    }

    public Material getConfirmButtonMaterial() {
        return confirmButtonMaterial;
    }

    public String getConfirmButtonName() {
        return confirmButtonName;
    }

    public List<String> getConfirmButtonLore() {
        return confirmButtonLore;
    }

    public boolean isSummaryItemEnabled() {
        return summaryItemEnabled;
    }

    public int getSummaryItemSlot() {
        return summaryItemSlot;
    }

    public Material getSummaryItemMaterial() {
        return summaryItemMaterial;
    }

    public String getSummaryItemName() {
        return summaryItemName;
    }

    public List<String> getSummaryItemLore() {
        return summaryItemLore;
    }

    public Set<Integer> getCancelButtonSlots() {
        return cancelButtonSlots;
    }

    public Material getCancelButtonMaterial() {
        return cancelButtonMaterial;
    }

    public String getCancelButtonName() {
        return cancelButtonName;
    }

    public List<String> getCancelButtonLore() {
        return cancelButtonLore;
    }
}
