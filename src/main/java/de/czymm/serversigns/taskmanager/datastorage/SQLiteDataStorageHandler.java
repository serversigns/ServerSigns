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

import de.czymm.serversigns.taskmanager.QueueConsumer;

import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SQLiteDataStorageHandler implements IDataStorageHandler {
    private final QueueConsumer<PersistTask> queueConsumer;
    private final String databaseUrl;
    private final BlockingQueue<PersistTask> tasksQueue;

    public SQLiteDataStorageHandler(Path dataFolder) throws ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        databaseUrl = String.format("jdbc:sqlite:%s", dataFolder.resolve("tasks.sqlite").toString());
        tasksQueue = new LinkedBlockingQueue<>();
        PersistTaskExecutor persistTaskExecutor = new PersistTaskExecutor(this);
        queueConsumer = new QueueConsumer<>(tasksQueue, persistTaskExecutor);
    }

    @Override
    public void init() throws Exception {
        try (IDataStorageAccessor storage = new SQLiteDataStorageAccessor(databaseUrl)) {
            storage.prepareDataStructure();
        }
        Thread persistThread = new Thread(queueConsumer, "ServerSigns-TaskPersistence");
        persistThread.start();
    }

    @Override
    public IDataStorageAccessor newDataStorageAccessor() throws Exception {
        return new SQLiteDataStorageAccessor(databaseUrl);
    }

    @Override
    public void addTask(PersistTask task) {
        tasksQueue.offer(task);
    }

    @Override
    public void close() {
        queueConsumer.stop();
        tasksQueue.add(new PersistTask(PersistAction.STOP, null));
    }
}
