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
import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class PlayerTask<T extends Enum<T>> extends TaskManagerTask<T> {
    private final UUID playerUniqueId;

    public PlayerTask(long runAt, T actionType, String action, UUID playerUniqueId, boolean alwaysPersisted) {
        super(runAt, actionType, action, alwaysPersisted);
        this.playerUniqueId = playerUniqueId;
    }

    protected PlayerTask(long taskID, long runAt, T type, String action, UUID playerUniqueId, boolean alwaysPersisted) {
        super(taskID, runAt, type, action, alwaysPersisted);
        this.playerUniqueId = playerUniqueId;
    }

    public UUID getPlayerUniqueId() {
        return playerUniqueId;
    }

    @Override
    public TaskStatus runTask(ServerSignsPlugin plugin) {
        Player player = Bukkit.getPlayer(getPlayerUniqueId());
        if (player == null) {
            return TaskStatus.PLAYER_NOT_ONLINE;
        }

        return runPlayerTask(plugin, player);
    }

    protected abstract TaskStatus runPlayerTask(ServerSignsPlugin plugin, Player player);

    @Override
    public String toString() {
        return "PlayerTask{" +
                "data='" + getData() + '\'' +
                "} " + super.toString();
    }
}
