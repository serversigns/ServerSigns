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
import de.czymm.serversigns.commands.core.SubCommand;
import de.czymm.serversigns.meta.SVSMetaKey;
import de.czymm.serversigns.meta.SVSMetaManager;
import de.czymm.serversigns.meta.SVSMetaValue;
import de.czymm.serversigns.translations.Message;

public class SubCommandLong extends SubCommand {
    public SubCommandLong(ServerSignsPlugin plugin) {
        super(
                plugin,
                "long",
                "long",
                "Toggle 'long' mode, which allows long commands to be applied to ServerSigns",
                "long", "longcommand", "longcmd"
        );
    }

    @Override
    public void execute(boolean verbose) {
        if (argSet(0)) {
            if (verbose) sendUsage();
            return;
        }

        SVSMetaValue[] values = getMetaValues(SVSMetaKey.LONG);
        if (values != null) {
            // Check to see if it's useless
            if (values.length <= 1) {
                SVSMetaManager.removeMeta(player);
                if (verbose) msg(Message.LONG_CANCELLED);
                return;
            }

            // Already activated, time to collect & pass as if an ADD
            String command = "";
            for (SVSMetaValue val : values)
                command += " " + val.asString();

            command = command.trim();
            if (!CommandUtils.isCommandSafe(command, plugin, sender)) {
                return;
            }

            CommandUtils.applyServerSignCommandMeta(command, plugin, sender, verbose, SVSMetaKey.ADD);
            return;
        }

        applyMeta(SVSMetaKey.LONG, new SVSMetaValue(""));
        if (verbose) msg(Message.LONG_TYPE_TO_CHAT);
    }
}
