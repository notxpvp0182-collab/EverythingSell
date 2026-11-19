package com.ahmed.everythingsell.price;

import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public final class SellCalculation {

    private final List<ItemStack> sellableItems;
    private final List<ItemStack> unsellableItems;
    private final int totalSellableQuantity;
    private final double totalValue;

    public SellCalculation(
            List<ItemStack> sellableItems,
            List<ItemStack> unsellableItems,
            int totalSellableQuantity,
            double totalValue
    ) {
        this.sellableItems = Collections.unmodifiableList(sellableItems);
        this.unsellableItems = Collections.unmodifiableList(unsellableItems);
        this.totalSellableQuantity = totalSellableQuantity;
        this.totalValue = totalValue;
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

    public boolean hasSellableItems() {
        return !sellableItems.isEmpty() && totalValue > 0.0D && totalSellableQuantity > 0;
    }
}
