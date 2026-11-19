package com.ahmed.everythingsell.gui;

import com.ahmed.everythingsell.price.SellCalculation;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SellSession {

    private final UUID playerId;
    private final List<ItemStack> sellableItems;
    private final List<ItemStack> unsellableItems;
    private final int totalSellableQuantity;
    private final double totalValue;
    private final AtomicBoolean completed = new AtomicBoolean(false);

    public SellSession(UUID playerId, SellCalculation calculation) {
        this.playerId = playerId;
        this.sellableItems = calculation.getSellableItems();
        this.unsellableItems = calculation.getUnsellableItems();
        this.totalSellableQuantity = calculation.getTotalSellableQuantity();
        this.totalValue = calculation.getTotalValue();
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public List<ItemStack> getSellableItems() {
        return sellableItems;
    }

    public List<ItemStack> getUnsellableItems() {
        return unsellableItems;
    }

    public int getTotalSellableQuantity() {
        return totalSellableQuantity;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public boolean tryMarkCompleted() {
        return completed.compareAndSet(false, true);
    }

    public boolean isCompleted() {
        return completed.get();
    }
}
