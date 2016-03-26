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

package de.czymm.serversigns.commands.core;

import de.czymm.serversigns.ServerSignsPlugin;
import de.czymm.serversigns.commands.SubCommandDev;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class Command extends AbstractCommand {
    protected final String name;
    protected ArrayList<SubCommand> subCommands = new ArrayList<>();

    // Command specific variables
    protected String label;
    protected boolean isConsole;

    protected Command(final String name, ServerSignsPlugin plugin) {
        super(plugin);
        this.name = name;
    }

    public void setPlugin(final ServerSignsPlugin plugin) {
        this.plugin = plugin;
    }

    public String getName() {
        return name;
    }

    public void addSubCommand(SubCommand command) {
        subCommands.add(command);
    }

    public void removeSubCommand(SubCommand command) {
        subCommands.remove(command);
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

    protected void sendHelpMessages() {
        for (SubCommand sub : subCommands) {
            if (!isConsole && !sub.hasPermission(player)) continue;
            if (sub instanceof SubCommandDev) continue; // Don't list the dev command
            msg("/" + label + " " + sub.getParameters());
        }
    }

    public void run(final Server server, final CommandSender sender, final String commandLabel, final org.bukkit.command.Command cmd, final String[] args) throws Exception {
        // Setup execution time specific variables
        this.sender = sender;
        this.label = commandLabel;
        this.isConsole = !(sender instanceof Player);
        this.player = isConsole ? null : (Player) sender;
        this.args = Arrays.asList(args);

        perform(commandLabel, cmd);
    }

    protected abstract void perform(final String commandLabel, final org.bukkit.command.Command cmd) throws Exception;
}