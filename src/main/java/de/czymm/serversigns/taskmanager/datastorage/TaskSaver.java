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

public class TaskSaver implements ISQLiteTaskSaver {
    private static final String SQL_INSERT_TASK = "INSERT INTO Task (TaskId, RunAt, Type, SubType, Data)\n" +
            "VALUES (?, ?, ?, ?, ?)";
    private PreparedStatement statement;

    @Override
    public void saveTask(Connection connection, TaskManagerTask task, List<ISQLiteTaskSaver> closeChain) throws SQLException {
        if (statement == null) {
            statement = connection.prepareStatement(SQL_INSERT_TASK);
            closeChain.add(this);
        }

        statement.setLong(1, task.getTaskID());
        statement.setLong(2, task.getRunAt());
        statement.setString(3, task.getTaskType().name());
        statement.setString(4, task.getSubType().name());
        statement.setString(5, task.getData());
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
