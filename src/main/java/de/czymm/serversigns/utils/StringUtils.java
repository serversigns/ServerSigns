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

import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class StringUtils {

    public static boolean checkIndices(String s, int start, int end) {
        return !(start < 0 || end > s.length());
    }

    public static int count(String s, char c) {
        int r = 0;
        for (int i = 0; i < s.length(); i++) {
            if (c == s.charAt(i))
                r++;
        }
        return r;
    }

    public static int count(String s, char c, int start) {
        return count(s, c, start, s.length());
    }

    public static int count(String s, char c, int start, int end) {
        checkIndices(s, start, end);
        int r = 0;
        for (int i = start; i < end; i++) {
            if (s.charAt(i) == c)
                r++;
        }
        return r;
    }

    public static boolean contains(String s, char c, int start, int end) {
        checkIndices(s, start, end);
        for (int i = start; i < end; i++) {
            if (s.charAt(i) == c)
                return true;
        }
        return false;
    }

    public static String toString(double d, int accuracy) {
        assert accuracy >= 0;
        if (accuracy <= 0)
            return "" + Math.round(d);
        final String s = String.format(Locale.ENGLISH, "%." + accuracy + "f", d);
        int c = s.length() - 1;
        while (s.charAt(c) == '0')
            c--;
        if (s.charAt(c) == '.')
            c--;
        return s.substring(0, c + 1);
    }

    public static String firstToUpper(String s) {
        if (s.isEmpty())
            return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static String fixCapitalization(String string) {
        final char[] s = string.toCharArray();
        int c = 0;
        while (c != -1) {
            while (c < s.length && (s[c] == '.' || s[c] == '!' || s[c] == '?' || Character.isWhitespace(s[c])))
                c++;
            if (c == s.length)
                return new String(s);
            if (c == 0 || Character.isWhitespace(s[c - 1]))
                s[c] = Character.toUpperCase(s[c]);
            c = indexOf(s, c + 1, '.', '!', '?');
        }
        return new String(s);
    }

    private static int indexOf(char[] s, int start, char... cs) {
        for (int i = start; i < s.length; i++) {
            for (final char c : cs)
                if (s[i] == c)
                    return i;
        }
        return -1;
    }

    public static boolean startsWithIgnoreCase(String string, String start) {
        return startsWithIgnoreCase(string, start, 0);
    }

    public static boolean startsWithIgnoreCase(String string, String start, int offset) {
        assert string != null && start != null;
        if (string.length() < offset + start.length())
            return false;
        return string.substring(offset, start.length()).equalsIgnoreCase(start);
    }

    public static String join(Object[] strings) {
        return join(strings, "", 0, strings.length);
    }

    public static String join(Object[] strings, String delimiter) {
        return join(strings, delimiter, 0, strings.length);
    }

    public static String join(Object[] strings, String delimiter, int start, int end) {
        assert strings != null;
        assert start >= 0 && start < end && end <= strings.length : start + "," + end;
        if (start >= strings.length) return "";

        final StringBuilder builder = new StringBuilder();
        for (int i = start; i < end; i++) {
            builder.append(strings[i]);
            builder.append(delimiter);
        }
        return builder.toString().substring(0, builder.toString().length() - delimiter.length());
    }

    public static String join(Iterable<?> strings, String delimiter) {
        return join(strings.iterator(), delimiter);
    }

    public static String join(Iterator<?> strings, String delimiter) {
        assert strings != null;

        StringBuilder builder = new StringBuilder();
        while (strings.hasNext()) {
            builder.append(strings.next());
            builder.append(delimiter);
        }

        return builder.toString().substring(0, builder.toString().length() - delimiter.length());
    }

    public static int findLastDigit(String string, int start) {
        int end = start;
        while (end < string.length() && '0' <= string.charAt(end) && string.charAt(end) <= '9')
            end++;
        return end;
    }

    public static boolean containsAny(String input, String compare) {
        for (int i = 0; i < compare.length(); i++) {
            if (input.indexOf(compare.charAt(i)) != -1)
                return true;
        }
        return false;
    }

    public static boolean containsIgnoreCase(String input, String search) {
        return Pattern.compile(search, Pattern.CASE_INSENSITIVE).matcher(input).find();
    }

    public static boolean containsIgnoreCaseAny(String[] search, String input) {
        for (String regex : search) {
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE); // Make case-insensitive
            Matcher matcher = pattern.matcher(input); //Apply matcher to regex pattern, and provide the input
            if (matcher.find()) {
                return true;
            }
        }
        return false;
    }

    public static boolean anyEqualsIgnoreCase(String search, List<String> rawinput) {
        for (String s : rawinput) {
            if (s.equalsIgnoreCase(search)) {
                return true;
            }
        }
        return false;
    }

    public static boolean equalsIgnoreCaseAny(List<String> search, String rawinput) {
        for (String s : search) {
            if (rawinput.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    public static boolean anyContainsIgnoreCase(String search, List<String> rawinput) {
        for (String input : rawinput) {
            Pattern pattern = Pattern.compile(search, Pattern.CASE_INSENSITIVE); // Make case-insensitive
            Matcher matcher = pattern.matcher(input); //Apply matcher to regex pattern, and provide the input
            if (matcher.find()) {
                return true;
            }
        }
        return true;
    }

    public static boolean containsIgnoreCaseAll(String[] search, String input) {
        for (String regex : search) {
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE); // Make case-insensitive
            Matcher matcher = pattern.matcher(input); //Apply matcher to regex pattern, and provide the input
            if (!matcher.find()) {
                return false;
            }
        }
        return true;
    }

    public static boolean contains(String input, String toFind, boolean caseSensitive) {
        return caseSensitive ? input.contains(toFind) : input.toLowerCase().contains(toFind.toLowerCase());
    }

    public static String replace(final String haystack, final String needle, final String replacement, final boolean caseSensitive) {
        if (caseSensitive)
            return haystack.replace(needle, replacement);
        return haystack.replaceAll("(?ui)" + Pattern.quote(needle), replacement);
    }

    public static boolean booleanFromString(String s) {
        return s.equalsIgnoreCase("true");
    }

    public static String[] alphabetise(String[] list) {
        Arrays.sort(list);
        return list;
    }

    public static boolean compare(String string1, String string2, boolean ignoreCase, boolean ignoreColour) {
        if (ignoreColour) {
            string1 = addRemoveColour(string1);
            string2 = addRemoveColour(string2);
        }
        if (ignoreCase) {
            string1 = string1.toLowerCase();
            string2 = string2.toLowerCase();
        }
        return string1.equals(string2);
    }

    public static String removeNonLetters(String input) {
        return input.replaceAll("[^a-zA-Z ]", "");
    }

    public static String replaceAtIndexes(String input, int startIndex, int endIndex, String replacement) {
        String partA = input.substring(0, startIndex);
        String partB = input.substring(endIndex, input.length());

        return partA + replacement + partB;
    }

    public static String removeSpaces(String command) {
        while (command.startsWith(" ")) {
            command = command.replaceFirst(" ", "");
        }
        return command;
    }

    public static String colour(String string) {
        if (string == null) return null;
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static String strip(String string) {
        if (string == null) return null;
        return ChatColor.stripColor(colour(string));
    }

    public static String decolour(String string) {
        if (string == null) return null;
        return string.replaceAll("(\u00A7([a-z0-9]))", "&$2");
    }

    public static String addRemoveColour(String string) {
        if (string == null) return null;
        return strip(colour(string));
    }
}