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
import de.czymm.serversigns.utils.NumberUtils;
import org.bukkit.entity.Player;

import java.util.Random;

public class RandomOperator extends ConditionalOperator {

    private static final Random RANDOM = new Random();

    public RandomOperator() {
        super("random", true);
    }

    @Override
    public ParameterValidityResponse checkParameterValidity(String params) {
        if (!params.endsWith("%")) {
            return new ParameterValidityResponse(false, "Parameter must be a percentage");
        }

        String value = params.substring(0, params.length() - 1);
        if (!NumberUtils.isDouble(value)) {
            return new ParameterValidityResponse(false, "Parameter must be a numeral percentage such as 25% or 0.01%");
        }

        return new ParameterValidityResponse(true);
    }

    @Override
    public boolean meetsConditions(Player executor, ServerSign executingSign, ServerSignsPlugin plugin) {
        if (params == null) {
            return false;
        }

        double percentChance = NumberUtils.parseDouble(params.substring(0, params.length() - 1));
        return RANDOM.nextDouble() < (percentChance / 100); // Normalise % to < 1.0 equivalent
    }
}
