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
import de.czymm.serversigns.meta.SVSMeta;
import de.czymm.serversigns.meta.SVSMetaKey;
import de.czymm.serversigns.meta.SVSMetaManager;
import de.czymm.serversigns.signs.ClickType;
import de.czymm.serversigns.signs.ServerSign;
import org.bukkit.command.Command;

import java.util.UUID;

public class CommandServerSigns extends de.czymm.serversigns.commands.core.Command {

    public CommandServerSigns(ServerSignsPlugin instance) {
        super("serversigns", instance);

        addSubCommand(new SubCommandAdd(plugin));
        addSubCommand(new SubCommandCancel(plugin));
        addSubCommand(new SubCommandCancelPermission(plugin));
        addSubCommand(new SubCommandConfirmation(plugin));
        addSubCommand(new SubCommandCopy(plugin));

        addSubCommand(new SubCommandCreate(plugin));
        addSubCommand(new SubCommandDefaultExecutor(plugin));
        addSubCommand(new SubCommandDev(plugin));
        addSubCommand(new SubCommandEdit(plugin));
        addSubCommand(new SubCommandHeldItemCriteria(plugin));

        addSubCommand(new SubCommandHolding(plugin));
        addSubCommand(new SubCommandImport(plugin));
        addSubCommand(new SubCommandInsert(plugin));
        addSubCommand(new SubCommandList(plugin));
        addSubCommand(new SubCommandLong(plugin));

        addSubCommand(new SubCommandGrantPermission(plugin));
        addSubCommand(new SubCommandOption(plugin));
        addSubCommand(new SubCommandPriceItem(plugin));
        addSubCommand(new SubCommandPriceItemCriteria(plugin));
        addSubCommand(new SubCommandReload(plugin));

        addSubCommand(new SubCommandReloadConfig(plugin));
        addSubCommand(new SubCommandReloadSigns(plugin));
        addSubCommand(new SubCommandRemove(plugin));
        addSubCommand(new SubCommandResetAllCooldowns(plugin));
        addSubCommand(new SubCommandResetCooldowns(plugin));

        addSubCommand(new SubCommandResetCooldown(plugin));
        addSubCommand(new SubCommandSelect(plugin));
        addSubCommand(new SubCommandSetCooldown(plugin));
        addSubCommand(new SubCommandSetGlobalCooldown(plugin));
        addSubCommand(new SubCommandSetLoops(plugin));

        addSubCommand(new SubCommandSetPermission(plugin));
        addSubCommand(new SubCommandSetPrice(plugin));
        addSubCommand(new SubCommandSetUses(plugin));
        addSubCommand(new SubCommandSilent(plugin));
        addSubCommand(new SubCommandTimelimit(plugin));

        addSubCommand(new SubCommandVoid(plugin));
        addSubCommand(new SubCommandXP(plugin));
    }

    @Override
    protected void perform(final String commandLabel, final Command cmd) throws Exception {
        if (!this.argSet(0)) {
            sendHelpMessages();
            msg("&7&oParameters: &2<required> &c{exact} &9[optional]");
            msg("&7Detailed reference: http://serversigns.de/cmds");
            return;
        }
        String arg0 = this.arg(0);

        if (arg0.equalsIgnoreCase("yes")) {
            if (isConsole) throw new CommandException("This command cannot be performed from console");
            if (SVSMetaManager.hasMeta(player)) {
                SVSMeta meta = SVSMetaManager.getMeta(player);
                if (meta.getKey().equals(SVSMetaKey.YES)) {
                    ServerSign sign = meta.getValue().asServerSign();
                    ClickType clickType = meta.getValue(1).asClickType();
                    plugin.serverSignExecutor.executeSignFull(player, sign, clickType, null);
                    SVSMetaManager.removeMeta(player);
                }
            }
            return; // Silently exit
        }

        SubCommand match = matchSubCommand(arg0);
        if (match != null) {
            if (isConsole) {
                ServerSignsPlugin.log("WARNING: /svs commands are not designed for Console use - errors may occur!");
            } else if (!match.hasPermission(player)) {
                sendHelpMessages();
                return;
            }

            match.execute(sender, args.subList(1, args.size()), commandLabel, true);
            // Imitate sender clicking the desired sign if they have SELECT special meta assigned
            UUID id = player == null ? SVSMetaManager.CONSOLE_UUID : player.getUniqueId();
            if (SVSMetaManager.hasSpecialMeta(id, SVSMetaKey.SELECT) && SVSMetaManager.hasMeta(id)) { // They should have normal meta too!!
                plugin.adminListener.handleAdminInteract(SVSMetaManager.getSpecialMeta(id).getValue().asLocation(), ClickType.RIGHT, sender, id);
            }
            return;
        }

        sendHelpMessages();
        msg("&7&oParameters: &2<required> &c{exact} &9[optional]");
        msg("&7Detailed reference: http://serversigns.de/cmds");
    }
}