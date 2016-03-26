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

import java.io.Serializable;

public class ItemSearchCriteria implements Serializable {

    private boolean ignoreEnchants;
    private boolean ignoreName;
    private boolean ignoreLore;
    private boolean ignoreDurability;

    public ItemSearchCriteria(boolean ignoreEnchants, boolean ignoreName, boolean ignoreLore, boolean ignoreDurability) {
        this.ignoreEnchants = ignoreEnchants;
        this.ignoreName = ignoreName;
        this.ignoreLore = ignoreLore;
        this.ignoreDurability = ignoreDurability;
    }

    public boolean getEnchantsCriteria() {
        return ignoreEnchants;
    }

    public boolean getIgnoreName() {
        return ignoreName;
    }

    public boolean getIgnoreLore() {
        return ignoreLore;
    }

    public boolean getIgnoreDurability() {
        return ignoreDurability;
    }

    public String getColouredString(ChatColor tru, ChatColor fals) {
        return ((ignoreEnchants ? tru + "Enchants " : fals + "Enchants ") +
                (ignoreName ? tru + "Name " : fals + "Name ") +
                (ignoreLore ? tru + "Lores " : fals + "Lores ") +
                (ignoreDurability ? tru + "Durability " : fals + "Durability "))
                .trim();
    }
}
