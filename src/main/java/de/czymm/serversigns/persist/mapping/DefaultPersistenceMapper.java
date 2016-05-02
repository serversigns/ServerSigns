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

import org.bukkit.configuration.ConfigurationSection;

public class DefaultPersistenceMapper implements IPersistenceMapper<Object> {
    private ConfigurationSection memorySection;

    @Override
    public void setMemorySection(ConfigurationSection memorySection) {
        this.memorySection = memorySection;
    }

    @Override
    public Object getValue(String path) {
        return memorySection.get(path);
    }

    @Override
    public void setValue(String path, Object value) {
        memorySection.set(path, value);
    }
}
