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
import de.czymm.serversigns.meta.SVSMetaManager;
import de.czymm.serversigns.signs.ClickType;
import de.czymm.serversigns.translations.Message;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ExecutableSVSR {

    private ServerSignsPlugin plugin;

    public ExecutableSVSR(ServerSignsPlugin instance) {
        plugin = instance;

        addSubCommand(new SubCommandAdd(plugin));
        addSubCommand(new SubCommandCancel(plugin));
        addSubCommand(new SubCommandCancelPermission(plugin));
        addSubCommand(new SubCommandConfirmation(plugin));
        addSubCommand(new SubCommandCreate(plugin));

        addSubCommand(new SubCommandEdit(plugin));
        addSubCommand(new SubCommandHeldItemCriteria(plugin));
        addSubCommand(new SubCommandHolding(plugin));
        addSubCommand(new SubCommandInsert(plugin));
        addSubCommand(new SubCommandList(plugin));

        addSubCommand(new SubCommandGrantPermission(plugin));
        addSubCommand(new SubCommandOption(plugin));
        addSubCommand(new SubCommandPriceItem(plugin));
        addSubCommand(new SubCommandPriceItemCriteria(plugin));
        addSubCommand(new SubCommandRemove(plugin));

        addSubCommand(new SubCommandResetCooldowns(plugin));
        addSubCommand(new SubCommandResetCooldown(plugin));
        addSubCommand(new SubCommandSetCooldown(plugin));
        addSubCommand(new SubCommandSetGlobalCooldown(plugin));
        addSubCommand(new SubCommandSetLoops(plugin));

        addSubCommand(new SubCommandSetPermission(plugin));
        addSubCommand(new SubCommandSetPrice(plugin));
        addSubCommand(new SubCommandSetUses(plugin));
        addSubCommand(new SubCommandSilent(plugin));
        addSubCommand(new SubCommandTimelimit(plugin));

        addSubCommand(new SubCommandXP(plugin));
    }

    protected ArrayList<SubCommand> subCommands = new ArrayList<>();

    public void addSubCommand(SubCommand command) {
        subCommands.add(command);
    }

    public SubCommand matchSubCommand(String label) {
        label = label.toLowerCase();
        for (SubCommand sub : subCommands) {
            if (sub.getAliases().contains(label)) {
                return sub;
            }
        }
        return null;
    }

    public void execute(final Location signLocation, final Player player, final ClickType clickType, final String command) throws Exception {
        List<String> parts = Arrays.asList(command.split(" "));

        SubCommand match = matchSubCommand(parts.get(0));
        if (match != null) {
            match.execute(player, parts.subList(1, parts.size()), "", false); // Verbose = false, as it's handled immediately
            // Imitate sender clicking the desired sign if they have meta to use
            UUID id = player == null ? SVSMetaManager.CONSOLE_UUID : player.getUniqueId();
            if (SVSMetaManager.hasMeta(id)) {
                plugin.adminListener.handleAdminInteract(signLocation, clickType, player, id);
            }
        } else {
            plugin.send(player, Message.UNABLE_TO_EXECUTE_CMD, "<string>", command);
        }
    }
}