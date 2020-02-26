package de.czymm.serversigns.utils.formatter;

import org.bukkit.command.CommandSender;

public class BasicMessageFormatter extends MessageFormatter {
    /**
     * Format a message to send to a player
     *
     * @param player  Player to use for format
     * @param message Message to format
     * @return Formatted message
     */
    @Override
    public String format(final CommandSender player, final String message) {
        return this.toColor(message);
    }
}
