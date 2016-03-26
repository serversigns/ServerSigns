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
import de.czymm.serversigns.taskmanager.datastorage.IDataStorageAccessor;
import de.czymm.serversigns.taskmanager.datastorage.IDataStorageHandler;
import de.czymm.serversigns.taskmanager.datastorage.PersistAction;
import de.czymm.serversigns.taskmanager.datastorage.PersistTask;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@PrepareForTest(ServerSignsPlugin.class)
@RunWith(PowerMockRunner.class)
public class TaskManagerTest {
    private ServerSignsPlugin plugin;
    private AtomicLong currentId;
    private BlockingQueue<TaskManagerTask> queue;
    private IDataStorageAccessor dataStorageAccessor;
    private IDataStorageHandler dataStorageHandler;
    private PlayerJoinTaskManager playerJoinTaskManager;
    private ITaskExecutor<TaskManagerTask> taskExecutor;
    private QueueConsumer<TaskManagerTask> queueConsumer;
    private Thread taskManagerThread;
    private TaskManager taskManager;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        plugin = PowerMockito.mock(ServerSignsPlugin.class, RETURNS_DEEP_STUBS);
        currentId = new AtomicLong();
        queue = mock(BlockingQueue.class);
        dataStorageAccessor = mock(IDataStorageAccessor.class);
        dataStorageHandler = mock(IDataStorageHandler.class);
        playerJoinTaskManager = mock(PlayerJoinTaskManager.class);
        taskExecutor = mock(ITaskExecutor.class);
        queueConsumer = mock(QueueConsumer.class);
        taskManagerThread = mock(Thread.class);

        when(dataStorageHandler.newDataStorageAccessor()).thenReturn(dataStorageAccessor);

        when(plugin.getServerSignsConfig().getTaskPersistThreshold()).thenReturn(10L);

        when(queue.iterator()).thenReturn(mock(Iterator.class));

        taskManager = spy(new TaskManager(plugin, currentId, queue, dataStorageHandler, playerJoinTaskManager,
                taskExecutor, queueConsumer, taskManagerThread));
    }

    @Test
    public void testConstructor() throws Exception {
        TaskManager taskManager = spy(new TaskManager(plugin, Paths.get("")));
        verify(taskManager, never()).init();
    }

    @Test
    public void testInit() throws Exception {
        List<TaskManagerTask> tasks = new ArrayList<>();

        TaskManagerTask task1 = mock(TaskManagerTask.class);
        when(task1.getTaskID()).thenReturn(1L);
        TaskManagerTask task2 = mock(TaskManagerTask.class);
        when(task2.getTaskID()).thenReturn(2L);

        tasks.add(task1);
        tasks.add(task2);

        when(dataStorageAccessor.loadTasks(plugin)).thenReturn(tasks);

        taskManager.init();

        verify(dataStorageHandler).init();
        verify(queue).offer(task1);
        verify(queue).offer(task2);
        assertEquals(3, currentId.get());
        verify(plugin.getServer().getPluginManager()).registerEvents(playerJoinTaskManager, plugin);
    }

    @Test
    public void testAddTask() throws Exception {
        final TaskManagerTask task1 = mock(TaskManagerTask.class);
        when(task1.getRunAt()).thenReturn(0L);

        final TaskManagerTask task2 = mock(TaskManagerTask.class);
        when(task2.getRunAt()).thenReturn(5000L);
        when(task2.getDelay(TimeUnit.SECONDS)).thenReturn(5L);

        final TaskManagerTask task3 = mock(TaskManagerTask.class);
        when(task3.getRunAt()).thenReturn(20000L);
        when(task3.getDelay(TimeUnit.SECONDS)).thenReturn(20L);

        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addTask(task3);


        verify(taskExecutor).runTask(task1);
        verify(queue, never()).offer(task1);
        verify(task1, never()).setPersisted(true);

        verify(taskExecutor, never()).runTask(task2);
        verify(queue).offer(task2);
        verify(task2, never()).setPersisted(true);

        verify(dataStorageHandler, times(1)).addTask(any(PersistTask.class));
        verify(dataStorageHandler).addTask(argThat(new ArgumentMatcher<PersistTask>() {
            @Override
            public boolean matches(Object o) {
                PersistTask persistTask = (PersistTask) o;
                return persistTask.getTask() == task3 && persistTask.getPersistAction() == PersistAction.SAVE;
            }
        }));
        verify(task3).setPersisted(true);
    }

    @Test
    public void testStop() throws Exception {
        taskManager.stop();
        verify(taskManagerThread).interrupt();
        verify(queueConsumer).stop();
    }
}