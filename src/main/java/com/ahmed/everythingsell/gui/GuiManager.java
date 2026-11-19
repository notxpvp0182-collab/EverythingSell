package com.ahmed.everythingsell.gui;

import com.ahmed.everythingsell.EverythingSellPlugin;
import com.ahmed.everythingsell.config.ConfigManager;
import com.ahmed.everythingsell.config.GuiConfig;
import com.ahmed.everythingsell.config.MessagesConfig;
import com.ahmed.everythingsell.manager.SellManager;
import com.ahmed.everythingsell.manager.SoundManager;
import com.ahmed.everythingsell.price.PriceManager;
import com.ahmed.everythingsell.price.SellCalculation;
import com.ahmed.everythingsell.util.ItemReturnUtil;
import com.ahmed.everythingsell.util.NumberUtil;
import com.ahmed.everythingsell.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class GuiManager {

    private final EverythingSellPlugin plugin;
    private final ConfigManager configManager;
    private final PriceManager priceManager;
    private final SellManager sellManager;
    private final SoundManager soundManager;
    private final Map<UUID, SellSession> pendingConfirmationSessions = new ConcurrentHashMap<>();
    private final Map<UUID, InventoryHolder> openGuis = new ConcurrentHashMap<>();

    public GuiManager(
            EverythingSellPlugin plugin,
            ConfigManager configManager,
            PriceManager priceManager,
            SellManager sellManager,
            SoundManager soundManager
    ) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.priceManager = priceManager;
        this.sellManager = sellManager;
        this.soundManager = soundManager;
    }

    public void openSellGui(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        GuiConfig guiConfig = configManager.getGuiConfig();
        SellGuiHolder holder = new SellGuiHolder(player.getUniqueId());
        Inventory inventory = Bukkit.createInventory(
                holder,
                guiConfig.getSellSize(),
                TextUtil.colorize(guiConfig.getSellTitle())
        );
        holder.setInventory(inventory);

        if (guiConfig.isSellFillerEnabled()) {
            ItemStack filler = createItem(guiConfig.getSellFillerMaterial(), guiConfig.getSellFillerName(), List.of());
            for (int slot : guiConfig.getSellFillerSlots()) {
                inventory.setItem(slot, filler);
            }
        }

        updateSellButton(inventory);
        player.openInventory(inventory);
        openGuis.put(player.getUniqueId(), holder);
        soundManager.playGuiOpen(player);
    }

    public void updateSellButton(Inventory inventory) {
        GuiConfig guiConfig = configManager.getGuiConfig();
        if (inventory == null || !guiConfig.isSellButtonEnabled()) {
            return;
        }

        List<ItemStack> currentItems = collectPlacedItems(inventory, false);
        SellCalculation calc = priceManager.calculate(currentItems);

        String currency = configManager.getSettings().getCurrencySymbol();
        String formattedAmount = NumberUtil.formatCurrency(currency, calc.getTotalValue());
        String formattedTotal = NumberUtil.formatAmount(calc.getTotalValue());
        int itemCount = calc.getTotalSellableQuantity();

        List<String> formattedLore = applyPlaceholders(
                guiConfig.getSellButtonLore(),
                formattedAmount,
                formattedTotal,
                currency,
                itemCount
        );
        String formattedName = applyPlaceholders(
                guiConfig.getSellButtonName(),
                formattedAmount,
                formattedTotal,
                currency,
                itemCount
        );

        ItemStack button = createItem(guiConfig.getSellButtonMaterial(), formattedName, formattedLore);
        inventory.setItem(guiConfig.getSellButtonSlot(), button);
    }

    public void handleSellGuiClose(Player player, SellGuiHolder holder) {
        if (player == null || holder == null) {
            return;
        }
        openGuis.remove(player.getUniqueId(), holder);
        if (!holder.tryMarkProcessed()) {
            return;
        }

        Inventory inventory = holder.getInventory();
        List<ItemStack> placedItems = collectPlacedItems(inventory, true);
        if (placedItems.isEmpty()) {
            return;
        }

        SellCalculation calculation = priceManager.calculate(placedItems);
        MessagesConfig messages = configManager.getMessages();

        if (!calculation.hasSellableItems()) {
            boolean dropped = ItemReturnUtil.returnItems(player, placedItems);
            messages.send(player, messages.getNoSellableItems());
            if (dropped) {
                messages.send(player, messages.getItemsDroppedFullInventory());
            }
            soundManager.playError(player);
            return;
        }

        SellSession session = new SellSession(player.getUniqueId(), calculation);

        if (!configManager.getSettings().isConfirmationEnabled()) {
            sellManager.executeSale(player, session);
            return;
        }

        pendingConfirmationSessions.put(player.getUniqueId(), session);
        Bukkit.getScheduler().runTask(plugin, () -> {
            SellSession pending = pendingConfirmationSessions.remove(player.getUniqueId());
            if (pending == null || pending.isCompleted()) {
                return;
            }
            if (!player.isOnline()) {
                sellManager.cancelSale(player, pending);
                return;
            }
            openConfirmationGui(player, pending);
        });
    }

    public void openConfirmationGui(Player player, SellSession session) {
        if (player == null || session == null || session.isCompleted()) {
            return;
        }

        GuiConfig guiConfig = configManager.getGuiConfig();
        ConfirmationGuiHolder holder = new ConfirmationGuiHolder(player.getUniqueId(), session);
        Inventory inventory = Bukkit.createInventory(
                holder,
                guiConfig.getConfirmSize(),
                TextUtil.colorize(guiConfig.getConfirmTitle())
        );
        holder.setInventory(inventory);

        if (guiConfig.isConfirmFillerEnabled()) {
            ItemStack filler = createItem(guiConfig.getConfirmFillerMaterial(), guiConfig.getConfirmFillerName(), List.of());
            for (int slot : guiConfig.getConfirmFillerSlots()) {
                inventory.setItem(slot, filler);
            }
        }

        String currency = configManager.getSettings().getCurrencySymbol();
        String formattedAmount = NumberUtil.formatCurrency(currency, session.getTotalValue());
        String formattedTotal = NumberUtil.formatAmount(session.getTotalValue());
        int itemCount = session.getTotalSellableQuantity();

        ItemStack confirmItem = createItem(
                guiConfig.getConfirmButtonMaterial(),
                applyPlaceholders(guiConfig.getConfirmButtonName(), formattedAmount, formattedTotal, currency, itemCount),
                applyPlaceholders(guiConfig.getConfirmButtonLore(), formattedAmount, formattedTotal, currency, itemCount)
        );
        for (int slot : guiConfig.getConfirmButtonSlots()) {
            inventory.setItem(slot, confirmItem);
        }

        if (guiConfig.isSummaryItemEnabled()) {
            ItemStack summaryItem = createItem(
                    guiConfig.getSummaryItemMaterial(),
                    applyPlaceholders(guiConfig.getSummaryItemName(), formattedAmount, formattedTotal, currency, itemCount),
                    applyPlaceholders(guiConfig.getSummaryItemLore(), formattedAmount, formattedTotal, currency, itemCount)
            );
            inventory.setItem(guiConfig.getSummaryItemSlot(), summaryItem);
        }

        ItemStack cancelItem = createItem(
                guiConfig.getCancelButtonMaterial(),
                applyPlaceholders(guiConfig.getCancelButtonName(), formattedAmount, formattedTotal, currency, itemCount),
                applyPlaceholders(guiConfig.getCancelButtonLore(), formattedAmount, formattedTotal, currency, itemCount)
        );
        for (int slot : guiConfig.getCancelButtonSlots()) {
            inventory.setItem(slot, cancelItem);
        }

        player.openInventory(inventory);
        openGuis.put(player.getUniqueId(), holder);
        soundManager.playConfirmationOpen(player);
    }

    public void handleConfirmationClick(Player player, ConfirmationGuiHolder holder, int rawSlot) {
        if (player == null || holder == null || holder.isResolved()) {
            return;
        }

        GuiConfig guiConfig = configManager.getGuiConfig();
        Set<Integer> confirmSlots = guiConfig.getConfirmButtonSlots();
        Set<Integer> cancelSlots = guiConfig.getCancelButtonSlots();

        if (confirmSlots.contains(rawSlot)) {
            if (!holder.tryMarkResolved()) {
                return;
            }
            soundManager.playConfirm(player);
            player.closeInventory();
            sellManager.executeSale(player, holder.getSession());
        } else if (cancelSlots.contains(rawSlot)) {
            if (!holder.tryMarkResolved()) {
                return;
            }
            soundManager.playCancel(player);
            player.closeInventory();
            sellManager.cancelSale(player, holder.getSession());
        }
    }

    public void handleConfirmationClose(Player player, ConfirmationGuiHolder holder) {
        if (player == null || holder == null) {
            return;
        }
        openGuis.remove(player.getUniqueId(), holder);
        if (holder.tryMarkResolved()) {
            soundManager.playCancel(player);
            sellManager.cancelSale(player, holder.getSession());
        }
    }

    public void handlePlayerQuit(Player player) {
        if (player == null) {
            return;
        }
        SellSession pending = pendingConfirmationSessions.remove(player.getUniqueId());
        if (pending != null && !pending.isCompleted()) {
            sellManager.cancelSale(player, pending);
        }
    }

    public void closeAndReturnAllActiveSessions() {
        List<Map.Entry<UUID, InventoryHolder>> snapshot = new ArrayList<>(openGuis.entrySet());
        openGuis.clear();

        for (Map.Entry<UUID, InventoryHolder> entry : snapshot) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) {
                continue;
            }
            InventoryHolder holder = entry.getValue();
            if (holder instanceof SellGuiHolder sellHolder) {
                if (sellHolder.tryMarkProcessed()) {
                    List<ItemStack> placed = collectPlacedItems(sellHolder.getInventory(), true);
                    ItemReturnUtil.returnItems(player, placed);
                }
                player.closeInventory();
            } else if (holder instanceof ConfirmationGuiHolder confirmHolder) {
                if (confirmHolder.tryMarkResolved()) {
                    sellManager.cancelSale(player, confirmHolder.getSession());
                }
                player.closeInventory();
            }
        }

        for (Map.Entry<UUID, SellSession> entry : new ArrayList<>(pendingConfirmationSessions.entrySet())) {
            Player player = Bukkit.getPlayer(entry.getKey());
            SellSession pending = entry.getValue();
            if (player != null && pending != null && !pending.isCompleted()) {
                sellManager.cancelSale(player, pending);
            }
        }
        pendingConfirmationSessions.clear();
    }

    private List<ItemStack> collectPlacedItems(Inventory inventory, boolean clearExtractedSlots) {
        List<ItemStack> items = new ArrayList<>();
        if (inventory == null) {
            return items;
        }
        GuiConfig guiConfig = configManager.getGuiConfig();
        int size = inventory.getSize();
        for (int slot = 0; slot < size; slot++) {
            if (!guiConfig.isSellableInputSlot(slot)) {
                continue;
            }
            ItemStack item = inventory.getItem(slot);
            if (item != null && !item.getType().isAir() && item.getAmount() > 0) {
                items.add(item.clone());
                if (clearExtractedSlots) {
                    inventory.setItem(slot, null);
                }
            }
        }
        return items;
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        Material safeMaterial = (material == null || material.isAir() || !material.isItem())
                ? Material.GRAY_STAINED_GLASS_PANE
                : material;
        ItemStack stack = new ItemStack(safeMaterial, 1);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            if (name != null) {
                meta.setDisplayName(TextUtil.colorize(name));
            }
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(TextUtil.colorizeList(lore));
            }
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private String applyPlaceholders(String text, String amount, String total, String currency, int items) {
        if (text == null) {
            return "";
        }
        return text
                .replace("{amount}", amount)
                .replace("{total}", total)
                .replace("{currency}", currency)
                .replace("{items}", String.valueOf(items));
    }

    private List<String> applyPlaceholders(List<String> lines, String amount, String total, String currency, int items) {
        if (lines == null || lines.isEmpty()) {
            return List.of();
        }
        List<String> result = new ArrayList<>(lines.size());
        for (String line : lines) {
            result.add(applyPlaceholders(line, amount, total, currency, items));
        }
        return result;
    }

    public EverythingSellPlugin getPlugin() {
        return plugin;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
