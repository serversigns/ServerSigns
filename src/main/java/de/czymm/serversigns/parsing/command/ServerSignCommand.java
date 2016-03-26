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

package de.czymm.serversigns.parsing.command;

import de.czymm.serversigns.ServerSignsPlugin;
import de.czymm.serversigns.parsing.CommandType;
import de.czymm.serversigns.taskmanager.TaskManagerTask;
import de.czymm.serversigns.taskmanager.tasks.*;
import de.czymm.serversigns.utils.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerSignCommand implements Serializable {

    protected CommandType type;
    protected String command;

    private long delay = 0;
    private boolean alwaysPersisted = false;
    private int interactValue = 0; // 0 = both, 1 = left, 2 = right
    private List<String> grant = new ArrayList<>();

    public ServerSignCommand(CommandType type, String command) {
        this.type = type;
        this.command = command;
    }

    public int getInteractValue() {
        return interactValue;
    }

    public void setInteractValue(int val) {
        if (val < 0 || val > 2) throw new IllegalArgumentException("Value cannot be < 0 or > 2");
        interactValue = val;
    }

    public boolean isAlwaysPersisted() {
        return alwaysPersisted;
    }

    public void setAlwaysPersisted(boolean val) {
        alwaysPersisted = val;
    }

    public CommandType getType() {
        return type;
    }

    public List<String> getGrantPermissions() {
        return grant;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public void setGrantPermissions(List<String> grant) {
        this.grant = grant;
    }

    public String getUnformattedCommand() {
        return command;
    }

    public String getFormattedCommand(Player executor, ServerSignsPlugin plugin, Map<String, String> injectedReplacements) {
        String ret = command;
        if (executor != null) {
            try {
                if (ret.contains("<group>")) {
                    ret = ret.replaceAll("<group>", plugin.hookManager.vault.getHook().getPermission().getPrimaryGroup(executor));
                }
            } catch (Throwable thrown) {
                ServerSignsPlugin.log("Vault is not properly hooked into Permissions! Unable to parse <group> parameters");
            }

            ret = ret.replaceAll("<player>", executor.getName());
            ret = ret.replaceAll("<name>", executor.getName());
            ret = ret.replaceAll("<uuid>", executor.getUniqueId().toString());
            if (plugin.hookManager.essentials.isHooked()) {
                String nick = plugin.hookManager.essentials.getHook().getNickname(executor);
                ret = ret.replaceAll("<nick>", Matcher.quoteReplacement(nick == null ? executor.getName() : nick));
            }
        }

        if (injectedReplacements != null) {
            for (Map.Entry<String, String> entry : injectedReplacements.entrySet()) {
                ret = Pattern.compile(entry.getKey(), Pattern.CASE_INSENSITIVE).matcher(ret).replaceAll(Matcher.quoteReplacement(entry.getValue()));
            }
        }

        ret = ret.trim();
        if (ret.startsWith("/")) {
            ret = ret.replaceFirst("/", "");
        }

        return de.czymm.serversigns.utils.StringUtils.colour(formatRandoms(ret));
    }

    private static final Pattern RANDOM_PATTERN = Pattern.compile("<r:(-?\\d+)-(-?\\d+)>");

    private String formatRandoms(String input) {
        Matcher matcher;
        while ((matcher = RANDOM_PATTERN.matcher(input)).find()) {
            int random = NumberUtils.randomBetweenInclusive(NumberUtils.parseInt(matcher.group(1)), NumberUtils.parseInt(matcher.group(2)));
            input = matcher.replaceFirst(random + "");
        }

        return input;
    }

    public List<TaskManagerTask> getTasks(Player executor, ServerSignsPlugin plugin, Map<String, String> injectedReplacements) {
        List<TaskManagerTask> tasks = new ArrayList<>();
        List<PermissionGrantPlayerTask> grantTasks = new ArrayList<>();

        if (executor != null) {
            for (String perm : getGrantPermissions()) {
                PermissionGrantPlayerTask grantTask = new PermissionGrantPlayerTask(getTimestamp(), perm, executor.getUniqueId(), isAlwaysPersisted());
                tasks.add(grantTask);
                grantTasks.add(grantTask);
            }
        }

        Object taskObj = getType().getTaskObject();
        if (getType() == CommandType.SERVER_COMMAND && StringUtils.containsIgnoreCase(getUnformattedCommand(), "<player>")) {
            taskObj = PlayerActionTaskType.SERVER_COMMAND;
        }

        if (taskObj instanceof PlayerActionTaskType && executor != null) {
            tasks.add(new PlayerActionTask(getTimestamp(), (PlayerActionTaskType) taskObj, getFormattedCommand(executor, plugin, injectedReplacements), executor.getUniqueId(), isAlwaysPersisted()));
        } else if (taskObj instanceof ServerActionTaskType) {
            tasks.add(new ServerActionTask(getTimestamp(), (ServerActionTaskType) taskObj, getFormattedCommand(executor, plugin, injectedReplacements), isAlwaysPersisted()));
        } else if (taskObj instanceof PermissionGrantPlayerTaskType && executor != null) {
            tasks.add(new PermissionGrantPlayerTask(getTimestamp(), getFormattedCommand(executor, plugin, injectedReplacements), executor.getUniqueId(), isAlwaysPersisted()));
        } else if (taskObj instanceof PermissionRemovePlayerTaskType && executor != null) {
            tasks.add(new PermissionRemovePlayerTask(getTimestamp(), getFormattedCommand(executor, plugin, injectedReplacements), executor.getUniqueId(), isAlwaysPersisted()));
        }

        if (executor != null && !grantTasks.isEmpty()) {
            for (PermissionGrantPlayerTask grantTask : grantTasks) {
                tasks.add(new PermissionRemovePlayerTask(getTimestamp(), executor.getUniqueId(), grantTask, isAlwaysPersisted()));
            }
        }

        return tasks;
    }

    private long getTimestamp() {
        if (getDelay() == 0) return 0;
        return System.currentTimeMillis() + (getDelay() * 1000);
    }
}