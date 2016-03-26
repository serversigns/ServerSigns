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

package de.czymm.serversigns.listeners;

import de.czymm.serversigns.ServerSignsPlugin;
import de.czymm.serversigns.signs.ServerSign;
import de.czymm.serversigns.translations.Message;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockListener implements Listener {
    public ServerSignsPlugin plugin;

    public BlockListener(ServerSignsPlugin instance) {
        this.plugin = instance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();

        if (plugin.config.getAnyBlock() || plugin.config.getBlocks().contains(block.getType())) {
            ServerSign sign = plugin.serverSignsManager.getServerSignByLocation(location);
            if (sign != null) {
                if (event.getPlayer().hasPermission("serversigns.admin")) {
                    if (plugin.config.getSneakToDestroy() && !event.getPlayer().isSneaking()) {
                        plugin.send(event.getPlayer(), Message.MUST_SNEAK);
                        event.setCancelled(true);
                        return;
                    }

                    plugin.serverSignsManager.remove(sign);
                    plugin.send(event.getPlayer(), Message.COMMANDS_REMOVED);
                } else {
                    event.setCancelled(true);
                    plugin.send(event.getPlayer(), Message.CANNOT_DESTROY);
                }
                return;
            }
        }

        if (plugin.serverSignsManager.isLocationProtectedByServerSign(location)) {
            event.setCancelled(true);
            plugin.send(event.getPlayer(), Message.BLOCK_IS_PROTECTED);
        }
    }
}
