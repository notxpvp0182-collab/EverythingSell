package com.ahmed.everythingsell.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SellGuiHolder implements InventoryHolder {

    private final UUID ownerId;
    private final AtomicBoolean processed = new AtomicBoolean(false);
    private Inventory inventory;

    public SellGuiHolder(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public boolean tryMarkProcessed() {
        return processed.compareAndSet(false, true);
    }

    public boolean isProcessed() {
        return processed.get();
    }
}
