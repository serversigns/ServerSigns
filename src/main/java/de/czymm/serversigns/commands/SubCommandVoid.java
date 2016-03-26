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
import de.czymm.serversigns.meta.SVSMetaManager;

import java.util.UUID;

public class SubCommandVoid extends SubCommand {
    public SubCommandVoid(ServerSignsPlugin plugin) {
        super(
                plugin,
                "void",
                "void",
                "Invalidates any pending actions you may have",
                "void"
        );
    }

    @Override
    public void execute(boolean verbose) {
        UUID id = player == null ? SVSMetaManager.CONSOLE_UUID : player.getUniqueId();
        if (SVSMetaManager.hasMeta(id)) {
            SVSMetaManager.removeMeta(id);
            SVSMetaManager.removeSpecialMeta(id); // Just remove special meta if it exists here, don't check/error
            msg("Success!");
        } else {
            sendUsage();
        }
    }
}
