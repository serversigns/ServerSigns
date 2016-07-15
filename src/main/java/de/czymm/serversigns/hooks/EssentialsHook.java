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

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import de.czymm.serversigns.ServerSignsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class EssentialsHook {

    protected ServerSignsPlugin pl;

    private Essentials essentials;

    public EssentialsHook(ServerSignsPlugin plugin) throws Exception {
        pl = plugin;
        essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        if (essentials == null || !essentials.isEnabled()) throw new Exception();
    }

    public String getNickname(Player player) {
        try {
            User user = essentials.getUser(player);
            if (user != null) {
                return user.getNickname();
            }

            return player.getDisplayName();
        } catch (Throwable thrown) {
            // Catch any errors thrown and just return the player's name
            // as it means we're unable to obtain their nickname anyway
            return player.getName();
        }
    }
}
