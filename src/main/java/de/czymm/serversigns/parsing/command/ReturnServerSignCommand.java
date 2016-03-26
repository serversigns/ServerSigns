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

package de.czymm.serversigns.parsing.command;

import de.czymm.serversigns.ServerSignsPlugin;
import de.czymm.serversigns.parsing.CommandType;
import de.czymm.serversigns.taskmanager.TaskManagerTask;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReturnServerSignCommand extends ServerSignCommand {

    public ReturnServerSignCommand() {
        super(CommandType.RETURN, "");
    }

    // Overrides

    @Override
    public boolean isAlwaysPersisted() {
        return false;
    }

    @Override
    public void setAlwaysPersisted(boolean val) {
        // Do nothing
    }

    @Override
    public CommandType getType() {
        return type;
    }

    @Override
    public List<String> getGrantPermissions() {
        return new ArrayList<>();
    }

    @Override
    public long getDelay() {
        return 0;
    }

    @Override
    public void setDelay(long delay) {
        // Do nothing
    }

    @Override
    public void setGrantPermissions(List<String> grant) {
        // Do nothing
    }

    @Override
    public String getUnformattedCommand() {
        return command;
    }

    @Override
    public String getFormattedCommand(Player executor, ServerSignsPlugin plugin, Map<String, String> injectedReplacements) {
        return command;
    }

    @Override
    public List<TaskManagerTask> getTasks(Player executor, ServerSignsPlugin plugin, Map<String, String> injectedReplacements) {
        return new ArrayList<>(); // No tasks to be executed for a halt command
    }
}
