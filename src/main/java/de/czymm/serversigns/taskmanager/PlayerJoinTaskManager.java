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

package de.czymm.serversigns.taskmanager;

import de.czymm.serversigns.ServerSignsPlugin;
import de.czymm.serversigns.taskmanager.datastorage.IDataStorageHandler;
import de.czymm.serversigns.taskmanager.datastorage.PersistAction;
import de.czymm.serversigns.taskmanager.datastorage.PersistTask;
import de.czymm.serversigns.taskmanager.tasks.PlayerTask;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;
import java.util.concurrent.BlockingQueue;

public class PlayerJoinTaskManager implements Listener {
    private final Map<UUID, List<PlayerTask>> playerJoinTasks = new HashMap<>();
    private final ServerSignsPlugin plugin;
    private final BlockingQueue<TaskManagerTask> taskQueue;
    private final IDataStorageHandler dataStorageHandler;

    public PlayerJoinTaskManager(ServerSignsPlugin plugin,
                                 BlockingQueue<TaskManagerTask> taskQueue,
                                 IDataStorageHandler dataStorageHandler) {
        this.plugin = plugin;
        this.taskQueue = taskQueue;
        this.dataStorageHandler = dataStorageHandler;
    }

    public void addPlayerJoinTasks(List<PlayerTask> tasksWaitingForPlayer) {
        if (tasksWaitingForPlayer.size() > 0) {
            synchronized (playerJoinTasks) {
                for (PlayerTask playerTask : tasksWaitingForPlayer) {
                    //verify player is offline
                    if (plugin.getServer().getPlayer(playerTask.getPlayerUniqueId()) != null) {
                        //player came online after executing, queueing again
                        taskQueue.offer(playerTask);
                    } else {
                        List<PlayerTask> playerTasks = playerJoinTasks.get(playerTask.getPlayerUniqueId());
                        if (playerTasks == null) {
                            playerTasks = new ArrayList<>();
                            playerJoinTasks.put(playerTask.getPlayerUniqueId(), playerTasks);
                        }
                        playerTasks.add(playerTask);

                        if (!playerTask.isPersisted()) {
                            dataStorageHandler.addTask(new PersistTask(PersistAction.SAVE, playerTask));
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID playerUniqueId = event.getPlayer().getUniqueId();
        synchronized (playerJoinTasks) {
            if (playerJoinTasks.containsKey(playerUniqueId)) {
                for (PlayerTask playerTask : playerJoinTasks.get(playerUniqueId)) {
                    taskQueue.offer(playerTask);
                }
                playerJoinTasks.remove(playerUniqueId);
            }
        }
    }

    Map<UUID, List<PlayerTask>> getPlayerJoinTasks() {
        return playerJoinTasks;
    }
}
