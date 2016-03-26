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

public class SubCommandCopy extends SubCommand {
    public SubCommandCopy(ServerSignsPlugin plugin) {
        super(
                plugin,
                "copy",
                "copy [persist]",
                "Copy and paste a ServerSign",
                "copy", "cp"
        );
    }

    @Override
    public void execute(boolean verbose) {
        if (argSet(0) && !arg(0).equalsIgnoreCase("true") && !arg(0).equalsIgnoreCase("false")) {
            if (verbose) sendUsage();
            return;
        }

        if (argSet(0) && arg(0).equalsIgnoreCase("false")) {
            if (SVSMetaManager.hasMeta(player)) {
                SVSMeta meta = SVSMetaManager.getMeta(player);
                if (meta.getValue().asBoolean()) // Persistence is currently on, disable it!
                {
                    SVSMetaManager.removeMeta(player);
                    if (verbose) msg(Message.PERSISTENCE_OFF);
                    return;
                }
            }
        }

        applyMeta(SVSMetaKey.COPY, new SVSMetaValue(argSet(0) ? argBool(0) : false));
        if (argSet(0) && argBool(0) && verbose) msg(Message.PERSISTENCE_ON);
        if (verbose) msg(Message.RIGHT_CLICK_COPY);
    }
}
