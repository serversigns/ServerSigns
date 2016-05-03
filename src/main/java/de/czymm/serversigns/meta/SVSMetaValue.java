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

package de.czymm.serversigns.meta;

import de.czymm.serversigns.parsing.command.ServerSignCommand;
import de.czymm.serversigns.signs.CancelMode;
import de.czymm.serversigns.signs.ClickType;
import de.czymm.serversigns.signs.ServerSign;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class SVSMetaValue {
    private Object object;

    public SVSMetaValue(Object value) {
        object = value;
    }

    public long asLong() {
        if (object instanceof Long) {
            return (Long) object;
        }

        return Long.MIN_VALUE;
    }

    public int asInt() {
        if (object instanceof Integer) {
            return (Integer) object;
        }

        return Integer.MIN_VALUE;
    }

    public double asDouble() {
        if (object instanceof Double) {
            return (Double) object;
        }

        return Double.MIN_VALUE;
    }

    public String asString() {
        if (object instanceof String) {
            return (String) object;
        }

        return "";
    }

    public boolean asBoolean() {
        if (object instanceof Boolean) {
            return (Boolean) object;
        }

        return false;
    }

    public Object asObject() {
        return object;
    }

    public Location asLocation() {
        if (object instanceof Location) {
            return (Location) object;
        }

        return null;
    }

    public ServerSign asServerSign() {
        if (object instanceof ServerSign) {
            return (ServerSign) object;
        }

        return null;
    }

    public ItemStack asItemStack() {
        if (object instanceof ItemStack) {
            return (ItemStack) object;
        }

        return null;
    }

    public ServerSignCommand asServerSignCommand() {
        if (object instanceof ServerSignCommand) {
            return (ServerSignCommand) object;
        }

        return null;
    }

    public List<String> asStringList() {
        if (object instanceof List<?>) {
            return (List<String>) object;
        }

        return null;
    }

    public ClickType asClickType() {
        if (object instanceof ClickType) {
            return (ClickType) object;
        }

        return null;
    }

    public CancelMode asCancelMode() {
        if (object instanceof CancelMode) {
            return (CancelMode) object;
        }

        return null;
    }
}
