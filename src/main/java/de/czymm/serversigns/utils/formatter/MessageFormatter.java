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
package de.czymm.serversigns.utils.formatter;

import de.czymm.serversigns.hooks.HookManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public abstract class MessageFormatter {

    public static String toColor(final String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Format a message to send to a player
     *
     * @param player    Player to use for format
     * @param message   Message to format
     * @return Formatted message
     */
    public abstract String format(CommandSender player, String message);

    /**
     * Get the formatter to use to format messages
     *
     * @param hookManager HookManager used to verify placeholder is loaded
     * @return MessageFormatter
     */
    public static MessageFormatter getFormatter(final HookManager hookManager) {
        if (hookManager.placeholderAPI.isHooked())
            return new PlaceholderMessageFormatter();
        return new BasicMessageFormatter();
    }
}
