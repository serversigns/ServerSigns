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

package de.czymm.serversigns.parsing;

import de.czymm.serversigns.taskmanager.tasks.PermissionGrantPlayerTaskType;
import de.czymm.serversigns.taskmanager.tasks.PermissionRemovePlayerTaskType;
import de.czymm.serversigns.taskmanager.tasks.PlayerActionTaskType;
import de.czymm.serversigns.taskmanager.tasks.ServerActionTaskType;

public enum CommandType {
    SERVER_COMMAND(ServerActionTaskType.COMMAND),
    BROADCAST(ServerActionTaskType.BROADCAST),
    CHAT(PlayerActionTaskType.CHAT),
    MESSAGE(PlayerActionTaskType.MESSAGE),
    BLANK_MESSAGE(PlayerActionTaskType.BLANK_MESSAGE),
    PLAYER_COMMAND(PlayerActionTaskType.COMMAND),
    OP_COMMAND(PlayerActionTaskType.OP_COMMAND),
    PERMISSION_GRANT(PermissionGrantPlayerTaskType.PERMISSION_GRANT),
    PERMISSION_REMOVE(PermissionRemovePlayerTaskType.PERMISSION_REMOVE),
    ADD_GROUP(PlayerActionTaskType.ADD_GROUP),
    DEL_GROUP(PlayerActionTaskType.DEL_GROUP),
    CONDITIONAL_IF(null),
    CONDITIONAL_ENDIF(null),
    RETURN(null),
    CANCEL_TASKS(PlayerActionTaskType.CANCEL_TASKS),
    DISPLAY_OPTIONS(null);

    private Object taskTypeObj;

    CommandType(Object taskObj) {
        this.taskTypeObj = taskObj;
    }

    public Object getTaskObject() {
        return taskTypeObj;
    }
}
