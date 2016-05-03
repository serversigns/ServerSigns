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
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class NearbyPlayersOperator extends ConditionalOperator {

    public NearbyPlayersOperator() {
        super("nearbyPlayers", true);
    }

    @Override
    public ParameterValidityResponse checkParameterValidity(String params) {

        char operatorChar = getOperatorChar(params);
        if (operatorChar == '¬') {
            return new ParameterValidityResponse(false, "Parameter must be in the format <radius><operator><players> - where <operator> is > < or =");
        }

        String radiusStr = params.split(operatorChar + "")[0];
        String playersStr = params.split(operatorChar + "")[1];

        if (!NumberUtils.isInt(radiusStr)) {
            return new ParameterValidityResponse(false, "Parameter must be in the format <radius><operator><players> - where <radius> is an integer");
        } else if (!NumberUtils.isInt(playersStr)) {
            return new ParameterValidityResponse(false, "Parameter must be in the format <radius><operator><players> - where <players> is an integer");
        }

        return new ParameterValidityResponse(true);
    }

    @Override
    public boolean meetsConditions(Player executor, ServerSign executingSign, ClickType clickType, ServerSignsPlugin plugin) {
        if (params == null) {
            return false;
        }

        char operatorChar = getOperatorChar(params);

        int radius = NumberUtils.parseInt(params.split(operatorChar + "")[0]);
        int players = NumberUtils.parseInt(params.split(operatorChar + "")[1]);

        switch (operatorChar) {
            case '>':
                return getNearbyPlayers(executingSign.getLocation(), radius) > players;
            case '<':
                return getNearbyPlayers(executingSign.getLocation(), radius) < players;
            case '=':
                return getNearbyPlayers(executingSign.getLocation(), radius) == players;
            default:
                return false;
        }
    }

    private char getOperatorChar(String params) {
        if (params.contains(">")) {
            return '>';
        } else if (params.contains("<")) {
            return '<';
        } else if (params.contains("=")) {
            return '=';
        } else {
            return '¬';
        }
    }

    private int getNearbyPlayers(Location location, int radius) {
        int k = 0;
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distance(location) <= radius) {
                k++;
            }
        }
        return k;
    }
}
