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
import de.czymm.serversigns.taskmanager.tasks.TaskStatus;
import de.czymm.serversigns.taskmanager.tasks.TaskType;
import de.czymm.serversigns.utils.CurrentTime;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public abstract class TaskManagerTask<T extends Enum<T>> implements Delayed {
    private final T subType;
    private final String data;

    private final long runAt;
    private long taskID;
    private boolean persisted;
    private boolean alwaysPersisted;

    protected TaskManagerTask(long taskID, long runAt, T subType, String data, boolean alwaysPersisted) {
        this.subType = subType;
        this.taskID = taskID;
        this.runAt = runAt;
        this.data = data;
        this.persisted = true;
        this.alwaysPersisted = alwaysPersisted;
    }

    public TaskManagerTask(long runAt, T subType, String data, boolean alwaysPersisted) {
        this.runAt = runAt;
        this.subType = subType;
        this.data = data;
        this.alwaysPersisted = alwaysPersisted;
    }

    public boolean isAlwaysPersisted() {
        return alwaysPersisted;
    }

    public void setAlwaysPersisted(boolean val) {
        alwaysPersisted = val;
    }

    void setTaskID(long taskID) {
        this.taskID = taskID;
    }

    public long getTaskID() {
        return taskID;
    }

    public long getRunAt() {
        return runAt;
    }

    void setPersisted(boolean persisted) {
        this.persisted = persisted;
    }

    public boolean isPersisted() {
        return persisted;
    }

    public T getSubType() {
        return subType;
    }

    public String getData() {
        return data;
    }

    public abstract TaskStatus runTask(ServerSignsPlugin plugin);

    public abstract TaskType getTaskType();

    @Override
    public long getDelay(TimeUnit unit) {
        return getRunAt() > 0 ? unit.convert(getRunAt() - CurrentTime.get(), TimeUnit.MILLISECONDS) : 0;
    }

    @Override
    public int compareTo(Delayed o) {
        TaskManagerTask that = (TaskManagerTask) o;
        return getRunAt() < that.getRunAt() ? -1 : getRunAt() > that.getRunAt() ? 1 : getTaskID() < that.getTaskID() ? -1 : 1;
    }
}
