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
import de.czymm.serversigns.translations.Message;

public class SubCommandSetUses extends SubCommand {
    public SubCommandSetUses(ServerSignsPlugin plugin) {
        super(
                plugin,
                "set_uses",
                "setuses {0|<number of uses>}",
                "Limit this ServerSign to X number of uses",
                "setuses", "setuse", "uses"
        );
    }

    @Override
    public void execute(boolean verbose) {
        if (!argSet(0)) {
            if (verbose) sendUsage();
            return;
        }

        int uses = argInt(0, -1);
        if (uses < 0) {
            if (verbose) msg(Message.USES_GREATER_ZERO);
            return;
        }

        applyMeta(SVSMetaKey.USES, new SVSMetaValue(uses));

        if (verbose) {
            if (uses == 0) msg(Message.RIGHT_CLICK_REMOVE_USES);
            else msg(Message.RIGHT_CLICK_SET_USES);
        }
    }
}
