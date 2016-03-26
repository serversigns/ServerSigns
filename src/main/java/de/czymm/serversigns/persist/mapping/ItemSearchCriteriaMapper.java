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

import de.czymm.serversigns.itemdata.ItemSearchCriteria;
import org.bukkit.configuration.MemorySection;

public class ItemSearchCriteriaMapper implements IPersistenceMapper<ItemSearchCriteria> {
    private MemorySection memorySection;

    @Override
    public void setMemorySection(MemorySection memorySection) {
        this.memorySection = memorySection;
    }

    @Override
    public ItemSearchCriteria getValue(String path) {
        if (memorySection.getConfigurationSection(path) == null)
            return new ItemSearchCriteria(false, false, false, false);
        return new ItemSearchCriteria(memorySection.getBoolean(path + ".enchants"), memorySection.getBoolean(path + ".name"),
                memorySection.getBoolean(path + ".lores"), memorySection.getBoolean(path + ".durability"));
    }

    @Override
    public void setValue(String path, ItemSearchCriteria val) {
        memorySection.set(path + ".enchants", val.getEnchantsCriteria());
        memorySection.set(path + ".name", val.getIgnoreName());
        memorySection.set(path + ".lores", val.getIgnoreLore());
        memorySection.set(path + ".durability", val.getIgnoreDurability());
    }
}
