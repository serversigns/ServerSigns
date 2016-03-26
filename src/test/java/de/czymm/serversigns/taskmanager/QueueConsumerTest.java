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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class QueueConsumerTest {
    private BlockingQueue<Object> queue;
    private ITaskExecutor<Object> taskExecutor;
    private QueueConsumer<Object> queueConsumer;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        queue = new LinkedBlockingQueue<>();
        taskExecutor = mock(ITaskExecutor.class);
        queueConsumer = spy(new QueueConsumer<>(queue, taskExecutor));
    }

    @Test
    public void testRun() throws Exception {
        final Object o1 = new Object();
        final Object o2 = new Object();

        queue.offer(o1);
        queue.offer(o2);

        Thread thread = new Thread(queueConsumer);
        thread.start();

        verify(taskExecutor, timeout(1000)).runTasks(argThat(new ArgumentMatcher<List<?>>() {
            @Override
            public boolean matches(Object o) {
                List list = (List) o;
                return list.contains(o1) && list.contains(o2);
            }
        }));

        queueConsumer.stop();
        thread.interrupt();
    }
}