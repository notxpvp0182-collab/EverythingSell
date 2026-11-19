package com.ahmed.everythingsell.economy;

import com.ahmed.everythingsell.EverythingSellPlugin;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Level;

public final class EconomyManager {

    private final EverythingSellPlugin plugin;
    private Economy economy;

    public EconomyManager(EverythingSellPlugin plugin) {
        this.plugin = plugin;
    }

    public synchronized boolean setupEconomy() {
        this.economy = null;

        if (!plugin.getConfigManager().getSettings().isEconomyEnabled()) {
            return false;
        }

        Plugin vaultPlugin = Bukkit.getPluginManager().getPlugin("Vault");
        if (vaultPlugin == null) {
            return false;
        }

        try {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                return false;
            }
            this.economy = rsp.getProvider();
            return this.economy != null;
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING, "Failed to hook into Vault Economy provider.", t);
            this.economy = null;
            return false;
        }
    }

    public synchronized boolean isAvailable() {
        if (!plugin.getConfigManager().getSettings().isEconomyEnabled()) {
            return false;
        }
        if (this.economy == null) {
            setupEconomy();
        }
        return this.economy != null && this.economy.isEnabled();
    }

    public synchronized String getProviderName() {
        if (this.economy == null) {
            return "None";
        }
        try {
            return this.economy.getName();
        } catch (Throwable ignored) {
            return "Vault";
        }
    }

    public synchronized boolean deposit(Player player, double amount) {
        if (player == null || !Double.isFinite(amount) || amount <= 0.0D) {
            return false;
        }
        if (!isAvailable()) {
            return false;
        }

        try {
            EconomyResponse response = this.economy.depositPlayer(player, amount);
            if (response == null) {
                return false;
            }
            if (!response.transactionSuccess() && plugin.getConfigManager().getSettings().isDebugEnabled()) {
                plugin.getLogger().warning("[Debug] Vault deposit failed for " + player.getName() + ": " + response.errorMessage);
            }
            return response.transactionSuccess();
        } catch (Throwable t) {
            plugin.getLogger().log(Level.SEVERE, "Exception occurred while depositing money through Vault for " + player.getName(), t);
            return false;
        }
    }
}
