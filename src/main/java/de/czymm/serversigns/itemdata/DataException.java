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

public class DataException extends Exception {
    private static final long serialVersionUID = -9036202694165168984L;
    protected static final String DEFAULT_MESSAGE = "Invalid value passed to ItemData parser!";

    public DataException() {
        super(DEFAULT_MESSAGE);
    }

    public DataException(final Throwable throwable) {
        super(DEFAULT_MESSAGE, throwable);
    }

    public DataException(final String message) {
        super(message);
    }

    public DataException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
