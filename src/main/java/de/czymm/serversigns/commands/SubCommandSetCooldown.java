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

public class SubCommandSetCooldown extends SubCommand {
    public SubCommandSetCooldown(ServerSignsPlugin plugin) {
        super(
                plugin,
                "set_cooldown",
                "setcooldown <cooldown>{s|m|h|d|w|mo}",
                "Set the per-player cooldown time for an existing ServerSign",
                "scd", "setcooldown"
        );
    }

    @Override
    public void execute(boolean verbose) {
        if (!argSet(0)) {
            if (verbose) sendUsage();
            return;
        }

        long cooldown = TimeUtils.getLengthFromString(arg(0));
        if (cooldown == 0 && !arg(0).equals("0")) cooldown = -1;
        if (cooldown < 0) {
            if (verbose) msg(Message.NO_NUMBER);
            return;
        }

        applyMeta(SVSMetaKey.SET_COOLDOWN, new SVSMetaValue(cooldown / 1000));
        if (verbose) {
            msg(Message.CLICK_SET_COOLDOWN);
            msg(Message.CLICK_INFO);
        }
    }

}
