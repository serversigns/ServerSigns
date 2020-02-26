package de.czymm.serversigns.utils.formatter;

import de.czymm.serversigns.hooks.HookManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public abstract class MessageFormatter {

    protected String toColor(final String message) {
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
