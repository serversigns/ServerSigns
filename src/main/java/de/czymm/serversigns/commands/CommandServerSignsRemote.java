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
import de.czymm.serversigns.commands.core.CommandException;
import de.czymm.serversigns.commands.core.SubCommand;
import de.czymm.serversigns.meta.SVSMetaManager;
import de.czymm.serversigns.signs.ClickType;
import de.czymm.serversigns.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;

import java.util.UUID;

public class CommandServerSignsRemote extends de.czymm.serversigns.commands.core.Command {

    public CommandServerSignsRemote(ServerSignsPlugin instance) {
        super("serversignsremote", instance);

        addSubCommand(new SubCommandAdd(plugin));
        addSubCommand(new SubCommandCancel(plugin));
        addSubCommand(new SubCommandCancelPermission(plugin));
        addSubCommand(new SubCommandConfirmation(plugin));
        addSubCommand(new SubCommandCreate(plugin));

        addSubCommand(new SubCommandDefaultExecutor(plugin));
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

    @Override
    protected void perform(final String commandLabel, final Command cmd) throws Exception {
        label += " <location> <executor-type>";
        if (!this.argSet(2)) {
            sendHelpMessages();
            msg("&7<location> must be in the format 'world,x,y,z'");
            msg("&7<executor-type> is the click type (left|right)");
            msg("&7&oParameters: &2<required> &c{exact} &9[optional]");
            msg("&7Detailed reference: http://serversigns.de/cmds");
            return;
        }

        Location remoteLocation;
        String rawLoc = arg(0);
        try {
            if (StringUtils.count(rawLoc, ',') >= 3) {
                String[] split = rawLoc.split(",");
                String worldName = "";
                double[] coords = new double[3];
                int i = 2;

                for (int k = split.length - 1; k >= 0; k--) {
                    if (i >= 0) {
                        coords[i--] = Double.parseDouble(split[k]);
                    } else {
                        worldName = StringUtils.join(split, ",", 0, k + 1);
                        break;
                    }
                }

                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    throw new CommandException("");
                }
                remoteLocation = new Location(world, coords[0], coords[1], coords[2]);
            } else {
                throw new CommandException("");
            }
        } catch (NumberFormatException | CommandException ex) {
            msg("Invalid remote location provided. Format is world,x,y,z");
            return;
        }

        String rawClickType = arg(1);
        ClickType clickType;
        try {
            clickType = ClickType.valueOf(rawClickType.toUpperCase());
            if (clickType.equals(ClickType.NONE)) throw new IllegalArgumentException();
        } catch (IllegalArgumentException ex) {
            msg("Invalid executor-type provided. Must be either left or right");
            return;
        }

        SubCommand match = matchSubCommand(arg(2));
        if (match != null) {
            if (!isConsole && !match.hasPermission(player)) {
                sendHelpMessages();
                return;
            }

            match.execute(sender, args.subList(3, args.size()), label, false); // Verbose = false, as it's handled immediately
            // Imitate sender clicking the desired sign if they have meta to use
            UUID id = player == null ? SVSMetaManager.CONSOLE_UUID : player.getUniqueId();
            if (SVSMetaManager.hasMeta(id)) {
                plugin.adminListener.handleAdminInteract(remoteLocation, clickType, sender, id);
            }
            return;
        }

        sendHelpMessages();
        msg("&7<location> must be in the format 'world,x,y,z'");
        msg("&7<executor-type> is the click type (left|right)");
        msg("&7&oParameters: &2<required> &c{exact} &9[optional]");
        msg("&7Detailed reference: http://serversigns.de/cmds");
    }
}