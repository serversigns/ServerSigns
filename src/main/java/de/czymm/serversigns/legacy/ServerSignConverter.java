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
import de.czymm.serversigns.parsing.CommandParseException;
import de.czymm.serversigns.parsing.ServerSignCommandFactory;
import de.czymm.serversigns.parsing.command.ServerSignCommand;
import de.czymm.serversigns.persist.PersistenceEntry;
import de.czymm.serversigns.signs.ClickType;
import de.czymm.serversigns.signs.ServerSign;
import de.czymm.serversigns.signs.ServerSignExecData;
import de.czymm.serversigns.signs.ServerSignManager;
import de.czymm.serversigns.utils.NumberUtils;
import de.czymm.serversigns.utils.UUIDUpdateTask;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.logging.Level;

public class ServerSignConverter {

    public static final int FILE_VERSION = 8;
    private static int currentVersion = -1;

    public static int getCurrentPersistVersion(Path signDirectory) throws IOException {
        Path verFile = signDirectory.resolve(".svs_persist_version");
        int currentVersion = 0;
        if (!Files.exists(verFile)) {
            Files.write(verFile, (FILE_VERSION + "").getBytes(), StandardOpenOption.CREATE);
        } else {
            currentVersion = NumberUtils.parseInt(new String(Files.readAllBytes(verFile)), 0);
            if (currentVersion < FILE_VERSION) {
                Files.delete(verFile);
                Files.write(verFile, (FILE_VERSION + "").getBytes(), StandardOpenOption.CREATE);
            }
        }
        return currentVersion;
    }

    public static Path performAllFileUpdates(Path signDirectory, Path signFile) throws IOException {
        if (currentVersion == -1) {
            currentVersion = getCurrentPersistVersion(signDirectory);
        }
        if (currentVersion <= 2) {
            createBackup(signDirectory);
            return updateMalformedFileName(
                    updateExecutorData(
                            updatePriceItemData(
                                    updateCommands(
                                            updatePermissions(signFile),
                                            signFile),
                                    signFile),
                            signFile),
                    signDirectory,
                    signFile
            );
        } else if (currentVersion <= 3) {
            createBackup(signDirectory);
            updateExecutorData(
            updatePriceItemData(
                    updateCommands(
                            updatePermissions(signFile),
                            signFile),
                    signFile),
                    signFile
            );
        } else if (currentVersion <= 4) {
            createBackup(signDirectory);
            updateExecutorData(
                    updateCommands(
                            updatePermissions(signFile),
                            signFile),
                    signFile
            );
        } else if (currentVersion <= 5) {
            createBackup(signDirectory);
            updateExecutorData(
                    updatePermissions(signFile),
                    signFile
            );
        } else if (currentVersion <= 7) {
            createBackup(signDirectory);
            updateExecutorData(signFile);
        }
        return signFile;
    }

    // Should be called after performAllFileUpdates
    public static void performAllServerSignUpdates(ServerSign sign, ServerSignsPlugin plugin, ServerSignManager manager) throws IOException {
        if (currentVersion <= 4) {
            createBackup(plugin.getDataFolder().toPath().resolve("signs"));
            updateLastUseMapUUIDs(sign, plugin);
            updateProtectedBlocks(sign, manager);
        } else if (currentVersion <= 6) {
            createBackup(plugin.getDataFolder().toPath().resolve("signs"));
            updateProtectedBlocks(sign, manager);
        }
    }

    // FILE_VERSION <= 2
    public static Path updateMalformedFileName(Path signDirectory, Path signFile) throws IOException {
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(signFile.toFile());
        return updateMalformedFileName(yml, signDirectory, signFile);
    }

    private static Path updateMalformedFileName(YamlConfiguration yml, Path signDirectory, Path signFile) throws IOException {
        return Files.move(signFile, signDirectory.resolve(String.format("%s_%d_%d_%d%s", yml.getString("world"), yml.getInt("X"), yml.getInt("Y"), yml.getInt("Z"), ".yml")), StandardCopyOption.REPLACE_EXISTING);
    }

    // FILE_VERSION <= 3
    public static YamlConfiguration updatePriceItemData(Path signFile) throws IOException {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(signFile.toFile());
        updatePriceItemData(yaml, signFile);
        return yaml;
    }

    private static YamlConfiguration updatePriceItemData(YamlConfiguration yml, Path signFile) throws IOException {
        List<String> piStrings = yml.getStringList("priceItems");

        for (int k = 0; k < piStrings.size(); k++) {
            String raw = piStrings.get(k);
            piStrings.set(k, ItemStringConverter.convertPreV4String(raw));
        }
        yml.set("priceItems", piStrings);
        yml.save(signFile.toFile());
        return yml;
    }

    // FILE_VERSION <= 4
    public static YamlConfiguration updateCommands(Path signFile) throws IOException {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(signFile.toFile());
        updateCommands(yaml, signFile);
        return yaml;
    }

    private static YamlConfiguration updateCommands(YamlConfiguration yaml, Path signFile) throws IOException {
        // Check if we need to update commands or not
        if (yaml.getConfigurationSection("commands") != null) return yaml;

        List<String> commands = yaml.getStringList("commands");
        if (commands.isEmpty()) return yaml; // Don't destroy empty ServerSigns
        yaml.set("commands", null);
        for (int k = 0; k < commands.size(); k++) {
            try {
                ServerSignCommand command = ServerSignCommandFactory.getCommandFromString(commands.get(k), null);
                if (command != null) {
                    yaml.set("commands." + k + ".command", command.getUnformattedCommand());
                    yaml.set("commands." + k + ".type", command.getType().toString());
                    yaml.set("commands." + k + ".delay", command.getDelay());
                    yaml.set("commands." + k + ".grantPerms", command.getGrantPermissions());
                    yaml.set("commands." + k + ".alwaysPersisted", command.isAlwaysPersisted());
                    yaml.set("commands." + k + ".interactValue", "0");
                } else {
                    ServerSignsPlugin.log("Encountered invalid command while updating " + signFile.getFileName().toString() + ": '" + commands.get(k) + "'");
                }
            } catch (CommandParseException ex) {
                ServerSignsPlugin.log(String.format("Encountered an exception while converting commands in file '%s' (%s) - this ServerSign might not perform correctly!", signFile.getFileName(), ex.getMessage()), Level.WARNING);
            }
        }
        yaml.save(signFile.toFile());
        return yaml;
    }

    public static void updateLastUseMapUUIDs(ServerSign sign, ServerSignsPlugin plugin) throws IOException {
        for (ServerSignExecData execData : sign.getServerSignExecutorData().values()) {
            for (Map.Entry<String, Long> entry : execData.getLastUse().entrySet()) {
                if (entry.getKey().length() <= 16) {
                    UUIDUpdateTask task = new UUIDUpdateTask(plugin, sign);
                    task.updateLastUse();
                    return;
                }
            }
        }
    }

    // FILE_VERSION <= 5
    public static YamlConfiguration updatePermissions(Path signFile) throws IOException {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(signFile.toFile());
        updatePermissions(yaml, signFile);
        return yaml;
    }

    private static YamlConfiguration updatePermissions(YamlConfiguration yaml, Path signFile) throws IOException {
        if (yaml.contains("permission")) {
            String perm = yaml.getString("permission");
            if (!perm.isEmpty()) {
                yaml.set("permissions", Collections.singletonList(perm));
                yaml.save(signFile.toFile());
            }
            yaml.set("permission", null);
        }
        return yaml;
    }

    // FILE_VERSION <= 6

    public static void updateProtectedBlocks(ServerSign sign, ServerSignManager manager) {
        sign.updateProtectedBlocks();
        manager.save(sign);
    }

    // FILE_VERSION <= 7

    public static YamlConfiguration updateExecutorData(Path signFile) throws IOException {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(signFile.toFile());
        updateExecutorData(yaml, signFile);
        return yaml;
    }

    public static YamlConfiguration updateExecutorData(YamlConfiguration yaml, Path signFile) throws IOException {
        // Construct new config to save to (as we're changing the whole thing basically)
        YamlConfiguration newYaml = new YamlConfiguration();

        // Everything in ExecData except commands moved to RIGHT section
        for (Field declaredField : ServerSign.class.getDeclaredFields()) {
            if (declaredField.getType().equals(Map.class)) {
                continue;
            } else if (declaredField.getType().equals(ClickType.class)) {
                PersistenceEntry configEntry = declaredField.getAnnotation(PersistenceEntry.class);
                if (configEntry != null) {
                    newYaml.set(configEntry.configPath(), "RIGHT");
                }
                continue;
            }

            PersistenceEntry configEntry = declaredField.getAnnotation(PersistenceEntry.class);
            if (configEntry != null) {
                newYaml.set(configEntry.configPath(), yaml.get(configEntry.configPath()));
            }
        }

        List<ConfigurationSection> leftClickCommands = new ArrayList<>();
        List<ConfigurationSection> rightClickCommands = new ArrayList<>();

        // Analyse each command and put into separate lists for RIGHT/LEFT sections
        for (String commandIndex : yaml.getConfigurationSection("commands").getKeys(false)) {
            int interactValue = yaml.getInt("commands." + commandIndex + ".interactValue");
            if (interactValue == 0 || interactValue == 2) {
                rightClickCommands.add(yaml.getConfigurationSection("commands." + commandIndex));
            } else if (interactValue == 1) {
                leftClickCommands.add(yaml.getConfigurationSection("commands." + commandIndex));
            }
        }

        // Construct RIGHT executor data (as all the previous data shifts to here now)
        String[] entries = leftClickCommands.isEmpty() ? new String[]{"RIGHT"} : new String[]{"RIGHT", "LEFT"};
        for (String entryType : entries) {
            for (Field declaredField : ServerSignExecData.class.getDeclaredFields()) {
                if (declaredField.getName().equals("commands")) {
                    continue;
                }

                PersistenceEntry configEntry = declaredField.getAnnotation(PersistenceEntry.class);
                if (configEntry != null) {
                    newYaml.set("executor-data." + entryType + "." + configEntry.configPath(), yaml.get(configEntry.configPath()));
                }
            }
        }

        if (!rightClickCommands.isEmpty()) {
            for (int k = 0; k < rightClickCommands.size(); k++) {
                ConfigurationSection configurationSection = rightClickCommands.get(k);
                for (String key : configurationSection.getKeys(false)) {
                    if (key.equals("interactValue")) continue;
                    newYaml.set("executor-data.RIGHT.commands." + k + "." + key, configurationSection.get(key));
                }
            }
        }
        if (!leftClickCommands.isEmpty()) {
            for (int k = 0; k < leftClickCommands.size(); k++) {
                ConfigurationSection configurationSection = leftClickCommands.get(k);
                for (String key : configurationSection.getKeys(false)) {
                    if (key.equals("interactValue")) continue;
                    newYaml.set("executor-data.LEFT.commands." + k + "." + key, configurationSection.get(key));
                }
            }
        }

        return newYaml;
    }

    // BACKUP

    private static long lastBackup = 0L;

    private static void createBackup(Path signsDir) throws IOException {
        if (lastBackup > 0) { // Already performed a backup since the last restart
            return;
        }
        lastBackup = System.currentTimeMillis();

        Path toCopyTo = signsDir.resolveSibling("signs_pre_conversion_backup");
        while (Files.isDirectory(toCopyTo)) {
            toCopyTo = signsDir.resolveSibling(toCopyTo.getFileName() + "1");
        }

        Files.createDirectory(toCopyTo);
        Iterator<Path> stream = Files.newDirectoryStream(signsDir).iterator();
        while (stream.hasNext()) {
            Path next = stream.next();
            if (Files.isDirectory(next)) continue;
            if (next.getFileName().toString().startsWith(".")) continue;
            Files.copy(next, toCopyTo.resolve(next.getFileName()));
        }
    }
}
