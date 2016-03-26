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
import de.czymm.serversigns.legacy.QueueToTaskConverter;
import de.czymm.serversigns.taskmanager.datastorage.*;
import de.czymm.serversigns.taskmanager.tasks.PlayerTask;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

public class TaskManager {
    private final ServerSignsPlugin plugin;
    private final AtomicLong currentId;
    private final BlockingQueue<TaskManagerTask> queue;
    private final IDataStorageHandler dataStorageHandler;
    private final PlayerJoinTaskManager playerJoinTaskManager;
    private final ITaskExecutor<TaskManagerTask> taskExecutor;
    private final QueueConsumer<TaskManagerTask> queueConsumer;
    private final Thread taskManagerThread;

    //Constructor for tests
    TaskManager(ServerSignsPlugin plugin,
                AtomicLong currentId,
                BlockingQueue<TaskManagerTask> queue,
                IDataStorageHandler dataStorageHandler,
                PlayerJoinTaskManager playerJoinTaskManager,
                ITaskExecutor<TaskManagerTask> taskExecutor,
                QueueConsumer<TaskManagerTask> queueConsumer,
                Thread taskManagerThread) {
        this.plugin = plugin;
        this.currentId = currentId;
        this.queue = queue;
        this.dataStorageHandler = dataStorageHandler;
        this.playerJoinTaskManager = playerJoinTaskManager;
        this.taskExecutor = taskExecutor;
        this.queueConsumer = queueConsumer;
        this.taskManagerThread = taskManagerThread;
    }

    public TaskManager(ServerSignsPlugin plugin, Path dataFolder) throws Exception {
        this.plugin = plugin;
        currentId = new AtomicLong();
        queue = new DelayQueue<>();
        dataStorageHandler = new SQLiteDataStorageHandler(dataFolder);
        playerJoinTaskManager = new PlayerJoinTaskManager(plugin, queue, dataStorageHandler);
        taskExecutor = new TaskManagerTaskExecutor(plugin, dataStorageHandler, playerJoinTaskManager);
        queueConsumer = new QueueConsumer<>(queue, new BukkitTaskManagerTaskExecutor(plugin, taskExecutor));
        taskManagerThread = new Thread(queueConsumer, "ServerSigns-TaskManager");
    }

    public void init() throws Exception {
        plugin.getServer().getPluginManager().registerEvents(playerJoinTaskManager, plugin);
        dataStorageHandler.init();
        try (IDataStorageAccessor dataStorageAccessor = dataStorageHandler.newDataStorageAccessor()) {
            Collection<TaskManagerTask> tasks = dataStorageAccessor.loadTasks(plugin);
            if (tasks.size() > 0) {
                long highestId = 0;
                for (TaskManagerTask taskManagerTask : tasks) {
                    queue.offer(taskManagerTask);
                    highestId = Math.max(taskManagerTask.getTaskID(), highestId);
                }
                currentId.set(highestId + 1);
            }
        }

        QueueToTaskConverter.QueueToTaskResult result = QueueToTaskConverter.convertFile(plugin.getDataFolder().toPath());
        if (result != null) {
            if (result.getHighestId() > currentId.get()) {
                currentId.set(result.getHighestId());
            }
            for (TaskManagerTask task : result.getTasks()) {
                addTask(task);
            }
        }
    }

    public void start() {
        taskManagerThread.start();
    }

    public void addTask(TaskManagerTask task) {
        task.setTaskID(currentId.getAndIncrement());
        if (task.getRunAt() == 0) {
            taskExecutor.runTask(task);
        } else {
            if (task.getDelay(TimeUnit.SECONDS) > plugin.getServerSignsConfig().getTaskPersistThreshold() || task.isAlwaysPersisted()) {
                dataStorageHandler.addTask(new PersistTask(PersistAction.SAVE, task));
                task.setPersisted(true);
            }
            queue.offer(task);
        }
    }

    public int removePlayerTasks(UUID player, Pattern regexPattern) {
        int removed = 0;
        for (Iterator<TaskManagerTask> it = queue.iterator(); it.hasNext(); ) {
            TaskManagerTask task = it.next();
            if (task instanceof PlayerTask) {
                PlayerTask ptask = (PlayerTask) task;
                if (ptask.getPlayerUniqueId().equals(player)) {
                    if (regexPattern == null || regexPattern.matcher(ptask.getData()).matches()) {
                        it.remove();
                        removed++;
                    }
                }
            }
        }
        return removed;
    }

    public void stop() {
        queueConsumer.stop();
        taskManagerThread.interrupt();

        for (TaskManagerTask taskManagerTask : queue) {
            if (!taskManagerTask.isPersisted()) {
                dataStorageHandler.addTask(new PersistTask(PersistAction.SAVE, taskManagerTask));
            }
        }

        dataStorageHandler.close();
    }
}
