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
import de.czymm.serversigns.itemdata.ItemStringParser;
import de.czymm.serversigns.utils.ItemUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemStackListMapper implements ISmartPersistenceMapper<List<ItemStack>> {
    private ConfigurationSection memorySection;
    private String host = "unknown";

    @Override
    public void setMemorySection(ConfigurationSection memorySection) {
        this.memorySection = memorySection;
    }

    @Override
    public List<ItemStack> getValue(String path) {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        List<String> rawStrings = memorySection.getStringList(path);

        for (String raw : rawStrings) {
            ItemStringParser parser = new ItemStringParser(raw);
            ItemStack stack = parser.parse();
            if (stack == null) continue;

            if (parser.encounteredErrors()) {
                ServerSignsPlugin.log("Encountered errors while building ItemStack from '" + host + "'");
            }
            stacks.add(stack);
        }

        return stacks;
    }

    @Override
    public void setValue(String path, List<ItemStack> val) {
        ArrayList<String> list = new ArrayList<>();
        for (ItemStack stack : val) {
            list.add(ItemUtils.getStringFromItemStack(stack));
        }

        memorySection.set(path, list);
    }

    @Override
    public void setHostId(String id) {
        host = id;
    }
}
