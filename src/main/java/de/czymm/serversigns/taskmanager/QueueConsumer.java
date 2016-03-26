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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class QueueConsumer<E> implements Runnable {
    private final BlockingQueue<E> queue;
    private final ITaskExecutor<E> taskExecutor;
    private volatile boolean running = true;

    public QueueConsumer(BlockingQueue<E> queue, ITaskExecutor<E> taskExecutor) {
        this.queue = queue;
        this.taskExecutor = taskExecutor;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                List<E> tasksToRun = new ArrayList<>();
                tasksToRun.add(queue.take());
                queue.drainTo(tasksToRun);
                taskExecutor.runTasks(tasksToRun);
            } catch (InterruptedException ignored) {
            }
        }
    }
}