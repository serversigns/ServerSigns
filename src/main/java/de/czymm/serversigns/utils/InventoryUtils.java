/*
 * This file is part of ServerSigns.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.czymm.serversigns.utils;

import de.czymm.serversigns.itemdata.ItemSearchCriteria;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;

public class InventoryUtils {
    public static Collection<ItemStack> scan(Inventory inventory, ItemSearchCriteria criteria, boolean removeOnFind, ItemStack... items) {
        return scan(inventory, criteria.getIgnoreDurability(), criteria.getEnchantsCriteria(), criteria.getIgnoreLore(), criteria.getIgnoreName(), removeOnFind, items);
    }

    // Scans inventory, and removes if requested
    // Returns what they don't have, empty if they have it all
    public static Collection<ItemStack> scan(Inventory inventory, boolean ignoreDurability, boolean ignoreEnchants, boolean ignoreLores, boolean ignoreName, boolean removeOnFind, ItemStack... items) {
        Validate.notNull(items, "Items cannot be null");
        Validate.notNull(inventory, "Inventory cannot be null");

        Inventory clone = cloneInventory(inventory);
        HashMap<Integer, ItemStack> leftover = new HashMap<>();

        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i].clone();
            int toDelete = item.getAmount();

            while (toDelete > 0) {
                int first = first(clone, item, true, ignoreDurability, ignoreEnchants, ignoreLores, ignoreName);

                if (first < 0) {
                    item.setAmount(toDelete);
                    leftover.put(i, item);
                    break;
                }

                ItemStack itemStack = clone.getItem(first);
                int amount = itemStack.getAmount();

                if (amount <= toDelete) {
                    toDelete -= amount;
                    clone.clear(first);
                    if (removeOnFind) inventory.clear(first);
                } else {
                    itemStack.setAmount(amount - toDelete);
                    clone.setItem(first, itemStack);
                    if (removeOnFind) inventory.setItem(first, itemStack);
                    toDelete = 0;
                }
            }
        }

        return leftover.values();
    }

    public static int first(Inventory inventory, ItemStack item, boolean ignoreAmount, boolean ignoreDurability, boolean ignoreEnchants, boolean ignoreLores, boolean ignoreName) {
        Validate.notNull(inventory);
        Validate.notNull(item);

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack == null) continue;
            if (ItemUtils.compare(item, stack, false, ignoreDurability, ignoreAmount, ignoreName, ignoreLores, ignoreEnchants))
                return i;
        }
        return -1;
    }

    public static ItemStack[] cloneItemStacks(ItemStack[] itemStacks) {
        ItemStack[] ret = new ItemStack[itemStacks.length];
        for (int i = 0; i < itemStacks.length; i++) {
            ItemStack stack = itemStacks[i];
            if (stack == null) continue;
            ret[i] = new ItemStack(itemStacks[i]);
        }
        return ret;
    }

    public static Inventory cloneInventory(Inventory inventory) {
        if (inventory == null) return null;

        int size = inventory.getSize();
        // Fixed inventories not %9
        int rem = size % 9;
        if (rem > 0) {
            size += 9 - rem;
        }

        InventoryHolder holder = inventory.getHolder();

        Inventory ret = Bukkit.createInventory(holder, size);

        ItemStack[] contents = cloneItemStacks(inventory.getContents());
        ret.setContents(contents);

        return ret;
    }
}
