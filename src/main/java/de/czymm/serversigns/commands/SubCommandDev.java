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
import de.czymm.serversigns.legacy.ServerSignConverter;
import org.bukkit.entity.Player;

public class SubCommandDev extends SubCommand {
    public SubCommandDev(ServerSignsPlugin plugin) {
        super(
                plugin,
                "",
                "dev <params>",
                "This command is intended for advanced users only; no documentation is provided.",
                "dev"
        );
    }

    @Override
    public void execute(boolean verbose) {
        if (!argSet(0)) {
            if (verbose) sendUsage();
            return;
        }

        if (argStr(0).equalsIgnoreCase("total")) {
            sender.sendMessage("Total number of ServerSigns loaded: " + plugin.serverSignsManager.getSigns().size());
            return;
        }

        if (argStr(0).equalsIgnoreCase("persist_version")) {
            sender.sendMessage("Current persistence version: " + ServerSignConverter.FILE_VERSION);
            return;
        }

        if (argStr(0).equalsIgnoreCase("hooks")) {
            sender.sendMessage(String.format("Vault: %b, NCP: %b, Ess: %b, PAPI: %b", plugin.hookManager.vault.isHooked(), plugin.hookManager.noCheatPlus.isHooked(), plugin.hookManager.essentials.isHooked(), plugin.hookManager.placeholderAPI.isHooked()));
            return;
        }

        if (verbose) sendUsage();
    }

    @Override
    public boolean hasPermission(Player player) {
        return player.isOp();
    }
}
