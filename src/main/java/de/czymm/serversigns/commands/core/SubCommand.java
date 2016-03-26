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
import de.czymm.serversigns.meta.SVSMeta;
import de.czymm.serversigns.meta.SVSMetaKey;
import de.czymm.serversigns.meta.SVSMetaManager;
import de.czymm.serversigns.meta.SVSMetaValue;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public abstract class SubCommand extends AbstractCommand {
    protected ServerSignsPlugin plugin;

    protected String commandParams;
    protected String commandDescription;
    protected String permission;
    protected List<String> aliases;

    public SubCommand(ServerSignsPlugin plugin, String permission, String commandParams, String commandDescription, String... aliases) {
        super(plugin);
        this.plugin = plugin;
        this.commandParams = commandParams;
        this.commandDescription = commandDescription;
        this.permission = permission;
        this.aliases = Arrays.asList(aliases);
    }

    public void execute(CommandSender sender, List<String> args, String label, boolean verbose) {
        if (sender instanceof Player) {
            this.player = (Player) sender;
        }
        this.sender = sender;
        this.args = args;
        this.label = label;

        execute(verbose);
    }

    protected abstract void execute(boolean verbose); // Returns true if the arguments were handled, false otherwise

    // Descriptors

    public String getParameters() {
        return this.commandParams;
    }

    public String getDescription() {
        return this.commandDescription;
    }

    protected void sendUsage() {
        msg("/" + getLastLabel() + " " + getParameters() + " - " + getDescription());
    }

    // Permissions

    public String getPermission() {
        return this.permission;
    }

    public boolean hasPermission(Player player) {
        return player.hasPermission("serversigns.command." + permission);
    }

    // Aliases

    public List<String> getAliases() {
        return this.aliases;
    }

    // Meta

    protected void applyMeta(SVSMetaKey key, SVSMetaValue... values) {
        SVSMetaManager.setMeta(player == null ? SVSMetaManager.CONSOLE_UUID : player.getUniqueId(), new SVSMeta(key, values));
    }

    @SuppressWarnings("unchecked")
    protected SVSMetaValue[] getMetaValues(SVSMetaKey key) {
        UUID id = player == null ? SVSMetaManager.CONSOLE_UUID : player.getUniqueId();
        if (SVSMetaManager.hasMeta(id)) {
            SVSMeta meta = SVSMetaManager.getMeta(id);

            if (meta.getKey().equals(key)) {
                return meta.getValues();
            }
        }

        return null;
    }
}
