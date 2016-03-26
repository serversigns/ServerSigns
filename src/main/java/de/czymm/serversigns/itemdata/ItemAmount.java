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

package de.czymm.serversigns.itemdata;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemAmount extends ItemData implements IItemData {
    private static ItemAmount i = new ItemAmount();

    public static ItemAmount get() {
        return i;
    }

    public ItemAmount() {
        super(ItemPart.AMOUNT, new String[]{"am.", "amount.", "count."});
    }

    @Override
    public ItemStack applyValue(ItemStack item, String value) throws DataException {
        if (item == null) return null;

        int amount = 0;
        try {
            amount = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new DataException(amount + " is not a valid ItemStack amount (integer)!");
        }

        if (amount < 1)
            throw new DataException(amount + " is not a valid ItemStack amount (must be >= 1)");

        item.setAmount(amount);
        return item;
    }

    @Override
    public ItemMeta applyMetaValue(ItemMeta item, String value) throws DataException {
        throw new UnsupportedOperationException();
    }
}
