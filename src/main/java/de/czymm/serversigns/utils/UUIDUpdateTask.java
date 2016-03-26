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

import de.czymm.serversigns.ServerSignsPlugin;
import de.czymm.serversigns.signs.ServerSign;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.Map.Entry;

public class UUIDUpdateTask {
    private ServerSignsPlugin plugin;
    private ServerSign sign;

    public UUIDUpdateTask(ServerSignsPlugin plugin, ServerSign sign) {
        this.plugin = plugin;
        this.sign = sign;
    }

    public void updateLastUse() {
        final List<String> list = new ArrayList<>(sign.getLastUse().keySet());
        if (list.isEmpty()) return;
        ServerSignsPlugin.log("Starting UUID conversion for " + list.size() + " usernames from a ServerSign at " + sign.getLocationString());

        fillUUIDMapAsync(list);

        // Repeating task to check for when the uuidMap is ready
        new BukkitRunnable() {
            public void run() {
                if (!complete) return;
                if (uuidMap == null || uuidMap.isEmpty()) {
                    this.cancel();
                    return;
                }

                HashMap<String, Long> oldLastUse = sign.getLastUse();
                HashMap<String, Long> newLastUse = new HashMap<>();

                for (Entry<String, Long> entry : oldLastUse.entrySet()) {
                    if (!uuidMap.containsKey(entry.getKey()))
                        continue;

                    newLastUse.put(uuidMap.get(entry.getKey()).toString().trim(), entry.getValue());
                }

                sign.setLastUse(newLastUse);
                plugin.serverSignsManager.save(sign);
                this.cancel();

                ServerSignsPlugin.log("Finishing UUID conversion for " + list.size() + " usernames from a ServerSign at " + sign.getLocationString());
            }
        }.runTaskTimer(plugin, 50L, 50L);
    }

    private Map<String, UUID> uuidMap = new HashMap<>();
    private boolean complete = false;

    private void fillUUIDMapAsync(final List<String> usernames) {
        new BukkitRunnable() {
            public void run() {
                int index = 0;

                // Convert 100 at a time
                while (index <= usernames.size()) {
                    int closeIndex = index + 100;
                    if (closeIndex >= usernames.size()) closeIndex = usernames.size() - 1;

                    UUIDFetcher fetcher = new UUIDFetcher(usernames.subList(index, closeIndex), true);
                    try {
                        uuidMap.putAll(fetcher.call());
                    } catch (Exception e) {
                        if (e.getMessage().contains("429")) {
                            // Too many requests, wait a little bit
                            try {
                                Thread.sleep(600000); // Wait 10m
                                return; // Return so the index isn't incremented - we need to try this group again
                            } catch (InterruptedException ex) {
                                // do nothing
                            }
                        }
                        ServerSignsPlugin.log("We encountered an error while trying to convert usernames to UUIDs! Error as follows:");
                        e.printStackTrace();
                    }

                    index += 100;
                }

                complete = true;
            }
        }.runTaskAsynchronously(plugin);
    }
}
