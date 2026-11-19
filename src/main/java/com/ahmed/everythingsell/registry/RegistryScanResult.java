package com.ahmed.everythingsell.registry;

import org.bukkit.Material;

import java.util.Collections;
import java.util.Map;

public final class RegistryScanResult {

    private final String serverVersion;
    private final Map<String, Material> blocks;
    private final Map<String, Material> items;

    public RegistryScanResult(String serverVersion, Map<String, Material> blocks, Map<String, Material> items) {
        this.serverVersion = serverVersion;
        this.blocks = Collections.unmodifiableMap(blocks);
        this.items = Collections.unmodifiableMap(items);
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public Map<String, Material> getBlocks() {
        return blocks;
    }

    public Map<String, Material> getItems() {
        return items;
    }

    public int getTotalDetected() {
        return blocks.size() + items.size();
    }
}
