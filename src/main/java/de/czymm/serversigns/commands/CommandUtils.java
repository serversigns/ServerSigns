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

package de.czymm.serversigns.commands;

import de.czymm.serversigns.ServerSignsPlugin;
import de.czymm.serversigns.meta.SVSMeta;
import de.czymm.serversigns.meta.SVSMetaKey;
import de.czymm.serversigns.meta.SVSMetaManager;
import de.czymm.serversigns.meta.SVSMetaValue;
import de.czymm.serversigns.parsing.CommandParseException;
import de.czymm.serversigns.parsing.ServerSignCommandFactory;
import de.czymm.serversigns.parsing.command.ServerSignCommand;
import de.czymm.serversigns.translations.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class CommandUtils {

    public static boolean isCommandSafe(String command, ServerSignsPlugin plugin, CommandSender executor) {
        String rawCmd = command.trim();
        if (rawCmd.startsWith("*") || rawCmd.startsWith("/"))
            rawCmd = rawCmd.substring(1);
        if (rawCmd.contains(" ")) rawCmd = rawCmd.split(" ")[0];

        if (plugin.config.getBlockedCommands().contains(rawCmd.toLowerCase())) {
            plugin.send(executor, Message.BLOCKED_COMMAND);
            return false;
        }

        return true;
    }

    public static void applyServerSignCommandMeta(String command, ServerSignsPlugin plugin, CommandSender executor, boolean verbose, SVSMetaKey key, SVSMetaValue... precursingValues) {
        try {
            ServerSignCommand cmd = ServerSignCommandFactory.getCommandFromString(command, plugin);
            if (cmd == null) throw new CommandParseException("Unidentified error");

            SVSMetaValue[] values = Arrays.copyOf(precursingValues, precursingValues.length + 1);
            values[values.length - 1] = new SVSMetaValue(cmd);

            SVSMetaManager.setMeta(
                    executor instanceof Player ? ((Player) executor).getUniqueId() : SVSMetaManager.CONSOLE_UUID,
                    new SVSMeta(key, values)
            );
            if (verbose) plugin.send(executor, Message.RIGHT_CLICK_BIND_CMD);
        } catch (CommandParseException ex) {
            if (verbose) plugin.send(executor, plugin.msgHandler.get(Message.INVALID_COMMAND) + " " + ex.getMessage());
        }
    }
}
