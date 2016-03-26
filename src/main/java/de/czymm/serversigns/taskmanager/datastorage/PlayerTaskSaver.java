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
import de.czymm.serversigns.taskmanager.tasks.PlayerTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class PlayerTaskSaver implements ISQLiteTaskSaver {
    private static final String SQL_INSERT_PLAYER_TASK = "INSERT INTO PlayerTask (TaskId, PlayerUniqueId)\n" +
            "VALUES (?, ?)";
    private final TaskSaver taskSaver;
    private PreparedStatement statement;

    public PlayerTaskSaver(TaskSaver taskSaver) {
        this.taskSaver = taskSaver;
    }

    @Override
    public void saveTask(Connection connection, TaskManagerTask task, List<ISQLiteTaskSaver> closeChain) throws SQLException {
        PlayerTask playerTask = ((PlayerTask) task);
        taskSaver.saveTask(connection, task, closeChain);

        if (statement == null) {
            statement = connection.prepareStatement(SQL_INSERT_PLAYER_TASK);
            closeChain.add(this);
        }

        statement.setLong(1, playerTask.getTaskID());
        statement.setString(2, playerTask.getPlayerUniqueId().toString());
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
