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

public class SubCommandOption extends SubCommand {

    public SubCommandOption(ServerSignsPlugin plugin) {
        super(
                plugin,
                "option",
                "option <id> {question|add|remove}", // q|a|r
                "Bind player-input option questions & answers to ServerSigns",
                "option", "opt"
        );
    }

    @Override
    public void execute(boolean verbose) {
        if (!argSet(1)) {
            if (verbose) sendUsage();
            return;
        }

        String displayName = arg(0);
        String param1 = arg(1);
        int optionId;
        SVSMetaValue[] values;

        if (param1.equalsIgnoreCase("question") || param1.equalsIgnoreCase("q")) {
            if (!argSet(2)) {
                msg("/" + getLastLabel() + " option " + arg(0) + " " + arg(1) + " <question to ask player>");
                return;
            }
            optionId = 0;
            values = new SVSMetaValue[]{null, null, new SVSMetaValue(loopArgs(2))};
        } else if (param1.equalsIgnoreCase("add") || param1.equalsIgnoreCase("a")) {
            if (!argSet(3)) {
                msg("/" + getLastLabel() + " option " + arg(0) + " " + arg(1) + " <answer label> <description>");
                return;
            }
            optionId = 1;
            values = new SVSMetaValue[]{null, null, new SVSMetaValue(arg(2)), new SVSMetaValue(loopArgs(3))};
        } else if (param1.equalsIgnoreCase("remove") || param1.equalsIgnoreCase("r")) {
            if (!argSet(2)) {
                msg("/" + getLastLabel() + " option " + arg(0) + " " + arg(1) + " <answer label>");
                return;
            }
            optionId = 2;
            values = new SVSMetaValue[]{null, null, new SVSMetaValue(arg(2))};
        } else {
            if (verbose) sendUsage();
            return;
        }

        values[0] = new SVSMetaValue(displayName);
        values[1] = new SVSMetaValue(optionId);

        applyMeta(SVSMetaKey.OPTION, values);

        if (verbose) {
            msg(Message.CLICK_APPLY);
            msg(Message.CLICK_INFO);
        }
    }
}
