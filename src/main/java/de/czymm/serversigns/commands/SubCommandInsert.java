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
import de.czymm.serversigns.meta.SVSMetaValue;

public class SubCommandInsert extends SubCommand {
    public SubCommandInsert(ServerSignsPlugin plugin) {
        super(
                plugin,
                "insert",
                "insert <line number> <command>",
                "Insert a new command at the given index on an existing ServerSign",
                "insert", "ins"
        );
    }

    @Override
    public void execute(boolean verbose) {
        if (!argSet(1)) {
            if (verbose) sendUsage();
            return;
        }

        int line = argInt(0, -1);
        if (line < 1) {
            if (verbose) sendUsage();
            return;
        }

        String command = loopArgs(1);
        if (!CommandUtils.isCommandSafe(command, plugin, sender)) {
            return;
        }

        CommandUtils.applyServerSignCommandMeta(command, plugin, sender, verbose, SVSMetaKey.INSERT, new SVSMetaValue(line));
    }
}
