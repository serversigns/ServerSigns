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

package de.czymm.serversigns.itemdata;

public enum ItemPart {
    TYPE(ItemType.get()),
    AMOUNT(ItemAmount.get()),
    DAMAGE(ItemDamage.get()),
    ENCHANTS(ItemEnchants.get()),
    NAME(ItemName.get()),
    LORES(ItemLore.get()),
    COLOURS(ItemColour.get())

    // End
    ;

    private ItemData attached;
    private String[] prefixes;

    ItemPart(ItemData attached) {
        this.attached = attached;
        this.prefixes = attached.getMatcherPrefix();
    }

    public ItemData getAttachedData() {
        return this.attached;
    }

    public String[] getPrefixes() {
        return this.prefixes;
    }

    public static ItemPart getPartFromPrefix(String input) {
        if (input.length() < 2) return null;

        for (ItemPart part : ItemPart.values()) {
            for (String prefix : part.getPrefixes()) {
                if (prefix.endsWith(".")) {
                    if (input.equalsIgnoreCase(prefix.substring(0, prefix.length() - 1)))
                        return part;
                } else if (input.equalsIgnoreCase(prefix))
                    return part;
            }
        }

        return null;
    }
}
