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

import org.bukkit.configuration.MemorySection;

import java.util.UUID;

public class PlayerUniqueIdMapper implements IPersistenceMapper<UUID> {
    private MemorySection memorySection;

    @Override
    public void setMemorySection(MemorySection memorySection) {
        this.memorySection = memorySection;
    }

    @Override
    public UUID getValue(String path) {
        String string = memorySection.getString(path);
        try {
            return UUID.fromString(string);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @Override
    public void setValue(String path, UUID value) {
        memorySection.set(path, value.toString());
    }
}
