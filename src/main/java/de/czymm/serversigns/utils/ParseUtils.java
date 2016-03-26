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

package de.czymm.serversigns.utils;

import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.List;

public class ParseUtils {
    public static class Alias {
        private String[] aliases;

        public Alias(String... aliases) {
            this.aliases = aliases;
        }

        public String[] getAliases() {
            return aliases;
        }

        public boolean matches(String input) {
            for (String str : aliases) {
                if (input.equalsIgnoreCase(str))
                    return true;
            }
            return false;
        }
    }

    private static Enchantment[] enchantList;
    private static Alias[] enchantAliases;

    static {
        enchantList = new Enchantment[25];
        enchantAliases = new Alias[25];

        enchantList[0] = Enchantment.ARROW_DAMAGE;
        enchantAliases[0] = new Alias("Power", "ARROW_DAMAGE");

        enchantList[1] = Enchantment.ARROW_FIRE;
        enchantAliases[1] = new Alias("Flame", "ARROW_FIRE");

        enchantList[2] = Enchantment.ARROW_INFINITE;
        enchantAliases[2] = new Alias("Infinity", "ARROW_INFINITE");

        enchantList[3] = Enchantment.ARROW_KNOCKBACK;
        enchantAliases[3] = new Alias("Punch", "ARROW_KNOCKBACK");

        enchantList[4] = Enchantment.ARROW_KNOCKBACK;
        enchantAliases[4] = new Alias("Punch", "ARROW_KNOCKBACK");

        enchantList[5] = Enchantment.DAMAGE_ALL;
        enchantAliases[5] = new Alias("Sharpness", "DAMAGE_ALL");

        enchantList[6] = Enchantment.DAMAGE_ARTHROPODS;
        enchantAliases[6] = new Alias("Bane of Arthropods", "baneofarthropods", "DAMAGE_ARTHROPODS");

        enchantList[7] = Enchantment.DAMAGE_UNDEAD;
        enchantAliases[7] = new Alias("Smite", "DAMAGE_UNDEAD");

        enchantList[8] = Enchantment.DIG_SPEED;
        enchantAliases[8] = new Alias("Efficiency", "DIG_SPEED");

        enchantList[9] = Enchantment.DURABILITY;
        enchantAliases[9] = new Alias("Unbreaking", "DURABILITY");

        enchantList[10] = Enchantment.FIRE_ASPECT;
        enchantAliases[10] = new Alias("Fire Aspect", "fireaspect", "FIRE_ASPECT");

        enchantList[11] = Enchantment.KNOCKBACK;
        enchantAliases[11] = new Alias("Knockback", "KNOCKBACK");

        enchantList[12] = Enchantment.LOOT_BONUS_BLOCKS;
        enchantAliases[12] = new Alias("Fortune", "LOOT_BONUS_BLOCKS");

        enchantList[13] = Enchantment.LOOT_BONUS_MOBS;
        enchantAliases[13] = new Alias("Looting", "LOOT_BONUS_MOBS");

        enchantList[14] = Enchantment.OXYGEN;
        enchantAliases[14] = new Alias("Respiration", "OXYGEN");

        enchantList[15] = Enchantment.PROTECTION_ENVIRONMENTAL;
        enchantAliases[15] = new Alias("Protection", "PROTECTION_ENVIRONMENTAL");

        enchantList[16] = Enchantment.PROTECTION_EXPLOSIONS;
        enchantAliases[16] = new Alias("Blast Protection", "blastprotection", "PROTECTION_EXPLOSIONS");

        enchantList[17] = Enchantment.PROTECTION_FALL;
        enchantAliases[17] = new Alias("Feather Falling", "featherfalling", "PROTECTION_FALL");

        enchantList[18] = Enchantment.PROTECTION_FIRE;
        enchantAliases[18] = new Alias("Fire Protection", "fireprotection", "PROTECTION_FIRE");

        enchantList[19] = Enchantment.PROTECTION_PROJECTILE;
        enchantAliases[19] = new Alias("Projectile Protection", "projectileprotection", "PROTECTION_PROJECTILE");

        enchantList[20] = Enchantment.SILK_TOUCH;
        enchantAliases[20] = new Alias("Silk Touch", "silktouch", "SILK_TOUCH");

        enchantList[21] = Enchantment.THORNS;
        enchantAliases[21] = new Alias("Thorns", "THORNS");

        enchantList[22] = Enchantment.WATER_WORKER;
        enchantAliases[22] = new Alias("Aqua Affinity", "aquaaffinity", "WATER_WORKER");

        enchantList[23] = Enchantment.LUCK;
        enchantAliases[23] = new Alias("Luck of The Sea", "luckofthesea", "LUCK");

        enchantList[24] = Enchantment.LURE;
        enchantAliases[24] = new Alias("Lure", "LURE");
    }

    public static Enchantment getEnchantmentByName(String name) {
        if (name.isEmpty()) return null;

        for (int j = 0; j < enchantAliases.length; j++)
            if (enchantAliases[j].matches(name))
                return enchantList[j];
        return null;
    }

    public static String getStringFromEnchantment(Enchantment e) {
        if (e == null) return null;

        for (int j = 0; j < enchantAliases.length; j++)
            if (enchantList[j].equals(e))
                return enchantAliases[j].getAliases()[0];
        return null;
    }

    public static String colorToString(org.bukkit.Color c) {
        if (c == null) return "";
        return c.getRed() + "," + c.getGreen() + "," + c.getBlue();
    }

    public static org.bukkit.Color stringToColor(String s) {
        if (!s.contains(",")) return null;
        List<Integer> intList = new ArrayList<>();
        for (String st : s.split(",")) {
            if (!NumberUtils.isInt(st)) return null;
            intList.add(NumberUtils.parseInt(st));
        }
        if (intList.size() < 3) return null;
        return org.bukkit.Color.fromRGB(intList.get(0), intList.get(1), intList.get(2));
    }
}
