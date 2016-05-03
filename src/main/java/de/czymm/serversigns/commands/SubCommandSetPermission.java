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
import de.czymm.serversigns.meta.SVSMeta;
import de.czymm.serversigns.meta.SVSMetaKey;
import de.czymm.serversigns.meta.SVSMetaManager;
import de.czymm.serversigns.meta.SVSMetaValue;
import de.czymm.serversigns.translations.Message;

import java.util.ArrayList;
import java.util.List;

public class SubCommandSetPermission extends SubCommand {
    public SubCommandSetPermission(ServerSignsPlugin plugin) {
        super(
                plugin,
                "set_permission",
                "setperms <perm|-> [perm]... [{m:}message]",
                "For each permission, the user must have 'serversigns.use.<permission>' to use this sign",
                "sp", "setpermission", "setpermissions", "setperms"
        );
    }

    @Override
    public void execute(boolean verbose) {
        if (!argSet(0)) {
            if (verbose) sendUsage();
            return;
        }

        if (arg(0).equals("-")) {
            // Remove permissions
            SVSMetaManager.setMeta(player, new SVSMeta(SVSMetaKey.PERMISSION, new SVSMetaValue(null)));
            if (verbose) {
                msg(Message.CLICK_DEL_PERMISSION);
                msg(Message.CLICK_INFO);
            }
            return;
        }

        StringBuilder message = new StringBuilder();
        List<String> perms = new ArrayList<>();
        boolean b = false;
        for (String arg : args) {
            if (arg.toLowerCase().contains("serversigns.use")) {
                if (verbose)
                    msg("NOTE: permissions will automatically be prefixed with 'serversigns.use.' - you do not need to add it yourself!");
            }
            if (arg.startsWith("m:")) {
                message.append(arg.substring(2));
                b = true;
            } else if (b) {
                message.append(" ").append(arg);
            } else {
                perms.add(arg);
            }
        }

        if (perms.isEmpty() && message.length() > 0) {
            if (verbose) sendUsage();
            return;
        }

        SVSMeta meta = new SVSMeta(SVSMetaKey.PERMISSION, new SVSMetaValue(perms));
        if (message.length() > 0) {
            meta.addValue(new SVSMetaValue(message.toString()));
        }

        SVSMetaManager.setMeta(player, meta);
        if (verbose) {
            msg(Message.CLICK_BIND_PERMISSION);
            msg(Message.CLICK_INFO);
        }
    }
}
