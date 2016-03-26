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
import de.czymm.serversigns.config.ConfigLoadingException;
import de.czymm.serversigns.translations.Message;
import de.czymm.serversigns.translations.NoDefaultException;
import org.bukkit.Bukkit;

public class SubCommandReload extends SubCommand {
    public SubCommandReload(ServerSignsPlugin plugin) {
        super(
                plugin,
                "reload",
                "reload",
                "Reload config.yml, translation files, and all ServerSigns from disk",
                "reload", "rl"
        );
    }

    @Override
    public void execute(boolean verbose) {
        try {
            plugin.loadConfig(plugin.getDataFolder().toPath());
        } catch (NoDefaultException | ConfigLoadingException ex) {
            if (verbose) msg(Message.RELOAD_CONFIG_FAIL);
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        }

        if (verbose) {
            if (!plugin.serverSignsManager.populateSignsMap(plugin.serverSignsManager.prepareServerSignsSet())) {
                msg(Message.RELOAD_SIGNS_FAIL);
            }
            msg(Message.RELOAD_SUCCESS);
        }
    }
}
