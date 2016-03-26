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

package de.czymm.serversigns.taskmanager.tasks;

import de.czymm.serversigns.taskmanager.TaskManagerTask;
import de.czymm.serversigns.taskmanager.datastorage.ISQLiteTaskLoader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class ServerActionTaskLoader implements ISQLiteTaskLoader<ServerActionTask> {
    @Override
    public ServerActionTask getTaskFromCurrentRow(ResultSet resultSet, Map<Long, TaskManagerTask> loadedTasks) throws SQLException {
        return new ServerActionTask(
                resultSet.getLong(1),
                resultSet.getLong(2),
                ServerActionTaskType.valueOf(resultSet.getString(4)),
                resultSet.getString(5),
                true);
    }
}
