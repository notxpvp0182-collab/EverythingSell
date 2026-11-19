package com.ahmed.everythingsell.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ConfirmationGuiHolder implements InventoryHolder {

    private final UUID ownerId;
    private final SellSession session;
    private final AtomicBoolean resolved = new AtomicBoolean(false);
    private Inventory inventory;

    public ConfirmationGuiHolder(UUID ownerId, SellSession session) {
        this.ownerId = ownerId;
        this.session = session;
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

    public SellSession getSession() {
        return session;
    }

    public boolean tryMarkResolved() {
        return resolved.compareAndSet(false, true);
    }

    public boolean isResolved() {
        return resolved.get();
    }
}
