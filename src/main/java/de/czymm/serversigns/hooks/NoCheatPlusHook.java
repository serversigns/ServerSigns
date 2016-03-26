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
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.UUID;

public class NoCheatPlusHook {
    protected ServerSignsPlugin pl;

    public NoCheatPlusHook(ServerSignsPlugin plugin) throws Exception {
        pl = plugin;

        Plugin ncpPlugin = Bukkit.getPluginManager().getPlugin("NoCheatPlus");
        if (ncpPlugin != null && ncpPlugin.isEnabled()) {
            // Make sure it supports UUIDs
            Method exemptMethod;
            try {
                exemptMethod = NCPExemptionManager.class.getMethod("exemptPermanently", UUID.class, CheckType.class);
            } catch (NoSuchMethodException e) {
                Bukkit.getLogger().severe("Your version of NoCheatPlus is not supported by ServerSigns as it does not support UUIDs. Please use v3.12 or higher");
                throw e;
            } catch (SecurityException e) {
                Bukkit.getLogger().severe("Unable to determine NoCheatPlus compatibility, supported features may not function as intended");
                throw e;
            }

            if (exemptMethod == null) {
                Bukkit.getLogger().severe("Your version of NoCheatPlus is not supported by ServerSigns as it does not support UUIDs. Please use v3.12 or higher");
                throw new Exception();
            }
        } else {
            throw new Exception("NoCheatPlus not loaded");
        }
    }

    public boolean exemptTemporarily(Player player, String checkType, long lengthInTicks) {
        final CheckType type = CheckType.valueOf(checkType.toUpperCase());
        if (type == null) return false;

        final UUID id = player.getUniqueId();
        NCPExemptionManager.exemptPermanently(id, type);

        Bukkit.getScheduler().runTaskLater(pl, new Runnable() {
            public void run() {
                NCPExemptionManager.unexempt(id, type);
            }
        }, lengthInTicks);

        return true;
    }

    public boolean exemptPermanently(Player player, String checkType) {
        CheckType type = CheckType.valueOf(checkType.toUpperCase());
        if (type == null) return false;

        NCPExemptionManager.exemptPermanently(player, type);
        return true;
    }

    public boolean exemptPermanently(Player player) {
        NCPExemptionManager.exemptPermanently(player);
        return true;
    }

    public boolean unexempt(UUID player) {
        NCPExemptionManager.unexempt(player);
        return true;
    }

    public boolean unexempt(UUID player, String checkType) {
        CheckType type = CheckType.valueOf(checkType.toUpperCase());
        if (type == null) return false;

        NCPExemptionManager.unexempt(player, type);
        return true;
    }

    public boolean unexempt(Player player, String checkType) {
        CheckType type = CheckType.valueOf(checkType.toUpperCase());
        if (type == null) return false;

        NCPExemptionManager.unexempt(player, type);
        return true;
    }
}
