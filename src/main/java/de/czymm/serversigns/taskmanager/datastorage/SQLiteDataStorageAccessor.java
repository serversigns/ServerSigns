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
import de.czymm.serversigns.taskmanager.tasks.*;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class SQLiteDataStorageAccessor implements IDataStorageAccessor {
    private static final String SQL_CREATE_TABLE_TASK = "CREATE TABLE IF NOT EXISTS Task(\n" +
            "TaskId INTEGER PRIMARY KEY NOT NULL,\n" +
            "RunAt INTEGER NOT NULL,\n" +
            "Type TEXT NOT NULL,\n" +
            "SubType TEXT NOT NULL,\n" +
            "Data TEXT NOT NULL\n)";
    private static final String SQL_CREATE_TABLE_PLAYER_TASK = "CREATE TABLE IF NOT EXISTS PlayerTask(\n" +
            "TaskId INTEGER NOT NULL,\n" +
            "PlayerUniqueId TEXT NOT NULL,\n" +
            "FOREIGN KEY (TaskId) REFERENCES Task(TaskId)\n)";
    private static final String SQL_SELECT_ALL =
            "SELECT Task.TaskId, Task.RunAt, Task.Type, Task.SubType, Task.Data, PlayerTask.PlayerUniqueId\n" +
                    "FROM Task LEFT JOIN PlayerTask\n" +
                    "ON Task.TaskId = PlayerTask.TaskId";
    private static final Map<TaskType, ISQLiteTaskLoader> TASK_LOADERS;
    private static final Map<TaskType, ISQLiteTaskSaver> TASK_SAVERS;
    private static final Map<TaskType, ISQLiteTaskDeleter> TASK_DELETER;

    static {
        TASK_LOADERS = new HashMap<>(4);
        TASK_LOADERS.put(TaskType.SERVER, new ServerActionTaskLoader());
        TASK_LOADERS.put(TaskType.PLAYER, new PlayerActionTaskLoader());
        TASK_LOADERS.put(TaskType.PERMISSION_GRANT, new PermissionGrantPlayerTaskLoader());
        TASK_LOADERS.put(TaskType.PERMISSION_REMOVE, new PermissionRemovePlayerTaskLoader());

        TASK_SAVERS = new HashMap<>(4);
        TaskSaver taskSaver = new TaskSaver();
        PlayerTaskSaver playerTaskSaver = new PlayerTaskSaver(taskSaver);
        TASK_SAVERS.put(TaskType.SERVER, taskSaver);
        TASK_SAVERS.put(TaskType.PLAYER, playerTaskSaver);
        TASK_SAVERS.put(TaskType.PERMISSION_GRANT, playerTaskSaver);
        TASK_SAVERS.put(TaskType.PERMISSION_REMOVE, playerTaskSaver);

        TASK_DELETER = new HashMap<>(4);
        TaskDeleter taskDeleter = new TaskDeleter();
        PlayerTaskDeleter playerTaskDeleter = new PlayerTaskDeleter(taskDeleter);
        TASK_DELETER.put(TaskType.SERVER, taskDeleter);
        TASK_DELETER.put(TaskType.PLAYER, playerTaskDeleter);
        TASK_DELETER.put(TaskType.PERMISSION_GRANT, playerTaskDeleter);
        TASK_DELETER.put(TaskType.PERMISSION_REMOVE, playerTaskDeleter);
    }

    private final Connection connection;
    private List<ISQLiteTaskSaver> saveCloseChain;
    private List<ISQLiteTaskDeleter> deleteCloseChain;

    SQLiteDataStorageAccessor(String databaseUrl) throws SQLException {
        connection = DriverManager.getConnection(databaseUrl);
        connection.setAutoCommit(false);
    }

    @Override
    public void prepareDataStructure() throws Exception {
        try (Statement s = connection.createStatement()) {
            s.execute(SQL_CREATE_TABLE_TASK);
            s.execute(SQL_CREATE_TABLE_PLAYER_TASK);
            s.execute("PRAGMA foreign_keys = ON");
        }
    }

    @Override
    public Collection<TaskManagerTask> loadTasks(ServerSignsPlugin plugin) throws SQLException {
        try (Statement s = connection.createStatement();
             ResultSet resultSet = s.executeQuery(SQL_SELECT_ALL)) {
            Map<Long, TaskManagerTask> loadedTasks = new TreeMap<>();
            while (resultSet.next()) {
                try {
                    TaskType taskType = TaskType.valueOf(resultSet.getString(3));
                    ISQLiteTaskLoader taskLoader = TASK_LOADERS.get(taskType);
                    if (taskLoader != null) {
                        TaskManagerTask task = taskLoader.getTaskFromCurrentRow(resultSet, loadedTasks);
                        if (task != null) {
                            loadedTasks.put(task.getTaskID(), task);
                            continue;
                        }
                    }
                    plugin.getLogger().log(Level.WARNING, "Could not load task (" + getSqlRowString(resultSet) + ")");
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Exception while loading task", e);
                }
            }
            return loadedTasks.values();
        }
    }

    private String getSqlRowString(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < metaData.getColumnCount(); i++) {
            stringBuilder.append(metaData.getColumnLabel(i)).append(": ").append(resultSet.getObject(i)).append(", ");
        }
        stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
        return stringBuilder.toString();
    }

    @Override
    public void saveTask(TaskManagerTask task) throws Exception {
        if (saveCloseChain == null) {
            saveCloseChain = new ArrayList<>();
        }

        ISQLiteTaskSaver taskSaver = TASK_SAVERS.get(task.getTaskType());
        try {
            taskSaver.saveTask(connection, task, saveCloseChain);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTask(TaskManagerTask task) throws Exception {
        if (deleteCloseChain == null) {
            deleteCloseChain = new ArrayList<>();
        }

        ISQLiteTaskDeleter taskDeleter = TASK_DELETER.get(task.getTaskType());
        try {
            taskDeleter.deleteTask(connection, task, deleteCloseChain);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {
        Exception thrownException = null;

        if (saveCloseChain != null) {
            for (ISQLiteTaskSaver taskSaver : saveCloseChain) {
                try {
                    taskSaver.close();
                } catch (Exception e) {
                    thrownException = e;
                }
            }
        }

        if (deleteCloseChain != null) {
            for (ISQLiteTaskDeleter taskDeleter : deleteCloseChain) {
                try {
                    taskDeleter.close();
                } catch (Exception e) {
                    thrownException = e;
                }
            }
        }

        if (connection != null) {
            try {
                try {
                    connection.commit();
                } finally {
                    connection.close();
                }
            } catch (Exception e) {
                thrownException = e;
            }
        }

        if (thrownException != null) {
            throw thrownException;
        }
    }
}
