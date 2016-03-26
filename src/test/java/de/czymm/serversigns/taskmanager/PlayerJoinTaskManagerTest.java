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
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@PrepareForTest({ServerSignsPlugin.class, PlayerJoinEvent.class})
@RunWith(PowerMockRunner.class)
public class PlayerJoinTaskManagerTest {
    private ServerSignsPlugin plugin;
    private BlockingQueue<TaskManagerTask> taskQueue;
    private IDataStorageHandler dataStorageHandler;
    private PlayerJoinTaskManager playerJoinTaskManager;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        plugin = PowerMockito.mock(ServerSignsPlugin.class, RETURNS_DEEP_STUBS);
        taskQueue = mock(BlockingQueue.class);
        dataStorageHandler = mock(IDataStorageHandler.class);

        playerJoinTaskManager = spy(new PlayerJoinTaskManager(plugin, taskQueue, dataStorageHandler));
    }

    @Test
    public void testAddPlayerJoinTasks_notPersisted() throws Exception {
        final PlayerTask task = mock(PlayerTask.class);
        UUID uuidPlayer = new UUID(0, 0);
        when(task.getPlayerUniqueId()).thenReturn(uuidPlayer);
        when(plugin.getServer().getPlayer(eq(uuidPlayer))).thenReturn(null);
        when(task.isPersisted()).thenReturn(false);

        playerJoinTaskManager.addPlayerJoinTasks(Collections.singletonList(task));

        assertNotNull(playerJoinTaskManager.getPlayerJoinTasks().get(uuidPlayer));
        assertEquals(task, playerJoinTaskManager.getPlayerJoinTasks().get(uuidPlayer).get(0));
        verify(dataStorageHandler).addTask(argThat(new ArgumentMatcher<PersistTask>() {
            @Override
            public boolean matches(Object o) {
                PersistTask persistTask = (PersistTask) o;
                return persistTask.getTask() == task && persistTask.getPersistAction() == PersistAction.SAVE;
            }
        }));
        verify(taskQueue, never()).offer(task);
    }

    @Test
    public void testAddPlayerJoinTasks_alreadyPersisted() throws Exception {
        PlayerTask task = mock(PlayerTask.class);
        UUID uuidPlayer = new UUID(0, 0);
        when(task.getPlayerUniqueId()).thenReturn(uuidPlayer);
        when(plugin.getServer().getPlayer(eq(uuidPlayer))).thenReturn(null);
        when(task.isPersisted()).thenReturn(true);

        playerJoinTaskManager.addPlayerJoinTasks(Collections.singletonList(task));

        assertNotNull(playerJoinTaskManager.getPlayerJoinTasks().get(uuidPlayer));
        assertEquals(task, playerJoinTaskManager.getPlayerJoinTasks().get(uuidPlayer).get(0));
        verify(dataStorageHandler, never()).addTask(any(PersistTask.class));
        verify(taskQueue, never()).offer(task);
    }

    @Test
    public void testAddPlayerJoinTasks_playerOnline() throws Exception {
        PlayerTask task = mock(PlayerTask.class);
        UUID uuidPlayer = new UUID(0, 0);
        when(task.getPlayerUniqueId()).thenReturn(uuidPlayer);
        when(plugin.getServer().getPlayer(eq(uuidPlayer))).thenReturn(mock(Player.class));

        playerJoinTaskManager.addPlayerJoinTasks(Collections.singletonList(task));

        assertNull(playerJoinTaskManager.getPlayerJoinTasks().get(uuidPlayer));
        verify(dataStorageHandler, never()).addTask(any(PersistTask.class));
        verify(taskQueue).offer(task);
    }

    @Test
    public void testAddPlayerJoinTasks_multipleTasks() throws Exception {
        PlayerTask task1 = mock(PlayerTask.class);
        UUID uuidPlayer1 = new UUID(0, 0);
        when(task1.getPlayerUniqueId()).thenReturn(uuidPlayer1);
        when(plugin.getServer().getPlayer(eq(uuidPlayer1))).thenReturn(null);
        when(task1.isPersisted()).thenReturn(true);

        PlayerTask task2 = mock(PlayerTask.class);
        UUID uuidPlayer2 = new UUID(0, 1);
        when(task2.getPlayerUniqueId()).thenReturn(uuidPlayer2);
        when(plugin.getServer().getPlayer(eq(uuidPlayer2))).thenReturn(null);
        when(task2.isPersisted()).thenReturn(true);

        PlayerTask task3 = mock(PlayerTask.class);
        when(task3.getPlayerUniqueId()).thenReturn(uuidPlayer2);
        when(plugin.getServer().getPlayer(eq(uuidPlayer2))).thenReturn(null);
        when(task3.isPersisted()).thenReturn(true);


        List<PlayerTask> tasks = new ArrayList<>();
        tasks.add(task1);
        tasks.add(task2);
        tasks.add(task3);

        playerJoinTaskManager.addPlayerJoinTasks(tasks);

        assertNotNull(playerJoinTaskManager.getPlayerJoinTasks().get(uuidPlayer1));
        assertNotNull(playerJoinTaskManager.getPlayerJoinTasks().get(uuidPlayer2));
        assertEquals(task1, playerJoinTaskManager.getPlayerJoinTasks().get(uuidPlayer1).get(0));
        assertEquals(task2, playerJoinTaskManager.getPlayerJoinTasks().get(uuidPlayer2).get(0));
        assertEquals(task3, playerJoinTaskManager.getPlayerJoinTasks().get(uuidPlayer2).get(1));
        verify(dataStorageHandler, never()).addTask(any(PersistTask.class));
        verify(taskQueue, never()).offer(any(TaskManagerTask.class));
    }

    @Test
    public void testOnPlayerJoin_noTaskAvailable() throws Exception {
        PlayerJoinEvent playerJoinEvent = PowerMockito.mock(PlayerJoinEvent.class, RETURNS_DEEP_STUBS);
        UUID uuid = new UUID(0, 0);
        when(playerJoinEvent.getPlayer().getUniqueId()).thenReturn(uuid);

        playerJoinTaskManager.onPlayerJoin(playerJoinEvent);

        verify(taskQueue, never()).offer(any(TaskManagerTask.class));
    }

    @Test
    public void testOnPlayerJoin_tasksAvailable() throws Exception {
        PlayerJoinEvent playerJoinEvent = PowerMockito.mock(PlayerJoinEvent.class, RETURNS_DEEP_STUBS);
        UUID uuid = new UUID(0, 0);
        when(playerJoinEvent.getPlayer().getUniqueId()).thenReturn(uuid);

        PlayerTask playerTask = mock(PlayerTask.class);
        List<PlayerTask> tasks = new ArrayList<>();
        tasks.add(playerTask);
        tasks.add(playerTask);
        playerJoinTaskManager.getPlayerJoinTasks().put(uuid, tasks);

        playerJoinTaskManager.onPlayerJoin(playerJoinEvent);

        verify(taskQueue, times(2)).offer(playerTask);
    }
}