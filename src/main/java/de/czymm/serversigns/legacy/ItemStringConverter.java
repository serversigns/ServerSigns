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

package de.czymm.serversigns.legacy;

import de.czymm.serversigns.utils.MaterialConvertor;
import de.czymm.serversigns.utils.NumberUtils;
import org.bukkit.Material;

public class ItemStringConverter {

    public static String convertPreV4String(String input) {
        if (input.contains(" ") && !input.split(" ")[0].contains(".")) {
            String newInput = "";
            String[] split = input.split(" ");
            if (split.length >= 3) // If it's less than 3, it's invalid anyway
            {
                for (int k = 0; k < split.length; k++) {
                    switch (k) {
                        case 0:
                            Material mat = MaterialConvertor.getMaterialById(NumberUtils.parseInt(split[k], -1));
                            if (mat != null) {
                                newInput += "id." + mat.toString() + " ";
                            } else {
                                return input;
                            }
                            break;

                        case 1:
                            newInput += "am." + split[k] + " ";
                            break;
                        case 2:
                            newInput += "du." + split[k] + " ";
                            break;
                        case 3:
                            if (!split[k].startsWith("name")) {
                                newInput += "na." + split[k] + " ";
                            } else {
                                newInput += "na." + split[k].substring(5) + " ";
                            }
                            break;
                        case 4:
                            if (!split[k].equals("#91643")) {
                                if (!split[k].startsWith("lore")) {
                                    newInput += "lo." + split[k] + " ";
                                } else {
                                    newInput += "lo." + split[k].substring(5) + " ";
                                }
                            }
                            break;
                        case 5:
                            if (!split[k].startsWith("lore")) {
                                newInput += "lo." + split[k] + " ";
                            } else {
                                newInput += "lo." + split[k].substring(5) + " ";
                            }
                            break;
                        default:
                            if (split[k].startsWith("name:") || split[k].startsWith("name.")) {
                                newInput += "na." + split[k].substring(5) + " ";
                            } else if (split[k].startsWith("lore:") || split[k].startsWith("lore.")) {
                                newInput += "lo." + split[k].substring(5) + " ";
                            } else {
                                newInput += split[k] + " ";
                            }
                            break;
                    }
                }
                input = newInput;
            }
        }

        return input.trim();
    }
}
