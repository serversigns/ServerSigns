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

package de.czymm.serversigns.hooks;

public class HookWrapper<E> {

    private E hook;
    private boolean hooked = false;

    private Class<E> clazz;
    private Class<?>[] paramClasses;
    private Object[] paramObjects;

    public HookWrapper(Class<E> instantiationClass, Class[] parameterClasses, Object[] parameterObjects) {
        this.clazz = instantiationClass;
        this.paramClasses = parameterClasses;
        this.paramObjects = parameterObjects;
    }

    public void instantiateHook() throws Exception {
        hook = clazz.getConstructor(paramClasses).newInstance(paramObjects);
        hooked = true;
    }

    public boolean isHooked() {
        return hooked;
    }

    public void setHooked(boolean val) {
        hooked = val;
    }

    public E getHook() {
        return hook;
    }
}