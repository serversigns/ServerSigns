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

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemType extends ItemData implements IItemData {
    private static ItemType i = new ItemType();

    public static ItemType get() {
        return i;
    }

    public ItemType() {
        super(ItemPart.TYPE, new String[]{"id.", "type.", "identity.", "item."});
    }

    @SuppressWarnings("deprecation")
    @Override
    public ItemStack applyValue(ItemStack item, String value) throws DataException {
        Material mat = Material.getMaterial(value);
        if (mat == null) {
            try {
                mat = Material.getMaterial(Integer.parseInt(value));
            } catch (NumberFormatException ex) {
                return null;
            } // Can't make an item without an ID!
        }
        if (mat == null) {
            throw new DataException(value + " is not a valid ItemStack type!");
        }

        if (item != null) {
            item.setType(mat);
            return item;
        }

        return new ItemStack(mat);
    }

    @Override
    public ItemMeta applyMetaValue(ItemMeta item, String value) throws DataException {
        throw new UnsupportedOperationException();
    }
}
