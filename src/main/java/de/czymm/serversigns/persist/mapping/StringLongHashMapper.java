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

import java.util.HashMap;
import java.util.Map.Entry;

public class StringLongHashMapper implements IPersistenceMapper<HashMap<String, Long>> {
    private MemorySection memorySection;

    @Override
    public void setMemorySection(MemorySection memorySection) {
        this.memorySection = memorySection;
    }

    @Override
    public HashMap<String, Long> getValue(String path) {
        HashMap<String, Long> map = new HashMap<>();
        if (memorySection.getConfigurationSection(path) == null) return map;
        for (String key : memorySection.getConfigurationSection(path).getKeys(false)) {
            map.put(key, memorySection.getLong(path + "." + key));
        }

        return map;
    }

    @Override
    public void setValue(String path, HashMap<String, Long> val) {
        for (Entry<?, ?> entry : val.entrySet()) {
            memorySection.set(path + "." + entry.getKey().toString(), entry.getValue());
        }
    }
}
