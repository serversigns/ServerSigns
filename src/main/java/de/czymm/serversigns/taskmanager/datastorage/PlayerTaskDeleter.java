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

import de.czymm.serversigns.taskmanager.TaskManagerTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class PlayerTaskDeleter implements ISQLiteTaskDeleter {
    private static final String SQL_DELETE_PLAYER_TASK = "DELETE FROM PlayerTask WHERE TaskId IS ?";
    private final TaskDeleter taskDeleter;
    private PreparedStatement statement;

    public PlayerTaskDeleter(TaskDeleter taskDeleter) {
        this.taskDeleter = taskDeleter;
    }

    @Override
    public void deleteTask(Connection connection, TaskManagerTask task, List<ISQLiteTaskDeleter> closeChain) throws SQLException {
        taskDeleter.deleteTask(connection, task, closeChain);

        if (statement == null) {
            statement = connection.prepareStatement(SQL_DELETE_PLAYER_TASK);
            closeChain.add(this);
        }

        statement.setLong(1, task.getTaskID());
        statement.execute();
    }

    @Override
    public void close() throws SQLException {
        try {
            statement.close();
        } finally {
            statement = null;
        }
    }
}
