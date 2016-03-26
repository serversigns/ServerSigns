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

package de.czymm.serversigns.legacy;

import de.czymm.serversigns.ServerSignsPlugin;
import de.czymm.serversigns.taskmanager.TaskManagerTask;
import de.czymm.serversigns.taskmanager.tasks.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QueueToTaskConverter {

    public static class QueueToTaskResult {
        private long highestId;
        private List<TaskManagerTask> tasks;

        public QueueToTaskResult(long highestId, List<TaskManagerTask> tasks) {
            this.highestId = highestId;
            this.tasks = tasks;
        }

        public long getHighestId() {
            return highestId;
        }

        public List<TaskManagerTask> getTasks() {
            return tasks;
        }
    }

    // Legacy code for pre v4.3

    public static QueueToTaskResult convertFile(Path dataFolder) {
        List<TaskManagerTask> converted = new ArrayList<>();
        long yamlCurrentId = -1;

        Path queueYml = dataFolder.resolve("taskQueue.yml");
        if (Files.exists(queueYml)) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(queueYml.toFile());
            yamlCurrentId = yaml.getInt("currentTaskID");
            processSectionUpdate(yaml.getConfigurationSection("tasks"), converted);
        }

        Path playerTasks = dataFolder.resolve("playerJoinTasks.yml");
        if (Files.exists(playerTasks)) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(playerTasks.toFile());
            processSectionUpdate(yaml.getConfigurationSection("tasks"), converted);
        }

        // Delete the old files
        try {
            Files.deleteIfExists(queueYml);
            Files.deleteIfExists(playerTasks);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (converted.size() > 0 && yamlCurrentId > 0) {
            ServerSignsPlugin.log("Successfully converted " + converted.size() + " tasks to the new TaskManager system and set currentId to " + (yamlCurrentId + 1));
            return new QueueToTaskResult(yamlCurrentId + 1, converted);
        }

        return null;
    }

    private static void processSectionUpdate(ConfigurationSection section, List<TaskManagerTask> tasks) {
        if (section != null) {
            for (String taskId : section.getKeys(false)) {
                try {
                    ConfigurationSection subSection = section.getConfigurationSection(taskId);
                    long timestamp = subSection.getLong("timestamp");
                    String type = subSection.getString("type");

                    switch (type) {
                        case "playerCommand":
                            tasks.add(new PlayerActionTask(timestamp, PlayerActionTaskType.COMMAND, subSection.getString("command"), UUID.fromString(subSection.getString("playerUUID")), true));
                            break;
                        case "playerMessage":
                            tasks.add(new PlayerActionTask(timestamp, subSection.getBoolean("blankMessage") ? PlayerActionTaskType.BLANK_MESSAGE : PlayerActionTaskType.MESSAGE, subSection.getString("message"), UUID.fromString(subSection.getString("playerUUID")), true));
                            break;
                        case "playerChat":
                            tasks.add(new PlayerActionTask(timestamp, subSection.getBoolean("op") ? PlayerActionTaskType.OP_COMMAND : PlayerActionTaskType.CHAT, subSection.getString("message"), UUID.fromString(subSection.getString("playerUUID")), true));
                            break;
                        case "serverCommand":
                            tasks.add(new ServerActionTask(timestamp, ServerActionTaskType.COMMAND, subSection.getString("command"), true));
                            break;
                        case "serverMessage":
                            tasks.add(new ServerActionTask(timestamp, ServerActionTaskType.BROADCAST, subSection.getString("message"), true));
                            break;
                        case "permissionGrant":
                            @SuppressWarnings("unchecked")
                            ArrayList<String> permissions = (ArrayList<String>) subSection.get("permissions");

                            for (String perm : permissions) {
                                tasks.add(new PermissionGrantPlayerTask(timestamp, perm, UUID.fromString(subSection.getString("playerUUID")), true));
                            }
                            break;
                        case "permissionRemove":
                            if (subSection.contains("changedPermissions")) {
                                @SuppressWarnings("unchecked")
                                ArrayList<String> changedPermissions = (ArrayList<String>) subSection.get("changedPermissions");

                                for (String perm : changedPermissions) {
                                    tasks.add(new PermissionRemovePlayerTask(timestamp, perm, UUID.fromString(subSection.getString("playerUUID")), true));
                                }
                            }
                            break;
                    }
                } catch (Throwable thrown) {
                    ServerSignsPlugin.log("Unable to convert task (id " + taskId + ") as the data is too old to reliably update.");
                }
            }
        }
    }
}
