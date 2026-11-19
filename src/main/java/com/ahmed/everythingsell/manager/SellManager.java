package com.ahmed.everythingsell.manager;

import com.ahmed.everythingsell.EverythingSellPlugin;
import com.ahmed.everythingsell.config.ConfigManager;
import com.ahmed.everythingsell.config.MessagesConfig;
import com.ahmed.everythingsell.config.PluginSettings;
import com.ahmed.everythingsell.economy.EconomyManager;
import com.ahmed.everythingsell.gui.SellSession;
import com.ahmed.everythingsell.price.PriceManager;
import com.ahmed.everythingsell.price.SellCalculation;
import com.ahmed.everythingsell.util.ItemReturnUtil;
import com.ahmed.everythingsell.util.NumberUtil;
import com.ahmed.everythingsell.util.TextUtil;
import org.bukkit.entity.Player;

public final class SellManager {

    private final EverythingSellPlugin plugin;
    private final ConfigManager configManager;
    private final EconomyManager economyManager;
    private final PriceManager priceManager;
    private final SoundManager soundManager;

    public SellManager(
            EverythingSellPlugin plugin,
            ConfigManager configManager,
            EconomyManager economyManager,
            PriceManager priceManager,
            SoundManager soundManager
    ) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.economyManager = economyManager;
        this.priceManager = priceManager;
        this.soundManager = soundManager;
    }

    public void executeSale(Player player, SellSession session) {
        if (player == null || session == null || !session.tryMarkCompleted()) {
            return;
        }

        MessagesConfig messages = configManager.getMessages();
        PluginSettings settings = configManager.getSettings();

        // 1. Return any unsellable items that were placed in the GUI alongside sellable items
        if (!session.getUnsellableItems().isEmpty()) {
            boolean droppedUnsellable = ItemReturnUtil.returnItems(player, session.getUnsellableItems());
            messages.send(player, messages.getUnsellableItemsReturned());
            if (droppedUnsellable) {
                messages.send(player, messages.getItemsDroppedFullInventory());
            }
        }

        // 2. Re-verify sellable items and recalculate final price to prevent ghost item exploits
        SellCalculation verifiedCalculation = priceManager.calculate(session.getSellableItems());
        if (!verifiedCalculation.hasSellableItems()) {
            boolean dropped = ItemReturnUtil.returnItems(player, session.getSellableItems());
            messages.send(player, messages.getNoSellableItems());
            if (dropped) {
                messages.send(player, messages.getItemsDroppedFullInventory());
            }
            soundManager.playError(player);
            return;
        }

        // 3. Verify Vault Economy is available before finalizing transaction
        if (!economyManager.isAvailable()) {
            boolean dropped = ItemReturnUtil.returnItems(player, session.getSellableItems());
            messages.send(player, messages.getEconomyUnavailable());
            if (dropped) {
                messages.send(player, messages.getItemsDroppedFullInventory());
            }
            soundManager.playError(player);
            return;
        }

        double totalValue = verifiedCalculation.getTotalValue();
        int totalQuantity = verifiedCalculation.getTotalSellableQuantity();

        // 4. Execute deposit through Vault Economy
        boolean deposited = economyManager.deposit(player, totalValue);
        if (!deposited) {
            boolean dropped = ItemReturnUtil.returnItems(player, session.getSellableItems());
            messages.send(player, messages.getEconomyError());
            if (dropped) {
                messages.send(player, messages.getItemsDroppedFullInventory());
            }
            soundManager.playError(player);
            return;
        }

        // 5. Format placeholders & notify player
        String currency = settings.getCurrencySymbol();
        String formattedTotal = NumberUtil.formatAmount(totalValue);
        String formattedAmountWithCurrency = NumberUtil.formatCurrency(currency, totalValue);

        messages.send(player, applyChatPlaceholders(messages.getSellSuccess(), settings, formattedAmountWithCurrency, formattedTotal, totalQuantity));
        messages.send(player, applyChatPlaceholders(messages.getMoneyReceived(), settings, formattedAmountWithCurrency, formattedTotal, totalQuantity));

        // 6. Display Sell Result Title & Subtitle
        if (settings.isSellResultEnabled()) {
            String title = formatSellResultText(
                    settings.getSellResultTitle(),
                    settings,
                    formattedAmountWithCurrency,
                    formattedTotal,
                    totalQuantity
            );
            String subtitle = formatSellResultText(
                    settings.getSellResultSubtitle(),
                    settings,
                    formattedAmountWithCurrency,
                    formattedTotal,
                    totalQuantity
            );
            player.sendTitle(
                    title,
                    subtitle,
                    settings.getSellResultFadeIn(),
                    settings.getSellResultStay(),
                    settings.getSellResultFadeOut()
            );
        }

        // 7. Play successful sell sound
        soundManager.playSellSuccess(player);
    }

    public void cancelSale(Player player, SellSession session) {
        if (player == null || session == null || !session.tryMarkCompleted()) {
            return;
        }

        MessagesConfig messages = configManager.getMessages();
        boolean droppedSellable = ItemReturnUtil.returnItems(player, session.getSellableItems());
        boolean droppedUnsellable = ItemReturnUtil.returnItems(player, session.getUnsellableItems());

        messages.send(player, messages.getSellCancelled());
        if (droppedSellable || droppedUnsellable) {
            messages.send(player, messages.getItemsDroppedFullInventory());
        }
    }

    public static String formatSellResultText(
            String template,
            PluginSettings settings,
            String formattedCurrencyAmount,
            String formattedRawTotal,
            int itemCount
    ) {
        if (template == null || template.isEmpty()) {
            return "";
        }
        String iconColor = settings.getSellResultIconColor();
        String amountColor = settings.getSellResultAmountColor();
        String icon = settings.getSellResultIcon();
        String currency = settings.getCurrencySymbol();

        String styledIcon = (iconColor == null ? "" : iconColor) + (icon == null ? "✓" : icon) + "&r";
        String styledAmount = (amountColor == null ? "" : amountColor) + formattedCurrencyAmount + "&r";
        String styledTotal = (amountColor == null ? "" : amountColor) + formattedRawTotal + "&r";

        String replaced = template
                .replace("{icon}", styledIcon)
                .replace("{amount}", styledAmount)
                .replace("{total}", styledTotal)
                .replace("{currency}", currency == null ? "$" : currency)
                .replace("{items}", String.valueOf(itemCount));

        return TextUtil.colorize(replaced);
    }

    private String applyChatPlaceholders(
            String template,
            PluginSettings settings,
            String formattedCurrencyAmount,
            String formattedRawTotal,
            int itemCount
    ) {
        if (template == null) {
            return "";
        }
        return template
                .replace("{icon}", settings.getSellResultIcon())
                .replace("{amount}", formattedCurrencyAmount)
                .replace("{total}", formattedRawTotal)
                .replace("{currency}", settings.getCurrencySymbol())
                .replace("{items}", String.valueOf(itemCount));
    }
}
