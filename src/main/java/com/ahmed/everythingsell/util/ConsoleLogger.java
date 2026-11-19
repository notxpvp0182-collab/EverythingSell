package com.ahmed.everythingsell.util;

import com.ahmed.everythingsell.EverythingSellPlugin;
import com.ahmed.everythingsell.economy.EconomyManager;
import com.ahmed.everythingsell.price.PriceManager;
import com.ahmed.everythingsell.registry.RegistryScanResult;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

public final class ConsoleLogger {

    private ConsoleLogger() {
    }

    public static void printStartupBanner(
            EverythingSellPlugin plugin,
            EconomyManager economyManager,
            RegistryScanResult scanResult,
            PriceManager priceManager
    ) {
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        String line = TextUtil.colorize("&8==================================================");

        console.sendMessage(line);
        console.sendMessage(TextUtil.colorize("&#55FF55ᴇᴠᴇʀʏᴛʜɪɴɢ sᴇʟʟ &7v" + plugin.getDescription().getVersion()));
        console.sendMessage(TextUtil.colorize("&7ᴅᴇᴠᴇʟᴏᴘᴇᴅ ʙʏ &fᴀʜᴍᴇᴅ"));
        console.sendMessage("");
        console.sendMessage(TextUtil.colorize("&#55FF55✓ &fᴘʟᴜɢɪɴ ᴇɴᴀʙʟᴇᴅ"));

        if (economyManager.isAvailable()) {
            console.sendMessage(TextUtil.colorize("&#55FF55✓ &fᴠᴀᴜʟᴛ ᴇᴄᴏɴᴏᴍʏ ᴄᴏɴɴᴇᴄᴛᴇᴅ &7(" + economyManager.getProviderName() + ")"));
        } else {
            console.sendMessage(TextUtil.colorize("&c✗ &fᴠᴀᴜʟᴛ ᴇᴄᴏɴᴏᴍʏ ɴᴏᴛ ᴀᴠᴀɪʟᴀʙʟᴇ"));
        }

        if (scanResult != null) {
            console.sendMessage(TextUtil.colorize("&#55FF55✓ &fʀᴇɢɪsᴛʀʏ sᴄᴀɴ ᴄᴏᴍᴘʟᴇᴛᴇᴅ &7(" + scanResult.getServerVersion() + ")"));
            console.sendMessage(TextUtil.colorize("&#55FF55✓ &fʙʟᴏᴄᴋs ᴅᴇᴛᴇᴄᴛᴇᴅ: &#55FF55" + scanResult.getBlocks().size()));
            console.sendMessage(TextUtil.colorize("&#55FF55✓ &fɪᴛᴇᴍs ᴅᴇᴛᴇᴄᴛᴇᴅ: &#55FF55" + scanResult.getItems().size()));
        }

        if (priceManager != null) {
            console.sendMessage(TextUtil.colorize(
                    "&#55FF55✓ &fᴘʀɪᴄᴇ ғɪʟᴇs sʏɴᴄʜʀᴏɴɪᴢᴇᴅ &7("
                            + priceManager.getTotalLoadedPrices() + " ᴇɴᴛʀɪᴇs, "
                            + priceManager.getActiveSellableCount() + " ᴀᴄᴛɪᴠᴇ > 0)"
            ));
        }

        console.sendMessage(TextUtil.colorize("&#55FF55✓ &fᴇᴠᴇʀʏᴛʜɪɴɢ sᴇʟʟ ɪs ʀᴇᴀᴅʏ"));
        console.sendMessage(line);
    }
}
