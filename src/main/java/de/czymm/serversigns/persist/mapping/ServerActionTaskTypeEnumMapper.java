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

package de.czymm.serversigns.persist.mapping;

import de.czymm.serversigns.taskmanager.tasks.ServerActionTaskType;
import org.bukkit.configuration.MemorySection;

public class ServerActionTaskTypeEnumMapper implements IPersistenceMapper<ServerActionTaskType> {
    private MemorySection memorySection;

    @Override
    public void setMemorySection(MemorySection memorySection) {
        this.memorySection = memorySection;
    }

    @Override
    public ServerActionTaskType getValue(String path) {
        try {
            return Enum.valueOf(ServerActionTaskType.class, memorySection.getString(path));
        } catch (IllegalArgumentException | NullPointerException ex) {
            return null;
        }
    }

    @Override
    public void setValue(String path, ServerActionTaskType value) {
        memorySection.set(path, value.toString());
    }
}
