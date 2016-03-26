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

package de.czymm.serversigns.utils;

import de.czymm.serversigns.ServerSignsPlugin;

public class NumberUtils {

    public static boolean isInt(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * @param string - String to convert to int
     * @return int from string, -1 otherwise
     */
    public static int parseInt(String string) {
        int i;
        try {
            i = Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return -1;
        }
        return i;
    }

    public static int parseInt(String string, int def) {
        int i;
        try {
            i = Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return def;
        }
        return i;
    }

    public static double parseDouble(String string) {
        double i;
        try {
            i = Double.parseDouble(string);
        } catch (NumberFormatException e) {
            return -1;
        }
        return i;
    }

    public static int randomBetweenInclusive(int from, int to) {
        if (from == to) {
            return from;
        } else if (to < from) {
            //swap 'from' and 'to'
            from = from^to;
            to = from^to;
            from = from^to;
        }

        return random(from, to + 1);
    }

    public static int random(final int start, final int end) {
        if (end <= start) return start;
        return start + ServerSignsPlugin.r.nextInt(end - start);
    }
}
