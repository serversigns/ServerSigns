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

import de.czymm.serversigns.ServerSignsPlugin;
import de.czymm.serversigns.utils.MaterialConvertor;
import de.czymm.serversigns.utils.NumberUtils;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;

public class BlocksIdMapper implements IPersistenceMapper<EnumSet<Material>> {
    private MemorySection memorySection;

    @Override
    public void setMemorySection(MemorySection memorySection) {
        this.memorySection = memorySection;
    }

    @Override
    public EnumSet<Material> getValue(String path) {
        List<String> unknownMaterials = new ArrayList<>();
        List<String> blocksList = memorySection.getStringList(path);
        EnumSet<Material> blocks = EnumSet.noneOf(Material.class);

        for (String blockId : blocksList) {
            int val = NumberUtils.parseInt(blockId, -1);
            Material material = MaterialConvertor.getMaterialById(val);
            if (material != null) {
                blocks.add(material);
            } else {
                unknownMaterials.add(blockId);
            }
        }

        if (!unknownMaterials.isEmpty()) {
            ServerSignsPlugin.log("Wrong blocks : " + String.join(", ", unknownMaterials), Level.WARNING);
            ServerSignsPlugin.log("Please visit the wiki : https://serversigns.de/wiki/Configuration", Level.WARNING);
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
