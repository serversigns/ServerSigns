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

import java.util.HashSet;
import java.util.Set;

public abstract class ConditionalOperator {

    public static final Set<ConditionalOperator> VALUES = new HashSet<>();

    static {
        VALUES.add(new HasPermOperator());
        VALUES.add(new IsOpOperator());
        VALUES.add(new LoopIsOperator());
        VALUES.add(new RandomOperator());
        VALUES.add(new UsesTallyIsOperator());
        VALUES.add(new IsBeforeOperator());
        VALUES.add(new IsAfterOperator());
        VALUES.add(new CheckOptionOperator());
        VALUES.add(new NearbyPlayersOperator());
        VALUES.add(new OnlinePlayersOperator());
        VALUES.add(new HasBalanceOperator());
        VALUES.add(new PlaceholderOperator());
        VALUES.add(new ScoreboardOperator());
    }

    protected String key;
    protected String params;
    protected boolean reqParams;
    protected boolean negative = false;

    public ConditionalOperator(String key, boolean requireParams) {
        this.key = key;
        this.reqParams = requireParams;
    }

    public String getKey() {
        return key;
    }

    public boolean isKey(String input) {
        return input.equalsIgnoreCase(key);
    }

    public void setNegative(boolean val) {
        negative = val;
    }

    public boolean isNegative() {
        return negative;
    }

    public abstract ParameterValidityResponse checkParameterValidity(String params);

    public void passParameters(String params) {
        if (!reqParams || !checkParameterValidity(params).isValid()) return;
        this.params = params;
    }

    public boolean evaluate(Player executor, ServerSign executingSign, ServerSignsPlugin plugin) {
        //Exclusive OR, return true if either:
        // - condition is true and negation is false
        // - condition is false and negation is true
        return isNegative() ^ meetsConditions(executor, executingSign, plugin);
    }

    protected abstract boolean meetsConditions(Player executor, ServerSign executingSign, ServerSignsPlugin plugin);

    public ConditionalOperator newInstance() {
        try {
            return getClass().newInstance();
        } catch (IllegalAccessException | InstantiationException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static class ParameterValidityResponse {
        boolean valid;
        String message;

        public ParameterValidityResponse(boolean isValid, String message) {
            this.valid = isValid;
            this.message = message;
        }

        public ParameterValidityResponse(boolean isValid) {
            this.valid = isValid;
            this.message = "";
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}
