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

public class SubCommandXP extends SubCommand {
    public SubCommandXP(ServerSignsPlugin plugin) {
        super(
                plugin,
                "xp",
                "xp <levels>",
                "Set an experience levels cost on an existing ServerSign",
                "xp", "xpprice", "exp"
        );
    }

    @Override
    public void execute(boolean verbose) {
        if (!argSet(0)) {
            if (verbose) sendUsage();
            return;
        }

        int levels = argInt(0, -1);
        if (levels < 0) {
            if (verbose) msg(Message.XP_COST_INVALID);
            return;
        }

        applyMeta(SVSMetaKey.XP, new SVSMetaValue(levels));
        if (verbose) msg(Message.XP_COST_BIND);
    }

}
