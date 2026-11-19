package com.ahmed.everythingsell.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Map;

public final class ItemReturnUtil {

    private ItemReturnUtil() {
    }

    /**
     * Safely returns items to the player's inventory.
     * Any overflow items that do not fit are dropped at the player's location.
     *
     * @return true if any items had to be dropped on the ground due to full inventory
     */
    public static boolean returnItems(Player player, Collection<ItemStack> items) {
        if (player == null || items == null || items.isEmpty()) {
            return false;
        }

        boolean droppedAny = false;
        Location dropLocation = player.getLocation();
        World world = player.getWorld();

        for (ItemStack item : items) {
            if (item == null || item.getType().isAir() || item.getAmount() <= 0) {
                continue;
            }
            Map<Integer, ItemStack> leftovers = player.getInventory().addItem(item.clone());
            if (!leftovers.isEmpty()) {
                droppedAny = true;
                for (ItemStack leftover : leftovers.values()) {
                    if (leftover != null && !leftover.getType().isAir() && leftover.getAmount() > 0) {
                        world.dropItemNaturally(dropLocation, leftover);
                    }
                }
            }
        }
        return droppedAny;
    }
}
