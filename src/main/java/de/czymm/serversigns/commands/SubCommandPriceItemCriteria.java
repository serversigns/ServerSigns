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

public class SubCommandPriceItemCriteria extends SubCommand {
    public SubCommandPriceItemCriteria(ServerSignsPlugin plugin) {
        super(
                plugin,
                "price_item_criteria",
                "pic <enchants> <lores> <name> <durability>",
                "Set whether the listed item attributes should be ignored in price item checks on an existing ServerSign (true/false)",
                "pic", "priceitemcriteria", "priceitemcrit", "picrit", "picriteria"
        );
    }

    @Override
    public void execute(boolean verbose) {
        if (!argSet(3)) {
            if (verbose) sendUsage();
            return;
        }

        // Sample
        if (!arg(0).equalsIgnoreCase("true") && !arg(0).equalsIgnoreCase("false")) {
            if (verbose) msg(Message.ITEM_CRITERIA_BOOLEAN);
            return;
        }

        applyMeta(SVSMetaKey.PRICE_ITEM_CRITERIA, new SVSMetaValue(argBool(0, true)), new SVSMetaValue(argBool(1, true)), new SVSMetaValue(argBool(2, true)), new SVSMetaValue(argBool(3, true)));
        if (verbose) msg(Message.PRICE_ITEM_CRITERIA_BIND);
    }
}
