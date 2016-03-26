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

import de.czymm.serversigns.ServerSignsPlugin;
import de.czymm.serversigns.taskmanager.TaskManagerTask;

import java.util.Collection;

public interface IDataStorageAccessor extends AutoCloseable {
    void prepareDataStructure() throws Exception;

    Collection<TaskManagerTask> loadTasks(ServerSignsPlugin plugin) throws Exception;

    void saveTask(TaskManagerTask task) throws Exception;

    void deleteTask(TaskManagerTask task) throws Exception;
}