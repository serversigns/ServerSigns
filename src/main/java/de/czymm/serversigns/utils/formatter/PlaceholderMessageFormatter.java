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

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlaceholderMessageFormatter extends MessageFormatter {
    /**
     * Format a message to send to a player
     *
     * @param player  Player to use for format
     * @param message Message to format
     * @return Formatted message
     */
    @Override
    public String format(final CommandSender player, final String message) {
        return PlaceholderAPI.setPlaceholders((Player)player, this.toColor(message));
    }
}
