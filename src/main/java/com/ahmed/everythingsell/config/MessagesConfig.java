package com.ahmed.everythingsell.config;

import com.ahmed.everythingsell.util.TextUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public final class MessagesConfig {

    private final String prefix;
    private final String ready;
    private final String sellSuccess;
    private final String moneyReceived;
    private final String noSellableItems;
    private final String unsellableItemsReturned;
    private final String sellCancelled;
    private final String itemsDroppedFullInventory;
    private final String economyUnavailable;
    private final String economyError;
    private final String playerOnly;
    private final String noPermission;
    private final String reloadSuccess;

    public MessagesConfig(FileConfiguration config) {
        this.prefix = config.getString("prefix", "&#55FF55ᴇᴠᴇʀʏᴛʜɪɴɢ sᴇʟʟ &8» &r");
        this.ready = config.getString("ready", "&aᴇᴠᴇʀʏᴛʜɪɴɢ sᴇʟʟ ɪs ʀᴇᴀᴅʏ.");
        this.sellSuccess = config.getString("sell-success", "&fɪᴛᴇᴍs sᴏʟᴅ sᴜᴄᴄᴇssғᴜʟʟʏ.");
        this.moneyReceived = config.getString("money-received", "&fʏᴏᴜ ʀᴇᴄᴇɪᴠᴇᴅ: &#55FF55{amount} &7(&f{items} ɪᴛᴇᴍs&7)");
        this.noSellableItems = config.getString("no-sellable-items", "&cᴛʜᴇʀᴇ ᴀʀᴇ ɴᴏ sᴇʟʟᴀʙʟᴇ ɪᴛᴇᴍs.");
        this.unsellableItemsReturned = config.getString("unsellable-items-returned", "&eᴜɴsᴇʟʟᴀʙʟᴇ ɪᴛᴇᴍs ᴡᴇʀᴇ ʀᴇᴛᴜʀɴᴇᴅ ᴛᴏ ʏᴏᴜʀ ɪɴᴠᴇɴᴛᴏʀʏ.");
        this.sellCancelled = config.getString("sell-cancelled", "&eᴛʀᴀɴsᴀᴄᴛɪᴏɴ ᴄᴀɴᴄᴇʟʟᴇᴅ. ʏᴏᴜʀ ɪᴛᴇᴍs ᴡᴇʀᴇ ʀᴇᴛᴜʀɴᴇᴅ.");
        this.itemsDroppedFullInventory = config.getString("items-dropped-full-inventory", "&eʏᴏᴜʀ ɪɴᴠᴇɴᴛᴏʀʏ ᴡᴀs ғᴜʟʟ. ʀᴇᴍᴀɪɴɪɴɢ ɪᴛᴇᴍs ᴡᴇʀᴇ ᴅʀᴏᴘᴘᴇᴅ ᴀᴛ ʏᴏᴜʀ ғᴇᴇᴛ.");
        this.economyUnavailable = config.getString("economy-unavailable", "&cᴇᴄᴏɴᴏᴍʏ sʏsᴛᴇᴍ ɪs ɴᴏᴛ ᴀᴠᴀɪʟᴀʙʟᴇ.");
        this.economyError = config.getString("economy-error", "&cᴀɴ ᴇʀʀᴏʀ ᴏᴄᴄᴜʀʀᴇᴅ ᴡʜɪʟᴇ ᴅᴇᴘᴏsɪᴛɪɴɢ ʏᴏᴜʀ ᴍᴏɴᴇʏ. ɪᴛᴇᴍs ʀᴇᴛᴜʀɴᴇᴅ.");
        this.playerOnly = config.getString("player-only", "&cᴏɴʟʏ ᴘʟᴀʏᴇʀs ᴄᴀɴ ᴏᴘᴇɴ ᴛʜᴇ sᴇʟʟ ɢᴜɪ.");
        this.noPermission = config.getString("no-permission", "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪssɪᴏɴ ᴛᴏ ᴜsᴇ ᴛʜɪs ᴄᴏᴍᴍᴀɴᴅ.");
        this.reloadSuccess = config.getString("reload-success", "&#55FF55ᴇᴠᴇʀʏᴛʜɪɴɢ sᴇʟʟ ᴄᴏɴғɪɢᴜʀᴀᴛɪᴏɴ ᴀɴᴅ ᴘʀɪᴄᴇs ʀᴇʟᴏᴀᴅᴇᴅ sᴜᴄᴄᴇssғᴜʟʟʏ.");
    }

    public void send(CommandSender sender, String rawMessage) {
        if (sender == null || rawMessage == null || rawMessage.isBlank()) {
            return;
        }
        sender.sendMessage(TextUtil.colorize(prefix + rawMessage));
    }

    public String getPrefix() {
        return prefix;
    }

    public String getReady() {
        return ready;
    }

    public String getSellSuccess() {
        return sellSuccess;
    }

    public String getMoneyReceived() {
        return moneyReceived;
    }

    public String getNoSellableItems() {
        return noSellableItems;
    }

    public String getUnsellableItemsReturned() {
        return unsellableItemsReturned;
    }

    public String getSellCancelled() {
        return sellCancelled;
    }

    public String getItemsDroppedFullInventory() {
        return itemsDroppedFullInventory;
    }

    public String getEconomyUnavailable() {
        return economyUnavailable;
    }

    public String getEconomyError() {
        return economyError;
    }

    public String getPlayerOnly() {
        return playerOnly;
    }

    public String getNoPermission() {
        return noPermission;
    }

    public String getReloadSuccess() {
        return reloadSuccess;
    }
}
