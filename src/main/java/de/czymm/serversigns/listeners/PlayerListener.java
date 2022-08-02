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
import de.czymm.serversigns.meta.SVSMetaKey;
import de.czymm.serversigns.meta.SVSMetaManager;
import de.czymm.serversigns.signs.ServerSign;
import de.czymm.serversigns.translations.Message;
import de.czymm.serversigns.utils.Version;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.UUID;

public class PlayerListener implements Listener {
    private ServerSignsPlugin plugin;
    private HashMap<UUID, PlatePair> plateMap = new HashMap<>();
    private static boolean offHandSupported = true;

    private static EnumSet<Material> PLATE_MATERIALS;

    public PlayerListener(ServerSignsPlugin instance) {
        this.plugin = instance;
        if (Version.isLowerOrEqualsTo(Version.V1_12)) {
            PLATE_MATERIALS = EnumSet.of(
                Material.getMaterial("WOOD_PLATE"),
                Material.getMaterial("STONE_PLATE"),
                Material.getMaterial("IRON_PLATE"),
                Material.getMaterial("GOLD_PLATE")
            );
        } else {
            PLATE_MATERIALS = EnumSet.of(
                Material.getMaterial("OAK_PRESSURE_PLATE"),
                Material.getMaterial("ACACIA_PRESSURE_PLATE"),
                Material.getMaterial("BIRCH_PRESSURE_PLATE"),
                Material.getMaterial("DARK_OAK_PRESSURE_PLATE"),
                Material.getMaterial("JUNGLE_PRESSURE_PLATE"),
                Material.getMaterial("SPRUCE_PRESSURE_PLATE"),
                Material.getMaterial("STONE_PRESSURE_PLATE"),
                Material.getMaterial("LIGHT_WEIGHTED_PRESSURE_PLATE"),
                Material.getMaterial("HEAVY_WEIGHTED_PRESSURE_PLATE")
            );
        }
        if (Version.isHigherOrEqualsTo(Version.V1_16)) {
            PLATE_MATERIALS.addAll(EnumSet.of(
                Material.getMaterial("POLISHED_BLACKSTONE_PRESSURE_PLATE"),
                Material.getMaterial("CRIMSON_PRESSURE_PLATE"),
                Material.getMaterial("WARPED_PRESSURE_PLATE")
            ));
        }
        if (Version.isHigherOrEqualsTo(Version.V1_19)) {
            PLATE_MATERIALS.add(Material.getMaterial("MANGROVE_PRESSURE_PLATE"));
        }

    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void playerInteractCheck(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (SVSMetaManager.hasExclusiveMeta(player, SVSMetaKey.YES) || isOffHand(event)) {
            return;
        }

        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.PHYSICAL) || (plugin.config.getAllowLeftClicking() && event.getAction().equals(Action.LEFT_CLICK_BLOCK))) {
            if (plugin.config.getAnyBlock() || plugin.config.getBlocks().contains(block.getType())) {
                final UUID playerUniqueId = player.getUniqueId();
                PlatePair pair = plateMap.get(playerUniqueId);
                if (pair == null) {
                    ServerSign sign = plugin.serverSignsManager.getServerSignByLocation(block.getLocation());
                    if (sign != null) {
                        // Check if this is an admin attempt to remove the ServerSign
                        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK) && plugin.config.getAllowLeftClicking() && player.hasPermission("serversigns.admin")) {
                            if ((plugin.config.getSneakToDestroy() && player.isSneaking()) || (!plugin.config.getSneakToDestroy() && !player.isSneaking())) {
                                plugin.serverSignsManager.remove(sign);
                                plugin.send(event.getPlayer(), Message.COMMANDS_REMOVED);
                                event.setCancelled(true);
                                return;
                            }
                        }

                        plugin.serverSignExecutor.executeSignFull(player, sign, event);
                        if (PLATE_MATERIALS.contains(block.getType())) {
                            pair = new PlatePair(createRemoveTask(plugin, playerUniqueId), event.isCancelled());
                            plateMap.put(playerUniqueId, pair);
                        }
                    }
                } else {
                    pair.getTask().cancel();
                    pair.setTask(createRemoveTask(plugin, playerUniqueId));
                    event.setCancelled(pair.isCancelled());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void playerDeathCheck(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (plugin.config.getCancelTasksOnDeath()) {
            plugin.taskManager.removePlayerTasks(player.getUniqueId(), plugin.config.getCompiledCancelTaskPattern());
        }
    }

    private BukkitTask createRemoveTask(ServerSignsPlugin plugin, final UUID playerUniqueId) {
        return new BukkitRunnable() {
            public void run() {
                plateMap.remove(playerUniqueId);
            }
        }.runTaskLater(plugin, 5L);
    }

    private class PlatePair {
        private boolean cancelled;
        private BukkitTask task;

        public PlatePair(BukkitTask task, boolean cancelled) {
            this.cancelled = cancelled;
            this.task = task;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public BukkitTask getTask() {
            return task;
        }

        public void setTask(BukkitTask task) {
            this.task = task;
        }
    }

    /**
     * Define if the hand used in event is off hand
     *
     * @param event Event to analyse
     * @return Is off hand
     */
    private boolean isOffHand(final PlayerInteractEvent event) {
        if(!offHandSupported) {
            return false;
        }
        try {
            return event.getHand() == EquipmentSlot.OFF_HAND;
        } catch (NoSuchMethodError e) {
            offHandSupported = false;
            return false;
        }
    }
}
