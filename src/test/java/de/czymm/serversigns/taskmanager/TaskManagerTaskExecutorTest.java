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
import de.czymm.serversigns.taskmanager.datastorage.IDataStorageHandler;
import de.czymm.serversigns.taskmanager.datastorage.PersistAction;
import de.czymm.serversigns.taskmanager.datastorage.PersistTask;
import de.czymm.serversigns.taskmanager.tasks.PlayerTask;
import de.czymm.serversigns.taskmanager.tasks.TaskStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ServerSignsPlugin.class)
public class TaskManagerTaskExecutorTest {
    private TaskManagerTaskExecutor taskManagerTaskExecutor;
    private ServerSignsPlugin plugin;
    private IDataStorageHandler dataStorageHandler;
    private PlayerJoinTaskManager playerJoinTaskManager;

    @Before
    public void setUp() throws Exception {
        plugin = PowerMockito.mock(ServerSignsPlugin.class, RETURNS_DEEP_STUBS);
        dataStorageHandler = mock(IDataStorageHandler.class);
        playerJoinTaskManager = mock(PlayerJoinTaskManager.class);
        taskManagerTaskExecutor = new TaskManagerTaskExecutor(plugin, dataStorageHandler, playerJoinTaskManager);
    }

    @Test
    public void testRunTask_StatusSuccess() throws Exception {
        TaskManagerTask taskSuccess = mock(TaskManagerTask.class);
        when(taskSuccess.runTask(plugin)).thenReturn(TaskStatus.SUCCESS);
        taskManagerTaskExecutor.runTask(taskSuccess);

        verify(taskSuccess).runTask(plugin);
        verifyZeroInteractions(dataStorageHandler);
        verifyZeroInteractions(playerJoinTaskManager);
    }

    @Test
    public void testRunTask_TaskPersisted() throws Exception {
        final TaskManagerTask taskPersisted = mock(TaskManagerTask.class);
        when(taskPersisted.runTask(plugin)).thenReturn(TaskStatus.SUCCESS);
        when(taskPersisted.isPersisted()).thenReturn(true);
        taskManagerTaskExecutor.runTask(taskPersisted);

        verify(taskPersisted).runTask(plugin);
        verify(dataStorageHandler).addTask(argThat(new ArgumentMatcher<PersistTask>() {
            @Override
            public boolean matches(Object o) {
                PersistTask persistTask = (PersistTask) o;
                return persistTask.getPersistAction() == PersistAction.DELETE &&
                        persistTask.getTask() == taskPersisted;
            }
        }));
        verifyZeroInteractions(playerJoinTaskManager);
    }

    @Test
    public void testRunTask_StatusErrorPersisted() throws Exception {
        final TaskManagerTask taskErrorStatus = mock(TaskManagerTask.class);
        when(taskErrorStatus.runTask(plugin)).thenReturn(TaskStatus.ERROR);
        when(taskErrorStatus.isPersisted()).thenReturn(true);
        taskManagerTaskExecutor.runTask(taskErrorStatus);

        verify(taskErrorStatus).runTask(plugin);
        verify(dataStorageHandler).addTask(argThat(new ArgumentMatcher<PersistTask>() {
            @Override
            public boolean matches(Object o) {
                PersistTask persistTask = (PersistTask) o;
                return persistTask.getPersistAction() == PersistAction.DELETE &&
                        persistTask.getTask() == taskErrorStatus;
            }
        }));
        verifyZeroInteractions(playerJoinTaskManager);
    }

    @Test
    public void testRunTask_ThrowExceptionPersisted() throws Exception {
        final TaskManagerTask taskException = mock(TaskManagerTask.class);
        when(taskException.runTask(plugin)).thenThrow(new RuntimeException());
        when(taskException.isPersisted()).thenReturn(true);
        taskManagerTaskExecutor.runTask(taskException);

        verify(taskException).runTask(plugin);
        verify(dataStorageHandler).addTask(argThat(new ArgumentMatcher<PersistTask>() {
            @Override
            public boolean matches(Object o) {
                PersistTask persistTask = (PersistTask) o;
                return persistTask.getPersistAction() == PersistAction.DELETE &&
                        persistTask.getTask() == taskException;
            }
        }));
        verifyZeroInteractions(playerJoinTaskManager);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRunTask_PlayerNotOnlinePersisted() throws Exception {
        final PlayerTask taskPlayerNotOnline = mock(PlayerTask.class);
        when(taskPlayerNotOnline.runTask(plugin)).thenReturn(TaskStatus.PLAYER_NOT_ONLINE);
        when(taskPlayerNotOnline.isPersisted()).thenReturn(true);
        taskManagerTaskExecutor.runTask(taskPlayerNotOnline);

        verify(taskPlayerNotOnline).runTask(plugin);
        verifyZeroInteractions(dataStorageHandler);
        verify(playerJoinTaskManager).addPlayerJoinTasks(argThat(new ArgumentMatcher<List<PlayerTask>>() {
            @Override
            public boolean matches(Object o) {
                return ((List) o).contains(taskPlayerNotOnline);
            }
        }));
    }

    @Test
    public void testRunTasks() throws Exception {
        List<TaskManagerTask> tasks = new ArrayList<>();
        TaskManagerTask task1 = mock(TaskManagerTask.class);
        TaskManagerTask task2 = mock(TaskManagerTask.class);
        tasks.add(task1);
        tasks.add(task2);

        taskManagerTaskExecutor.runTasks(tasks);

        verify(task1).runTask(plugin);
        verify(task2).runTask(plugin);
    }
}