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

package de.czymm.serversigns.parsing.operators;

import de.czymm.serversigns.ServerSignsPlugin;
import de.czymm.serversigns.signs.ServerSign;
import org.bukkit.entity.Player;

public class HasPermOperator extends ConditionalOperator {

    public HasPermOperator() {
        super("hasPerm", true);
    }

    @Override
    public ParameterValidityResponse checkParameterValidity(String params) {
        return new ParameterValidityResponse(true); // Anything can be a permission node
    }

    @Override
    public boolean meetsConditions(Player executor, ServerSign executingSign, ServerSignsPlugin plugin) {
        if (params == null) {
            return false;
        }
        if (executor == null) {
            return true; // Console
        }

        return executor.hasPermission(params);
    }
}
