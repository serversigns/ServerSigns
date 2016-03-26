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

public class SubCommandConfirmation extends SubCommand {
    public SubCommandConfirmation(ServerSignsPlugin plugin) {
        super(
                plugin,
                "confirmation",
                "confirmation {true | false} [message]",
                "Set whether this sign should ask for confirmation before allowing use",
                "conf", "confirm", "confirmation", "cf"
        );
    }

    @Override
    public void execute(boolean verbose) {
        if (!argSet(0)) {
            if (verbose) sendUsage();
            return;
        }

        boolean value = argBool(0);
        SVSMeta meta = new SVSMeta(SVSMetaKey.CONFIRMATION, new SVSMetaValue(value));
        if (value && argSet(1)) {
            meta.addValue(new SVSMetaValue(loopArgs(1)));
        }

        SVSMetaManager.setMeta(player, meta);
        if (verbose) msg(Message.RIGHT_CLICK_APPLY);
    }

}
