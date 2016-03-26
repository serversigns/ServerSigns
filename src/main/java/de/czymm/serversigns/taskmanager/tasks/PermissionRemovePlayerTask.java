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
import org.bukkit.entity.Player;

import java.util.UUID;

public class PermissionRemovePlayerTask extends PlayerTask<PermissionRemovePlayerTaskType> {
    private PermissionGrantPlayerTask permissionGrantTask;

    public PermissionRemovePlayerTask(long runAt, UUID playerUniqueId, PermissionGrantPlayerTask permissionGrantTask, boolean alwaysPersisted) {
        super(runAt, PermissionRemovePlayerTaskType.PERMISSION_GRANT_REMOVE, String.valueOf(permissionGrantTask.getTaskType()), playerUniqueId, alwaysPersisted);
        this.permissionGrantTask = permissionGrantTask;
    }

    public PermissionRemovePlayerTask(long runAt, String action, UUID playerUniqueId, boolean alwaysPersisted) {
        super(runAt, PermissionRemovePlayerTaskType.PERMISSION_REMOVE, action, playerUniqueId, alwaysPersisted);
    }

    protected PermissionRemovePlayerTask(long taskID, long runAt, String action, UUID playerUniqueId, boolean alwaysPersisted) {
        super(taskID, runAt, PermissionRemovePlayerTaskType.PERMISSION_REMOVE, action, playerUniqueId, alwaysPersisted);
    }

    protected PermissionRemovePlayerTask(long taskID, long runAt, UUID playerUniqueId, PermissionGrantPlayerTask permissionGrantTask, boolean alwaysPersisted) {
        super(taskID, runAt, PermissionRemovePlayerTaskType.PERMISSION_GRANT_REMOVE, String.valueOf(permissionGrantTask.getTaskType()), playerUniqueId, alwaysPersisted);
        this.permissionGrantTask = permissionGrantTask;
    }

    @Override
    protected TaskStatus runPlayerTask(ServerSignsPlugin plugin, Player player) {
        if (plugin.hookManager.vault.isHooked() && plugin.hookManager.vault.getHook().hasPermissions()) {
            switch (getSubType()) {
                case PERMISSION_GRANT_REMOVE:
                    if (permissionGrantTask.isPermissionChanged()) {
                        plugin.hookManager.vault.getHook().getPermission().playerRemove(player, permissionGrantTask.getData());
                    }
                    break;
                case PERMISSION_REMOVE:
                    plugin.hookManager.vault.getHook().getPermission().playerRemove(player, getData());
            }
        }

        return TaskStatus.SUCCESS;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.PERMISSION_REMOVE;
    }
}
