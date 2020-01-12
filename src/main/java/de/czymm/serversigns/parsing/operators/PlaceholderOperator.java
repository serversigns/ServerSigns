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
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderOperator extends ConditionalOperator {
    private Pattern placeholderPattern = Pattern.compile("(%.+%)([><=])(.+)");

    public PlaceholderOperator() {
        super("placeholder", true);
    }

    @Override
    public ParameterValidityResponse checkParameterValidity(String params) {
        final Matcher matcher = placeholderPattern.matcher(params);
        final boolean isValid = matcher.find() && (matcher.group(2).equals("=") || NumberUtils.isDouble(matcher.group(3)));
        return new ParameterValidityResponse(isValid, "Parameter must be in the format <placeholder>=<match>[|<match>...] or <placeholder>(<|>)<number>");
    }

    @Override
    public boolean meetsConditions(Player executor, ServerSign executingSign, ServerSignsPlugin plugin) {
        if (executor == null) {
            return true; // Console
        }

        if (params == null || !plugin.hookManager.placeholderAPI.isHooked()) {
            return false;
        }

        final Matcher matcher = placeholderPattern.matcher(params);

        if (!matcher.find()) {
            return false;
        }

        final String placeholder = PlaceholderAPI.setPlaceholders(executor, matcher.group(1));
        final String comparator = matcher.group(2);
        final String value = matcher.group(3);

        if ("=".equals(comparator)) {
            final List<String> values = Arrays.asList(value.split("\\|"));
            if (NumberUtils.isDouble(placeholder) && values.stream().allMatch(NumberUtils::isDouble)) {
                final double placeholderDouble = NumberUtils.parseDouble(placeholder);
                return values.stream().map(NumberUtils::parseDouble).anyMatch(number -> placeholderDouble == number);
            } else {
                return values.contains(placeholder);
            }
        }
        return compareDoubles(comparator, placeholder, value);
    }

    /**
     * Compare two string value as double
     * In case of error return false
     *
     * @param comparator Comparator to use to compare double values
     * @param first First double as string
     * @param second Second double as string
     * @return Comparison result
     */
    private Boolean compareDoubles(final String comparator, final String first, final String second) {
        try {
            final Double doubleFirst = Double.parseDouble(first);
            final Double doubleSecond = Double.parseDouble(second);

            switch (comparator) {
                case ">":
                    return doubleFirst > doubleSecond;
                case "<":
                    return doubleFirst < doubleSecond;
                default:
                    return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
