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

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemName extends ItemData implements IItemData {
    private static ItemName i = new ItemName();

    public static ItemName get() {
        return i;
    }

    public ItemName() {
        super(ItemPart.NAME, new String[]{"na.", "name.", "displayname.", "display_name."});
        setApplyToMeta(true);
    }

    @Override
    public ItemStack applyValue(ItemStack item, String value) throws DataException {
        if (item == null) return null;

        ItemMeta meta = applyMetaValue(item.getItemMeta(), value);
        item.setItemMeta(meta);

        return item;
    }

    @Override
    public ItemMeta applyMetaValue(ItemMeta meta, String value) throws DataException {
        if (!value.isEmpty()) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', value).replaceAll("_", " "));
            return meta;
        }

        throw new DataException("Empty strings cannot be used for ItemStack names!");
    }
}
