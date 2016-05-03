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
import de.czymm.serversigns.signs.ServerSign;
import de.czymm.serversigns.signs.ServerSignExecData;
import de.czymm.serversigns.translations.Message;

import java.util.Iterator;

public class SubCommandResetAllCooldowns extends SubCommand {
    public SubCommandResetAllCooldowns(ServerSignsPlugin plugin) {
        super(
                plugin,
                "reset_all_cooldowns",
                "resetallcd",
                "Reset all ServerSigns cooldowns across all signs",
                "resetallcd", "cdra", "cooldownresetall", "cdresetall", "rcda", "resetcdall", "resetallcooldown"
        );

    }

    @Override
    public void execute(boolean verbose) {
        Iterator<ServerSign> iterator = plugin.serverSignsManager.getSigns().iterator();
        while (iterator.hasNext()) {
            ServerSign sign = iterator.next();
            for (ServerSignExecData execData : sign.getServerSignExecutorData().values()) {
                execData.setLastGlobalUse(0);
                execData.getLastUse().clear();
            }
            plugin.serverSignsManager.save(sign);
        }

        if (verbose) msg(Message.ALL_COOLDOWNS_RESET);
    }
}
