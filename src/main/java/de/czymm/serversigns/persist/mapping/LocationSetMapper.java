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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LocationSetMapper implements ISmartPersistenceMapper<Set<Location>> {
    private ConfigurationSection memorySection;
    private String host = "unknown";

    @Override
    public void setMemorySection(ConfigurationSection memorySection) {
        this.memorySection = memorySection;
    }

    @Override
    public Set<Location> getValue(String path) {
        List<String> strings = memorySection.getStringList(path);
        Set<Location> locs = new HashSet<>();

        for (String str : strings) {
            Location loc = stringToLocation(str);
            if (loc == null) continue;
            locs.add(loc);
        }

        return locs;
    }

    @Override
    public void setValue(String path, Set<Location> val) {
        ArrayList<String> list = new ArrayList<>();
        for (Location loc : val) {
            list.add(loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
        }

        memorySection.set(path, list);
    }

    @Override
    public void setHostId(String id) {
        host = id;
    }

    private Location stringToLocation(String input) {
        try {
            String[] split = input.split(",");
            if (split.length < 4) return null;

            int z = Integer.parseInt(split[split.length - 1]);
            int y = Integer.parseInt(split[split.length - 2]);
            int x = Integer.parseInt(split[split.length - 3]);

            String worldName = "";
            for (int k = 0; k <= split.length - 4; k++) {
                worldName += split[k] + ",";
            }
            if (worldName.isEmpty()) return null;
            worldName = worldName.substring(0, worldName.length() - 1);

            World world = Bukkit.getWorld(worldName);
            if (world == null) return null;

            return new Location(world, x, y, z);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
