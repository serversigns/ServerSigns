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
import de.czymm.serversigns.taskmanager.tasks.TaskStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class TaskManagerTaskExecutor implements ITaskExecutor<TaskManagerTask> {
    private final ServerSignsPlugin plugin;
    private final IDataStorageHandler dataStorageHandler;
    private final PlayerJoinTaskManager playerJoinTaskManager;

    public TaskManagerTaskExecutor(ServerSignsPlugin plugin, IDataStorageHandler dataStorageHandler,
                                   PlayerJoinTaskManager playerJoinTaskManager) {
        this.plugin = plugin;
        this.dataStorageHandler = dataStorageHandler;
        this.playerJoinTaskManager = playerJoinTaskManager;
    }

    @Override
    public void runTasks(List<? extends TaskManagerTask> tasks) {
        List<PlayerTask> tasksWaitingForPlayer = new ArrayList<>();
        for (TaskManagerTask task : tasks) {
            runTaskImpl(task, tasksWaitingForPlayer);
        }
        cleanUp(tasksWaitingForPlayer);
    }

    @Override
    public void runTask(TaskManagerTask task) {
        List<PlayerTask> tasksWaitingForPlayer = new ArrayList<>();
        runTaskImpl(task, tasksWaitingForPlayer);
        cleanUp(tasksWaitingForPlayer);
    }

    private void runTaskImpl(TaskManagerTask task, List<PlayerTask> tasksWaitingForPlayer) {
        TaskStatus taskStatus;
        try {
            taskStatus = task.runTask(plugin);
        } catch (RuntimeException e) {
            taskStatus = TaskStatus.ERROR;
            plugin.getLogger().log(Level.WARNING, "Error while executing task " + task, e);
        }

        if (task instanceof PlayerTask && taskStatus == TaskStatus.PLAYER_NOT_ONLINE) {
            tasksWaitingForPlayer.add(((PlayerTask) task));
        } else if (task.isPersisted()) {
            dataStorageHandler.addTask(new PersistTask(PersistAction.DELETE, task));
        }
    }

    private void cleanUp(List<PlayerTask> tasksWaitingForPlayer) {
        if (tasksWaitingForPlayer.size() > 0) {
            playerJoinTaskManager.addPlayerJoinTasks(tasksWaitingForPlayer);
        }
    }
}
