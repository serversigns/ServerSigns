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

package de.czymm.serversigns.taskmanager.tasks;

import de.czymm.serversigns.ServerSignsPlugin;
import de.czymm.serversigns.taskmanager.TaskManagerTask;
import org.bukkit.Bukkit;

public class ServerActionTask extends TaskManagerTask<ServerActionTaskType> {

    public ServerActionTask(long runAt, ServerActionTaskType actionType, String action, boolean alwaysPersisted) {
        super(runAt, actionType, action, alwaysPersisted);
    }

    protected ServerActionTask(long taskID, long runAt, ServerActionTaskType actionType, String action, boolean alwaysPersisted) {
        super(taskID, runAt, actionType, action, alwaysPersisted);
    }

    @Override
    public TaskStatus runTask(ServerSignsPlugin plugin) {
        switch (getSubType()) {
            case COMMAND:
                plugin.serverCommand(getData());
                break;
            case BROADCAST:
                Bukkit.broadcastMessage(getData());
                break;
        }

        return TaskStatus.SUCCESS;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.SERVER;
    }
}
