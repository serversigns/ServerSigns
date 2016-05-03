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
import de.czymm.serversigns.signs.ClickType;
import de.czymm.serversigns.signs.ServerSign;
import de.czymm.serversigns.utils.NumberUtils;
import de.czymm.serversigns.utils.TimeUtils;
import org.bukkit.entity.Player;

public class IsAfterOperator extends ConditionalOperator {

    public IsAfterOperator() {
        super("isAfter", true);
    }

    @Override
    public ParameterValidityResponse checkParameterValidity(String params) {
        boolean isValid = true;
        if (params.length() != 13 || !NumberUtils.isInt(params.substring(0, 6)) || !NumberUtils.isInt(params.substring(7))) {
            isValid = false;
        }
        return new ParameterValidityResponse(isValid, "Parameter must be a date & time string in the format DDMMYY,HHMMSS - note the comma separator");
    }

    @Override
    public boolean meetsConditions(Player executor, ServerSign executingSign, ClickType clickType, ServerSignsPlugin plugin) {
        if (params == null) {
            return false;
        }

        long timestamp = TimeUtils.convertDSDDFToEpochMillis(params, plugin.config.getTimeZone());
        return System.currentTimeMillis() >= timestamp;
    }
}
