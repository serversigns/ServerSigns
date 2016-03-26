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

public class SubCommandAdd extends SubCommand {

    public SubCommandAdd(ServerSignsPlugin plugin) {
        super(
                plugin,
                "add",
                "add [type] [params] <command>",
                "Bind a command to a new or existing ServerSign",
                "add", "a"
        );
    }

    @Override
    public void execute(boolean verbose) {
        if (!argSet(0)) {
            if (verbose) sendUsage();
            return;
        }

        String command = loopArgs(0);
        if (!CommandUtils.isCommandSafe(command, plugin, sender)) {
            return;
        }

        CommandUtils.applyServerSignCommandMeta(command, plugin, sender, verbose, SVSMetaKey.ADD);
    }
}
