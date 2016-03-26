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

package de.czymm.serversigns.meta;

import java.util.Arrays;

public class SVSMeta {
    private SVSMetaKey key;
    private SVSMetaValue[] values;

    public SVSMeta(SVSMetaKey key, SVSMetaValue... values) {
        this.key = key;
        this.values = values;
    }

    public SVSMetaKey getKey() {
        return key;
    }

    public void addValue(SVSMetaValue value) {
        SVSMetaValue[] array = new SVSMetaValue[values.length + 1];
        System.arraycopy(values, 0, array, 0, values.length);

        array[values.length] = value;
        values = array;
    }

    public SVSMetaValue removeValue(int index) {
        if (index >= values.length || index < 0)
            return null;

        SVSMetaValue before = values[index];
        System.arraycopy(values, index + 1, values, index, values.length - 1 - index);

        values = Arrays.copyOf(values, values.length - 1);

        return before;
    }

    public SVSMetaValue getValue(int index) {
        if (index >= values.length || index < 0)
            return null;

        return values[index];
    }

    public SVSMetaValue getValue() {
        return getValue(0);
    }

    public boolean hasValue(int index) {
        return index >= 0 && index < values.length;
    }

    public SVSMetaValue[] getValues() {
        return values;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SVSMeta) {
            SVSMeta meta = (SVSMeta) other;
            if (meta.getKey().equals(getKey()) && meta.getValues().length == getValues().length) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((key == null) ? 0 : key.hashCode());
        result = prime * result + (values == null ? 1231 : 1237);
        result = prime * result + ((values == null) ? 0 : values.length * prime);
        return result;
    }
}
