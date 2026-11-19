package com.ahmed.everythingsell;

import com.ahmed.everythingsell.config.CommandsConfig;
import com.ahmed.everythingsell.config.PluginSettings;
import com.ahmed.everythingsell.manager.SellManager;
import com.ahmed.everythingsell.price.PriceManager;
import com.ahmed.everythingsell.price.SellCalculation;
import com.ahmed.everythingsell.registry.MaterialCategory;
import com.ahmed.everythingsell.registry.RegistryScanner;
import com.ahmed.everythingsell.util.NumberUtil;
import com.ahmed.everythingsell.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EverythingSellTest {

    @Test
    void testNumberFormatting() {
        assertEquals("500", NumberUtil.formatAmount(500.0));
        assertEquals("1,250", NumberUtil.formatAmount(1250.0));
        assertEquals("12.50", NumberUtil.formatAmount(12.5));
        assertEquals("$750", NumberUtil.formatCurrency("$", 750.0));
        assertEquals("$1,250.75", NumberUtil.formatCurrency("$", 1250.75));
    }

    @Test
    void testSmallCapsConversionAndColorize() {
        assertEquals("ᴇᴠᴇʀʏᴛʜɪɴɢ sᴇʟʟ", TextUtil.toSmallCaps("EVERYTHING SELL"));
        assertEquals("ᴄᴏɴғɪʀᴍ sᴇʟʟ", TextUtil.toSmallCaps("Confirm Sell"));
        assertEquals("§a{amount} ɪᴛᴇᴍs", TextUtil.toSmallCaps("§a{amount} items"));

        String hexColored = TextUtil.colorize("#55FF55✓");
        assertTrue(hexColored.startsWith("§x§5§5§F§F§5§5"));
        assertTrue(hexColored.endsWith("✓"));
    }

    @Test
    void testPriceSynchronizationPreservesExistingPrices() {
        YamlConfiguration blocksYaml = new YamlConfiguration();
        blocksYaml.set("blocks.diamond_block.sell-price", 999.0);

        Map<String, Material> detectedBlocks = new LinkedHashMap<>();
        detectedBlocks.put("diamond_block", Material.DIAMOND_BLOCK);
        detectedBlocks.put("stone", Material.STONE);

        boolean modifiedFirstRun = PriceManager.synchronizeSection(blocksYaml, "blocks", detectedBlocks, 0.0);
        assertTrue(modifiedFirstRun, "Should add missing stone entry");
        assertEquals(999.0, blocksYaml.getDouble("blocks.diamond_block.sell-price"), "Existing admin price must be preserved");
        assertEquals(0.0, blocksYaml.getDouble("blocks.stone.sell-price"), "Newly detected material should use default price");

        boolean modifiedSecondRun = PriceManager.synchronizeSection(blocksYaml, "blocks", detectedBlocks, 5.0);
        assertFalse(modifiedSecondRun, "Should not modify file when all entries already exist");
        assertEquals(999.0, blocksYaml.getDouble("blocks.diamond_block.sell-price"));
        assertEquals(0.0, blocksYaml.getDouble("blocks.stone.sell-price"));
    }

    @Test
    void testSellCalculationWithMultipleStacksAndUnsellableItems() {
        Map<Material, Double> priceMap = new EnumMap<>(Material.class);
        priceMap.put(Material.DIAMOND, 50.0);
        priceMap.put(Material.IRON_INGOT, 5.0);
        priceMap.put(Material.DIRT, 0.0);

        List<ItemStack> placed = List.of(
                new ItemStack(Material.DIAMOND, 10),     // 10 * 50.0 = 500.0
                new ItemStack(Material.IRON_INGOT, 20),  // 20 * 5.0  = 100.0
                new ItemStack(Material.DIRT, 64),        // 0.0 -> unsellable
                new ItemStack(Material.STICK, 16)        // unconfigured -> unsellable
        );

        SellCalculation calculation = PriceManager.calculateWithPrices(placed, priceMap);

        assertTrue(calculation.hasSellableItems());
        assertEquals(600.0, calculation.getTotalValue(), 0.001);
        assertEquals(30, calculation.getTotalSellableQuantity());
        assertEquals(2, calculation.getSellableItems().size());
        assertEquals(2, calculation.getUnsellableItems().size());
    }

    @Test
    void testRegistryCandidateFilteringAndClassification() {
        YamlConfiguration rawConfig = new YamlConfiguration();
        rawConfig.set("registry.exclude-technical-materials", true);
        rawConfig.set("registry.excluded-materials", List.of("BEDROCK"));
        PluginSettings settings = new PluginSettings(rawConfig);

        assertFalse(RegistryScanner.isValidSellableCandidate(Material.AIR, settings));
        assertFalse(RegistryScanner.isValidSellableCandidate(Material.WATER, settings));
        assertFalse(RegistryScanner.isValidSellableCandidate(Material.COMMAND_BLOCK, settings));
        assertFalse(RegistryScanner.isValidSellableCandidate(Material.BEDROCK, settings));

        assertTrue(RegistryScanner.isValidSellableCandidate(Material.STONE, settings));
        assertEquals(MaterialCategory.BLOCK, RegistryScanner.classify(Material.STONE));

        assertTrue(RegistryScanner.isValidSellableCandidate(Material.DIAMOND, settings));
        assertEquals(MaterialCategory.ITEM, RegistryScanner.classify(Material.DIAMOND));
    }

    @Test
    void testSellResultTitleAndSubtitleFormatting() {
        YamlConfiguration rawConfig = new YamlConfiguration();
        rawConfig.set("prices.currency-symbol", "$");
        rawConfig.set("sell-result.enabled", true);
        rawConfig.set("sell-result.title", "ᴠɪᴇᴡ sᴇʟʟ");
        rawConfig.set("sell-result.subtitle", "{icon} {amount} sᴇʟᴅ");
        rawConfig.set("sell-result.icon", "◆");
        rawConfig.set("sell-result.icon-color", "#55FF55");
        rawConfig.set("sell-result.amount-color", "#55FF55");

        PluginSettings settings = new PluginSettings(rawConfig);

        String title = SellManager.formatSellResultText(
                settings.getSellResultTitle(),
                settings,
                "$750",
                "750",
                15
        );
        String subtitle = SellManager.formatSellResultText(
                settings.getSellResultSubtitle(),
                settings,
                "$750",
                "750",
                15
        );

        assertEquals("ᴠɪᴇᴡ sᴇʟʟ", title);
        assertTrue(subtitle.contains("◆"));
        assertTrue(subtitle.contains("$750"));
        assertTrue(subtitle.contains("sᴇʟᴅ"));
    }

    @Test
    void testCommandsConfigSanitization() {
        YamlConfiguration raw = new YamlConfiguration();
        raw.set("command", "/esell");
        raw.set("aliases", List.of("sell", "everythingsell"));
        raw.set("reload-argument", "reload");

        CommandsConfig commandsConfig = new CommandsConfig(raw);
        assertEquals("esell", commandsConfig.getPrimaryCommand());
        assertEquals(List.of("sell", "everythingsell"), commandsConfig.getAliases());
        assertEquals("reload", commandsConfig.getReloadArgument());
    }
}
