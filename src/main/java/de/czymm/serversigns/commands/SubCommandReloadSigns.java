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
import de.czymm.serversigns.translations.Message;

public class SubCommandReloadSigns extends SubCommand {
    public SubCommandReloadSigns(ServerSignsPlugin plugin) {
        super(
                plugin,
                "reload_signs",
                "reloadSigns",
                "Reload all ServerSigns from disk",
                "reloadsigns", "reloadsign", "reloads", "rls", "rlsigns"
        );
    }

    @Override
    public void execute(boolean verbose) {
        if (verbose) {
            if (plugin.serverSignsManager.populateSignsMap(plugin.serverSignsManager.prepareServerSignsSet())) {
                msg(Message.RELOAD_SUCCESS);
            } else {
                msg(Message.RELOAD_SIGNS_FAIL);
            }
        }
    }
}
