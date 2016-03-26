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

package de.czymm.serversigns.commands;

import de.czymm.serversigns.ServerSignsPlugin;
import de.czymm.serversigns.commands.core.SubCommand;
import de.czymm.serversigns.meta.SVSMetaKey;
import de.czymm.serversigns.meta.SVSMetaValue;
import de.czymm.serversigns.translations.Message;
import de.czymm.serversigns.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SubCommandPriceItem extends SubCommand {
    public SubCommandPriceItem(ServerSignsPlugin plugin) {
        super(
                plugin,
                "price_item",
                "pi {hand|0|<item data...>}",
                "Set item requirements for an existing ServerSign",
                "item", "pi", "priceitem"
        );
    }

    @Override
    public void execute(boolean verbose) {
        if (!argSet(0)) {
            if (verbose) sendUsage();
            return;
        }

        ItemStack item;

        if (arg(0).equalsIgnoreCase("hand")) {
            if (player.getItemInHand() == null || player.getItemInHand().getType().equals(Material.AIR)) {
                if (verbose) msg(Message.INVALID_HAND_ITEM);
                return;
            }

            item = player.getItemInHand();
        } else if (arg(0).equalsIgnoreCase("0") || arg(0).equalsIgnoreCase("off")) {
            applyMeta(SVSMetaKey.PRICE_ITEM, new SVSMetaValue(null));
            if (verbose) msg(Message.PRICE_ITEM_REMOVE);
            return;
        } else {
            item = ItemUtils.getItemStackFromString(loopArgs(0));
            if (item == null) {
                if (verbose)
                    msg("Invalid item string - Use this format, in any order: id.<type> [am.<amount>] [du.<durability>] [na.<display name>] [lo.<lore>]... [en.<enchantment>.<level>]...");
                return;
            }
        }

        applyMeta(SVSMetaKey.PRICE_ITEM, new SVSMetaValue(item));
        if (verbose) msg(Message.PRICE_ITEM_BIND);
    }

}
