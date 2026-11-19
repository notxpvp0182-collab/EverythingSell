package com.ahmed.everythingsell.listener;

import com.ahmed.everythingsell.EverythingSellPlugin;
import com.ahmed.everythingsell.economy.EconomyManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;

public final class ServiceListener implements Listener {

    private final EverythingSellPlugin plugin;
    private final EconomyManager economyManager;

    public ServiceListener(EverythingSellPlugin plugin, EconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
    }

    @EventHandler
    public void onServiceRegister(ServiceRegisterEvent event) {
        if (event.getProvider().getService().equals(Economy.class)) {
            if (economyManager.setupEconomy()) {
                plugin.getLogger().info("Connected to Vault Economy provider: " + economyManager.getProviderName());
            }
        }
    }
}
