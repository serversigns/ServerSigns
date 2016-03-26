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
import java.util.regex.Pattern;

public class PlayerActionTask extends PlayerTask<PlayerActionTaskType> {
    public PlayerActionTask(long runAt, PlayerActionTaskType actionType, String action, UUID playerUniqueId, boolean alwaysPersisted) {
        super(runAt, actionType, action, playerUniqueId, alwaysPersisted);
    }

    protected PlayerActionTask(long taskID, long runAt, PlayerActionTaskType type, String action, UUID playerUniqueId, boolean alwaysPersisted) {
        super(taskID, runAt, type, action, playerUniqueId, alwaysPersisted);
    }

    private void performCommand(ServerSignsPlugin plugin, Player player) {
        if (plugin.getServerSignsConfig().getAlternateCommandDispatching()) {
            player.chat("/" + getData());
        } else {
            player.performCommand(getData());
        }
    }

    @Override
    protected TaskStatus runPlayerTask(ServerSignsPlugin plugin, Player player) {
        switch (getSubType()) {
            case SERVER_COMMAND:
                plugin.serverCommand(getData());
                break;
            case CHAT:
                player.chat(getData());
                break;
            case MESSAGE:
                plugin.send(player, getData());
                break;
            case BLANK_MESSAGE:
                plugin.sendBlank(player, getData());
                break;

            case COMMAND:
            case OP_COMMAND:
                boolean changedOp = false;
                try {
                    if (getSubType() == PlayerActionTaskType.OP_COMMAND && !player.isOp()) {
                        changedOp = true;
                        player.setOp(true);
                    }

                    performCommand(plugin, player);
                } finally {
                    if (changedOp) {
                        player.setOp(false);
                    }
                }
                break;

            case ADD_GROUP:
            case DEL_GROUP:
                if (plugin.hookManager.vault.isHooked() && plugin.hookManager.vault.getHook().hasPermissions()) {
                    if (getSubType() == PlayerActionTaskType.ADD_GROUP) {
                        plugin.hookManager.vault.getHook().getPermission().playerAddGroup(player, getData());
                    } else {
                        plugin.hookManager.vault.getHook().getPermission().playerRemoveGroup(player, getData());
                    }
                }
                break;

            case CANCEL_TASKS:
                int removed = plugin.taskManager.removePlayerTasks(player.getUniqueId(), getData().isEmpty() ? null : Pattern.compile(getData()));
                if (removed > 0) {
                    ServerSignsPlugin.log("Successfully removed " + removed + " player tasks from the queue.");
                }
                break;
        }

        return TaskStatus.SUCCESS;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.PLAYER;
    }
}
