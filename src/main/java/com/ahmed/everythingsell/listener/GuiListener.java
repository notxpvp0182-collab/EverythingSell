package com.ahmed.everythingsell.listener;

import com.ahmed.everythingsell.config.GuiConfig;
import com.ahmed.everythingsell.gui.ConfirmationGuiHolder;
import com.ahmed.everythingsell.gui.GuiManager;
import com.ahmed.everythingsell.gui.SellGuiHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class GuiListener implements Listener {

    private final GuiManager guiManager;

    public GuiListener(GuiManager guiManager) {
        this.guiManager = guiManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory topInventory = event.getInventory();
        InventoryHolder holder = topInventory.getHolder();

        if (holder instanceof SellGuiHolder sellHolder) {
            if (sellHolder.isProcessed()) {
                event.setCancelled(true);
                return;
            }

            GuiConfig guiConfig = guiManager.getConfigManager().getGuiConfig();
            int rawSlot = event.getRawSlot();

            if (rawSlot >= 0 && rawSlot < topInventory.getSize()) {
                if (guiConfig.isSellButtonEnabled() && rawSlot == guiConfig.getSellButtonSlot()) {
                    event.setCancelled(true);
                    player.closeInventory();
                    return;
                }
                if (!guiConfig.isSellableInputSlot(rawSlot)) {
                    event.setCancelled(true);
                    return;
                }
            }

            Bukkit.getScheduler().runTask(guiManager.getPlugin(), () -> {
                if (!sellHolder.isProcessed()) {
                    guiManager.updateSellButton(topInventory);
                }
            });
            return;
        }

        if (holder instanceof ConfirmationGuiHolder confirmHolder) {
            event.setCancelled(true);
            int rawSlot = event.getRawSlot();
            if (rawSlot >= 0 && rawSlot < topInventory.getSize()) {
                guiManager.handleConfirmationClick(player, confirmHolder, rawSlot);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Inventory topInventory = event.getInventory();
        InventoryHolder holder = topInventory.getHolder();

        if (holder instanceof SellGuiHolder sellHolder) {
            if (sellHolder.isProcessed()) {
                event.setCancelled(true);
                return;
            }
            GuiConfig guiConfig = guiManager.getConfigManager().getGuiConfig();
            for (int rawSlot : event.getRawSlots()) {
                if (rawSlot >= 0 && rawSlot < topInventory.getSize() && !guiConfig.isSellableInputSlot(rawSlot)) {
                    event.setCancelled(true);
                    return;
                }
            }
            Bukkit.getScheduler().runTask(guiManager.getPlugin(), () -> {
                if (!sellHolder.isProcessed()) {
                    guiManager.updateSellButton(topInventory);
                }
            });
            return;
        }

        if (holder instanceof ConfirmationGuiHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        Inventory topInventory = event.getInventory();
        InventoryHolder holder = topInventory.getHolder();

        if (holder instanceof SellGuiHolder sellHolder) {
            guiManager.handleSellGuiClose(player, sellHolder);
        } else if (holder instanceof ConfirmationGuiHolder confirmHolder) {
            guiManager.handleConfirmationClose(player, confirmHolder);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        guiManager.handlePlayerQuit(event.getPlayer());
    }
}
