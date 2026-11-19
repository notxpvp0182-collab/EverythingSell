package com.ahmed.everythingsell.registry;

import com.ahmed.everythingsell.config.PluginSettings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public final class RegistryScanner {

    private static final Set<String> TECHNICAL_MATERIALS = Set.of(
            "air",
            "cave_air",
            "void_air",
            "barrier",
            "command_block",
            "chain_command_block",
            "repeating_command_block",
            "command_block_minecart",
            "structure_block",
            "structure_void",
            "jigsaw",
            "debug_stick",
            "knowledge_book",
            "light",
            "petrified_oak_slab"
    );

    public RegistryScanResult scanServerRegistry(PluginSettings settings) {
        String serverVersion = detectServerVersion();
        Map<String, Material> detectedBlocks = new TreeMap<>();
        Map<String, Material> detectedItems = new TreeMap<>();

        Iterable<Material> materialIterable = getRegisteredMaterials();
        for (Material material : materialIterable) {
            if (!isValidSellableCandidate(material, settings)) {
                continue;
            }
            String key = extractMaterialKey(material);
            if (classify(material) == MaterialCategory.BLOCK) {
                detectedBlocks.put(key, material);
            } else {
                detectedItems.put(key, material);
            }
        }

        return new RegistryScanResult(serverVersion, detectedBlocks, detectedItems);
    }

    public static boolean isValidSellableCandidate(Material material, PluginSettings settings) {
        if (material == null) {
            return false;
        }
        try {
            if (material.isAir() || material.isLegacy()) {
                return false;
            }
            if (!material.isItem()) {
                return false;
            }
        } catch (Throwable ignored) {
            return false;
        }

        String key = extractMaterialKey(material);
        if (settings != null) {
            if (settings.getExcludedMaterials().contains(key)) {
                return false;
            }
            if (settings.isExcludeTechnicalMaterials() && TECHNICAL_MATERIALS.contains(key)) {
                return false;
            }
        }
        return true;
    }

    public static MaterialCategory classify(Material material) {
        return material.isBlock() ? MaterialCategory.BLOCK : MaterialCategory.ITEM;
    }

    public static String extractMaterialKey(Material material) {
        try {
            return material.getKey().getKey().toLowerCase(Locale.ROOT);
        } catch (Throwable ignored) {
            return material.name().toLowerCase(Locale.ROOT);
        }
    }

    private Iterable<Material> getRegisteredMaterials() {
        try {
            if (Registry.MATERIAL != null) {
                return Registry.MATERIAL;
            }
        } catch (Throwable ignored) {
        }
        return Arrays.asList(Material.values());
    }

    private String detectServerVersion() {
        try {
            String bukkitVersion = Bukkit.getBukkitVersion();
            if (bukkitVersion != null && !bukkitVersion.isBlank()) {
                int dashIndex = bukkitVersion.indexOf('-');
                return "MC " + (dashIndex > 0 ? bukkitVersion.substring(0, dashIndex) : bukkitVersion);
            }
        } catch (Throwable ignored) {
        }
        return "Minecraft Server";
    }
}
