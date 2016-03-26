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

import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemColour extends ItemData implements IItemData {
    private static ItemColour i = new ItemColour();

    public static ItemColour get() {
        return i;
    }

    private static final Pattern PATTERN = Pattern.compile("(\\d{1,3}),(\\d{1,3}),(\\d{1,3})");
    private static Matcher MATCHER = null;

    public ItemColour() {
        super(ItemPart.COLOURS, new String[]{"co.", "color.", "colour."});
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
        if (meta == null) return null;

        if (!(meta instanceof LeatherArmorMeta))
            throw new DataException("Leather dye colours cannot be applied to ItemMeta of type '" + meta.getClass().toString() + "'");

        MATCHER = PATTERN.matcher(value);
        int[] colours = {0, 0, 0};

        if (MATCHER.find()) {
            for (int k = 0; k < 3; k++) {
                int cap = Integer.parseInt(MATCHER.group(k + 1));
                if (cap < 0 || cap > 256)
                    throw new DataException(cap + " is not a valid ItemStack Leather dye value! (must be >= 0 && <= 256)");
                colours[k] = cap;
            }
        } else
            throw new DataException("Unable to match Leather dye colour pattern with '" + value + "' - Must take the format co.<red>,<green>,<blue>");

        LeatherArmorMeta lmeta = (LeatherArmorMeta) meta;
        Color color = Color.fromRGB(colours[0], colours[1], colours[2]);
        lmeta.setColor(color);

        return lmeta;
    }
}
