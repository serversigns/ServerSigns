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

public class SubCommandSetLoops extends SubCommand {
    public SubCommandSetLoops(ServerSignsPlugin plugin) {
        super(
                plugin,
                "set_loops",
                "setloops <loop count> [loop delay (secs)]",
                "Convert an existing ServerSign to a looped ServerSign (60s by default)",
                "setloops", "setl", "setloop"
        );
    }

    @Override
    public void execute(boolean verbose) {
        if (!argSet(0)) {
            if (verbose) sendUsage();
            return;
        }

        if (argInt(1, 60) < 1) {
            if (verbose) msg(Message.DELAY_GREATER_THAN_ZERO);
            if (verbose) sendUsage();
            return;
        }

        applyMeta(SVSMetaKey.LOOP, new SVSMetaValue(argInt(0)), new SVSMetaValue(argInt(1, 60)));
        if (verbose) msg(Message.RIGHT_CLICK_BIND_LOOPS);
    }

}
