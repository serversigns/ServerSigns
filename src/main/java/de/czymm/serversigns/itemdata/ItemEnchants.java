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

import de.czymm.serversigns.utils.ParseUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemEnchants extends ItemData implements IItemData {
    private static ItemEnchants i = new ItemEnchants();

    public static ItemEnchants get() {
        return i;
    }

    private static final Pattern PATTERN = Pattern.compile("^([a-zA-Z_]+)([.](\\d+))*$");
    private static Matcher MATCHER = null;

    public ItemEnchants() {
        super(ItemPart.ENCHANTS, new String[]{"en.", "enchant.", "enchantment.", "ench."});
        setApplyToMeta(true);
    }

    @Override
    public ItemStack applyValue(ItemStack item, String value) throws DataException {
        if (item == null) return null;

        ItemMeta meta = applyMetaValue(item.getItemMeta(), value);
        if (meta == null)
            throw new DataException("Unable to retrieve ItemStack metadata from type '" + item.getType().name() + "'");

        item.setItemMeta(meta);
        return item;
    }

    @Override
    public ItemMeta applyMetaValue(ItemMeta meta, String value) throws DataException {
        if (meta == null) return null;

        MATCHER = PATTERN.matcher(value);
        if (MATCHER.matches()) {
            String enchantmentName = MATCHER.group(1);
            Enchantment enchant = ParseUtils.getEnchantmentByName(enchantmentName);
            if (enchant == null)
                throw new DataException("'" + enchantmentName + "' is not a valid ItemStack enchantment name");

            int level = 1;
            if (MATCHER.groupCount() > 1 && MATCHER.group(3) != null)
                level = Integer.parseInt(MATCHER.group(3));

            meta.addEnchant(enchant, level, true);
            return meta;
        }

        throw new DataException("Unable to match enchantment pattern with '" + value + "' - Must take the format en.<enchant>[.<level>]");
    }
}
