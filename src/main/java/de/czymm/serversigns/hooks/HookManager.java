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

import de.czymm.serversigns.ServerSignsPlugin;

public class HookManager {

    public final HookWrapper<EssentialsHook> essentials;
    public final HookWrapper<NoCheatPlusHook> noCheatPlus;
    public final HookWrapper<VaultHook> vault;
    public final HookWrapper<PlaceholderAPIHook> placeholderAPI;


    public HookManager(ServerSignsPlugin plugin) {
        essentials = new HookWrapper<>(EssentialsHook.class, new Class[]{ServerSignsPlugin.class}, new Object[]{plugin});
        noCheatPlus = new HookWrapper<>(NoCheatPlusHook.class, new Class[]{ServerSignsPlugin.class}, new Object[]{plugin});
        vault = new HookWrapper<>(VaultHook.class, new Class[]{ServerSignsPlugin.class}, new Object[]{plugin});
        placeholderAPI = new HookWrapper<>(PlaceholderAPIHook.class, new Class[]{ServerSignsPlugin.class}, new Object[]{plugin});
    }

    public void tryInstantiateHooks(boolean deepVerbose) {
        try {
            essentials.instantiateHook();
        } catch (Exception ex) {
            if (deepVerbose) ServerSignsPlugin.log("Unable to load Essentials hook");
        }
        try {
            noCheatPlus.instantiateHook();
        } catch (Exception ex) {
            if (deepVerbose) ServerSignsPlugin.log("Unable to load No Cheat Plus hook");
        }
        try {
            vault.instantiateHook();
        } catch (Exception ex) {
            ServerSignsPlugin.log("Unable to load Vault dependency - certain economy and permission features will be disabled");
            ServerSignsPlugin.log("Please download Vault at http://dev.bukkit.org/server-mods/vault/ for Economy and \"Permission grant\" support.");
        }
        try {
        	placeholderAPI.instantiateHook();
        } catch (Exception ex) {
            ServerSignsPlugin.log("Unable to load PlaceholderAPI dependency - placeholder features will be disabled");
        }
    }
}
