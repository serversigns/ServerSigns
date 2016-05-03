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
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.HashSet;
import java.util.Iterator;

public class SubCommandResetCooldowns extends SubCommand {
    public SubCommandResetCooldowns(ServerSignsPlugin plugin) {
        super(
                plugin,
                "resetcooldowns",
                "resetcooldowns <player>",
                "Reset all ServerSigns cooldowns for <player>",
                "resetcooldowns", "resetcds", "rcds"
        );

    }

    @Override
    public void execute(boolean verbose) {
        if (!argSet(0)) {
            if (verbose) sendUsage();
            return;
        }

        OfflinePlayer offline = Bukkit.getOfflinePlayer(argStr(0));
        if (offline == null) {
            if (verbose) msg(Message.PLAYER_NOT_FOUND, "<string>", argStr(0));
            return;
        }

        Iterator<ServerSign> it = plugin.serverSignsManager.getSigns().iterator();
        HashSet<ServerSign> toSave = new HashSet<>();
        while (it.hasNext()) {
            ServerSign sign = it.next();

            for (ServerSignExecData execData : sign.getServerSignExecutorData().values()) {
                if (execData.getLastUse(offline.getUniqueId()) > 0) {
                    execData.removeLastUse(offline.getUniqueId());
                    toSave.add(sign);
                }
            }
        }

        if (!toSave.isEmpty()) {
            for (ServerSign sign : toSave) {
                plugin.serverSignsManager.save(sign);
            }
        }

        if (verbose) msg(Message.COOLDOWNS_RESET, "<string>", argStr(0));
    }
}
