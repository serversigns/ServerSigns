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

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class BlocksMapper implements IPersistenceMapper<EnumSet<Material>> {
    private ConfigurationSection memorySection;

    @Override
    public void setMemorySection(ConfigurationSection memorySection) {
        this.memorySection = memorySection;
    }

    @Override
    public EnumSet<Material> getValue(String path, Class<?> valueClass) {
        List<String> blocksList = memorySection.getStringList(path);
        EnumSet<Material> blocks = EnumSet.noneOf(Material.class);
        for (String block : blocksList) {
            Material material = Material.getMaterial(block);
            if (material != null) {
                blocks.add(material);
            }
        }
        return blocks;
    }

    @Override
    public void setValue(String path, EnumSet<Material> val) {
        ArrayList<String> list = new ArrayList<>();
        for (Object material : val) {
            list.add(material.toString());
        }

        memorySection.set(path, list);
    }
}
