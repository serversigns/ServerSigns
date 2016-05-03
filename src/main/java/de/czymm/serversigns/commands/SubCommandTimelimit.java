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
import de.czymm.serversigns.utils.TimeUtils;

public class SubCommandTimelimit extends SubCommand {

    public SubCommandTimelimit(ServerSignsPlugin plugin) {
        super(
                plugin,
                "timelimit",
                "timelimit {<minimum>|@|-} [maximum]",
                "Set the minimum & maximum date/time between which a ServerSign can be used",
                "timelimit", "timel", "tl"
        );
    }

    @Override
    public void execute(boolean verbose) {
        if (!argSet(0)) {
            if (verbose) sendUsage();
            return;
        }

        long min = -1;
        long max = -1;

        if (arg(0).equals("@")) {
            if (!argSet(1)) {
                sendUsage();
                return;
            }
        } else if (arg(0).equals("-")) {
            min = 0;
            max = 0;
        } else if (arg(0).equals("0")) {
            min = 0;
        } else {
            min = TimeUtils.convertDSDDFToEpochMillis(arg(0), plugin.config.getTimeZone());
            if (min == 0) {
                msg(Message.TIMELIMIT_INVALID);
                return;
            }
        }

        if (argSet(1)) {
            if (arg(1).equals("0")) {
                max = 0;
            } else {
                max = TimeUtils.convertDSDDFToEpochMillis(arg(1), plugin.config.getTimeZone());
                if (max == 0) {
                    msg(Message.TIMELIMIT_INVALID);
                    return;
                }
            }
        }

        msg(Message.CLICK_APPLY);
        applyMeta(SVSMetaKey.TIME_LIMIT, new SVSMetaValue(min), new SVSMetaValue(max)); // -1 keep current value | 0 remove current value | >0 set as current value
    }
}
