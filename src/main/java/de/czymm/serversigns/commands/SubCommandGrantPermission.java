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

public class SubCommandGrantPermission extends SubCommand {
    public SubCommandGrantPermission(ServerSignsPlugin plugin) {
        super(
                plugin,
                "grant",
                "grant {delete | add} [permission]",
                "Add or remove a granted permission to/from an existing ServerSign",
                "permissiongrant", "permissionsgrant", "pgrant", "grantpermission", "grantpermissions", "grant"
        );
    }

    @Override
    public void execute(boolean verbose) {
        if (!argSet(0)) {
            if (verbose) sendUsage();
            return;
        }

        SVSMeta meta;
        if (arg(0).equalsIgnoreCase("delete")) {
            meta = new SVSMeta(SVSMetaKey.GRANT, new SVSMetaValue(false));
            if (verbose) {
                msg(Message.CLICK_DEL_PERMISSION);
                msg(Message.CLICK_INFO);
            }
        } else if (arg(0).equalsIgnoreCase("add")) {
            if (!argSet(1)) {
                if (verbose) sendUsage();
                return;
            }

            meta = new SVSMeta(SVSMetaKey.GRANT, new SVSMetaValue(true), new SVSMetaValue(arg(1)));
            if (verbose) {
                msg(Message.CLICK_BIND_PERMISSION);
                msg(Message.CLICK_INFO);
            }
        } else {
            if (verbose) sendUsage();
            return;
        }

        SVSMetaManager.setMeta(player, meta);
    }

}
