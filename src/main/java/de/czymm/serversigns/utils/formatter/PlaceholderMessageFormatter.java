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
