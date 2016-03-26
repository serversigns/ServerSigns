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

package de.czymm.serversigns.taskmanager.datastorage;

import de.czymm.serversigns.taskmanager.ITaskExecutor;
import de.czymm.serversigns.taskmanager.TaskManagerTask;

import java.util.List;

public class PersistTaskExecutor implements ITaskExecutor<PersistTask> {
    private final IDataStorageHandler dataStorageHandler;

    public PersistTaskExecutor(IDataStorageHandler dataStorageHandler) {
        this.dataStorageHandler = dataStorageHandler;
    }

    @Override
    public void runTasks(List<? extends PersistTask> tasks) {
        try (IDataStorageAccessor storage = dataStorageHandler.newDataStorageAccessor()) {
            for (PersistTask persistTask : tasks) {
                PersistAction persistAction = persistTask.getPersistAction();
                TaskManagerTask taskManagerTask = persistTask.getTask();

                if (persistAction == PersistAction.SAVE) {
                    storage.saveTask(taskManagerTask);
                } else if (persistAction == PersistAction.DELETE) {
                    storage.deleteTask(taskManagerTask);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void runTask(PersistTask task) {
        throw new UnsupportedOperationException();
    }
}