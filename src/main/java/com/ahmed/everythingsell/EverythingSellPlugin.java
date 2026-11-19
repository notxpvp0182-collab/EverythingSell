package com.ahmed.everythingsell;

import com.ahmed.everythingsell.command.CommandRegistrar;
import com.ahmed.everythingsell.command.EverythingSellCommand;
import com.ahmed.everythingsell.config.ConfigManager;
import com.ahmed.everythingsell.economy.EconomyManager;
import com.ahmed.everythingsell.gui.GuiManager;
import com.ahmed.everythingsell.listener.GuiListener;
import com.ahmed.everythingsell.listener.ServiceListener;
import com.ahmed.everythingsell.manager.SellManager;
import com.ahmed.everythingsell.manager.SoundManager;
import com.ahmed.everythingsell.price.PriceManager;
import com.ahmed.everythingsell.registry.RegistryScanResult;
import com.ahmed.everythingsell.registry.RegistryScanner;
import com.ahmed.everythingsell.util.ConsoleLogger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class EverythingSellPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private EconomyManager economyManager;
    private RegistryScanner registryScanner;
    private PriceManager priceManager;
    private SoundManager soundManager;
    private SellManager sellManager;
    private GuiManager guiManager;
    private CommandRegistrar commandRegistrar;
    private EverythingSellCommand commandHandler;
    private RegistryScanResult lastScanResult;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.configManager.loadAll();

        this.economyManager = new EconomyManager(this);
        this.economyManager.setupEconomy();

        this.registryScanner = new RegistryScanner();
        this.priceManager = new PriceManager(this, configManager);
        this.soundManager = new SoundManager(this, configManager);
        this.sellManager = new SellManager(this, configManager, economyManager, priceManager, soundManager);
        this.guiManager = new GuiManager(this, configManager, priceManager, sellManager, soundManager);

        synchronizeRegistryAndPrices();

        this.commandHandler = new EverythingSellCommand(this, configManager, guiManager);
        this.commandRegistrar = new CommandRegistrar(this, commandHandler);
        this.commandRegistrar.registerConfiguredCommands(configManager.getCommandsConfig());

        Bukkit.getPluginManager().registerEvents(new GuiListener(guiManager), this);
        Bukkit.getPluginManager().registerEvents(new ServiceListener(this, economyManager), this);

        ConsoleLogger.printStartupBanner(
                this,
                economyManager,
                lastScanResult,
                priceManager
        );
    }

    @Override
    public void onDisable() {
        if (this.guiManager != null) {
            this.guiManager.closeAndReturnAllActiveSessions();
        }
    }

    public synchronized void reloadEverythingSell() {
        if (this.guiManager != null) {
            this.guiManager.closeAndReturnAllActiveSessions();
        }
        this.configManager.loadAll();
        this.economyManager.setupEconomy();
        this.soundManager.validateConfiguredSounds();
        synchronizeRegistryAndPrices();
        if (this.commandRegistrar != null) {
            this.commandRegistrar.registerConfiguredCommands(configManager.getCommandsConfig());
        }
        ConsoleLogger.printStartupBanner(
                this,
                economyManager,
                lastScanResult,
                priceManager
        );
    }

    private void synchronizeRegistryAndPrices() {
        this.lastScanResult = this.registryScanner.scanServerRegistry(configManager.getSettings());
        this.priceManager.synchronizeAndLoad(this.lastScanResult);
        this.soundManager.validateConfiguredSounds();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public PriceManager getPriceManager() {
        return priceManager;
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    public SellManager getSellManager() {
        return sellManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public RegistryScanResult getLastScanResult() {
        return lastScanResult;
    }
}
