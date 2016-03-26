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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class BukkitTaskManagerTaskExecutor implements ITaskExecutor<TaskManagerTask> {
    private final ServerSignsPlugin plugin;
    private final ITaskExecutor<TaskManagerTask> taskExecutor;

    public BukkitTaskManagerTaskExecutor(ServerSignsPlugin plugin, ITaskExecutor<TaskManagerTask> taskExecutor) {
        this.plugin = plugin;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void runTasks(List<? extends TaskManagerTask> tasks) {
        BukkitTaskExecutor bukkitTaskExecutor = new BukkitTaskExecutor(tasks);
        bukkitTaskExecutor.runTask(plugin);

        synchronized (this) {
            while (bukkitTaskExecutor.isRunning()) {
                try {
                    this.wait();
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    @Override
    public void runTask(TaskManagerTask task) {
        throw new UnsupportedOperationException();
    }

    private class BukkitTaskExecutor extends BukkitRunnable {
        private final List<? extends TaskManagerTask> tasks;
        private boolean running = true;

        public BukkitTaskExecutor(List<? extends TaskManagerTask> tasks) {
            this.tasks = tasks;
        }

        public boolean isRunning() {
            return running;
        }

        @Override
        public void run() {
            taskExecutor.runTasks(tasks);

            synchronized (BukkitTaskManagerTaskExecutor.this) {
                running = false;
                BukkitTaskManagerTaskExecutor.this.notify();
            }

        }
    }
}
