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

public class ItemDamage extends ItemData implements IItemData {
    private static ItemDamage i = new ItemDamage();

    public static ItemDamage get() {
        return i;
    }

    public ItemDamage() {
        super(ItemPart.DAMAGE, new String[]{"du.", "durability.", "damage.", "dura."});
    }

    @Override
    public ItemStack applyValue(ItemStack item, String value) throws DataException {
        if (item == null) return null;

        short damage = 0;
        try {
            damage = (short) Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new DataException(damage + " is not a valid ItemStack durability (integer)!");
        }

        if (damage < 0)
            throw new DataException(damage + " is not a valid ItemStack durability (must be >= 0)");

        item.setDurability(damage);
        return item;
    }

    @Override
    public ItemMeta applyMetaValue(ItemMeta item, String value) throws DataException {
        throw new UnsupportedOperationException();
    }
}
