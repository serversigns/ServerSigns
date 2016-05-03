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

package de.czymm.serversigns.signs;

import de.czymm.serversigns.ServerSignsPlugin;
import de.czymm.serversigns.legacy.ServerSignConverter;
import de.czymm.serversigns.persist.PersistenceException;
import de.czymm.serversigns.persist.YamlFieldPersistence;
import de.czymm.serversigns.persist.mapping.MappingException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.material.Door;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

public class ServerSignManager {
    private final ServerSignsPlugin plugin;
    private HashMap<Location, ServerSign> signs = new HashMap<>();

    private final Path SIGNS_DIRECTORY;
    private final Path INVALID_SIGNS_PARENT_DIRECTORY;
    private final Path INVALID_SIGNS_LOCATION_DIRECTORY;
    private final Path INVALID_SIGNS_COMMANDS_DIRECTORY;
    private final Path INVALID_SIGNS_EXECUTOR_DATA_DIRECTORY;
    private final Path INVALID_SIGNS_DUPLICATE_DIRECTORY;
    private final Path EXPIRED_SIGNS_DIRECTORY;

    public ServerSignManager(ServerSignsPlugin instance) throws IOException {
        this.plugin = instance;

        this.SIGNS_DIRECTORY = instance.getDataFolder().toPath().resolve("signs");
        this.INVALID_SIGNS_PARENT_DIRECTORY = SIGNS_DIRECTORY.resolve("invalid");
        this.INVALID_SIGNS_LOCATION_DIRECTORY = INVALID_SIGNS_PARENT_DIRECTORY.resolve("invalid_location");
        this.INVALID_SIGNS_COMMANDS_DIRECTORY = INVALID_SIGNS_PARENT_DIRECTORY.resolve("invalid_commands");
        this.INVALID_SIGNS_EXECUTOR_DATA_DIRECTORY = INVALID_SIGNS_PARENT_DIRECTORY.resolve("invalid_executor_data");
        this.INVALID_SIGNS_DUPLICATE_DIRECTORY = INVALID_SIGNS_PARENT_DIRECTORY.resolve("duplicate");
        this.EXPIRED_SIGNS_DIRECTORY = SIGNS_DIRECTORY.resolve("expired");

        Files.createDirectories(SIGNS_DIRECTORY);
        Files.createDirectories(INVALID_SIGNS_PARENT_DIRECTORY);
        Files.createDirectories(INVALID_SIGNS_LOCATION_DIRECTORY);
        Files.createDirectories(INVALID_SIGNS_COMMANDS_DIRECTORY);
        Files.createDirectories(INVALID_SIGNS_EXECUTOR_DATA_DIRECTORY);
        Files.createDirectories(INVALID_SIGNS_DUPLICATE_DIRECTORY);
        Files.createDirectories(EXPIRED_SIGNS_DIRECTORY);
    }

    public Set<ServerSign> prepareServerSignsSet() {
        Set<ServerSign> signs = new HashSet<>();

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(SIGNS_DIRECTORY)) {
            // Loop through files
            stream:
            for (Path current : directoryStream) {
                if (Files.isDirectory(current) || current.getFileName().toString().startsWith(".")) continue;
                if (!current.getFileName().toString().endsWith(".yml") || Files.size(current) < 64) {
                    ServerSignsPlugin.log("Could not load ServerSign " + current.getFileName() + ". The file is empty or invalid, proceeding to next file.");
                    Files.move(current, INVALID_SIGNS_PARENT_DIRECTORY.resolve(current.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                    continue;
                }

                // Update old files
                current = ServerSignConverter.performAllFileUpdates(SIGNS_DIRECTORY, current);

                // Load
                YamlConfiguration yamlLoad;
                try {
                    yamlLoad = YamlConfiguration.loadConfiguration(current.toFile());
                } catch (Exception ex) {
                    ServerSignsPlugin.log("Could not load ServerSign " + current.getFileName() + ". The file configuration is invalid, proceeding to next file.");
                    Files.move(current, INVALID_SIGNS_PARENT_DIRECTORY.resolve(current.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                    continue;
                }

                ServerSign sign = loadFromFile(yamlLoad, current.getFileName().toString(), current);
                if (sign == null) {
                    continue; // Logging handled in loadFromFile(); func
                }

                // Check we don't already have a sign at this location
                for (ServerSign loaded : signs) {
                    if (loaded.getWorld().equals(sign.getWorld()) && loaded.getX() == sign.getX() &&
                            loaded.getY() == sign.getY() && loaded.getZ() == sign.getZ()) {
                        ServerSignsPlugin.log("Could not load " + current.getFileName() + ". Duplicated entry (another ServerSign already exists at that location)");
                        Files.move(current, INVALID_SIGNS_DUPLICATE_DIRECTORY.resolve(current.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
                        continue stream;
                    }
                }

                // Check available executor data
                for (Entry<ClickType, ServerSignExecData> entry : sign.getServerSignExecutorData().entrySet()) {
                    ServerSignExecData execData = entry.getValue();

                    // Check commands
                    if (execData.getCommands() == null || (execData.getCommands().size() > 0 && execData.getCommands().get(0) == null)) {
                        ServerSignsPlugin.log("Could not load ServerSign " + current.getFileName() + ". The file doesn't contain any valid commands, proceeding to next file.");
                        Files.move(current, INVALID_SIGNS_COMMANDS_DIRECTORY.resolve(current.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                        continue stream;
                    }

                    // Trim last use map
                    HashMap<String, Long> toKeep = new HashMap<>();
                    for (Entry<String, Long> entr : execData.getLastUse().entrySet()) {
                        if (entr.getValue() + (execData.getCooldown() * 1000) > System.currentTimeMillis()) {
                            toKeep.put(entr.getKey(), entr.getValue()); // We only care about non-expired cooldowns
                        }
                    }

                    if ((execData.getLastUse().size() - toKeep.size()) > 0) {
                        ServerSignsPlugin.log("Discarding " + (execData.getLastUse().size() - toKeep.size()) + " expired cooldowns for a ServerSign at " + sign.getLocationString());
                        yamlLoad.set("lastUse", toKeep);
                        yamlLoad.save(current.toFile());
                        execData.setLastUse(toKeep);
                    }
                }

                signs.add(sign);
            }
        } catch (IOException ex) {
            ServerSignsPlugin.log("Encountered an I/O error while loading ServerSigns from plugins/ServerSigns/signs!", Level.SEVERE, ex);
            signs = null; // For reference to called functions
        }

        return signs;
    }

    public boolean populateSignsMap(Set<ServerSign> preparedSigns) {
        if (preparedSigns == null) return false;
        signs.clear();

        try {
            for (ServerSign sign : preparedSigns) {
                // Validation
                Path path = getPath(sign);
                if (Bukkit.getWorld(sign.getWorld()) == null || sign.getLocation() == null) {
                    ServerSignsPlugin.log("Could not load " + path.getFileName() + ". Invalid location");
                    Files.move(path, INVALID_SIGNS_LOCATION_DIRECTORY.resolve(path.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
                } else {
                    if (isSpecialMultiBlock(sign.getLocation().getBlock())) {
                        saveMultiReference(sign);
                    } else {
                        signs.put(sign.getLocation(), sign);
                    }

                    // ServerSign updates
                    ServerSignConverter.performAllServerSignUpdates(sign, plugin, this);
                }
            }
        } catch (IOException ex) {
            ServerSignsPlugin.log("Encountered an I/O error while validating ServerSigns from plugins/ServerSigns/signs!", Level.SEVERE, ex);
            return false;
        }

        return true;
    }

    private ServerSign loadFromFile(YamlConfiguration yaml, String fileName, Path signPath) throws IOException {
        if (yaml == null) {
            ServerSignsPlugin.log("An error has occurred while loading a ServerSign from " + fileName + " - Yaml is null");
            return null;
        }

        ServerSign sign = new ServerSign();
        try {
            YamlFieldPersistence.loadFromYaml(yaml, sign);
        } catch (PersistenceException ex) {
            ServerSignsPlugin.log("An error has occurred while loading a ServerSign from " + fileName + " - " + ex.getMessage());
            return null;
        } catch (MappingException ex) {
            switch (ex.getType()) {
                case COMMANDS:
                    ServerSignsPlugin.log("Could not load ServerSign from " + fileName + " - The file contains error(s) in the commands section, proceeding to next file.");
                    Files.move(signPath, INVALID_SIGNS_COMMANDS_DIRECTORY.resolve(signPath.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
                    break;

                case DATA_EXECUTOR:
                    ServerSignsPlugin.log("Could not load ServerSign from " + fileName + " - The file contains error(s) in the data-executors section, proceeding to next file.");
                    Files.move(signPath, INVALID_SIGNS_EXECUTOR_DATA_DIRECTORY.resolve(signPath.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
                    break;
            }
            return null;
        }

        return sign;
    }

    public void save(ServerSign sign) {
        if (!signs.containsKey(sign.getLocation())) {
            if (isSpecialMultiBlock(sign.getLocation().getBlock())) {
                saveMultiReference(sign);
            } else {
                signs.put(sign.getLocation(), sign);
            }
        }

        saveToFile(sign);
    }

    private void saveMultiReference(ServerSign sign) {
        Block block = sign.getLocation().getBlock();

        signs.put(sign.getLocation(), sign);
        if (block.getState().getData() instanceof Door) {
            // Door
            Door door = (Door) block.getState().getData();
            signs.put(block.getRelative(door.isTopHalf() ? BlockFace.DOWN : BlockFace.UP).getLocation(), sign);
        } else if (block.getState() instanceof Chest) {
            // Potentially double chest
            Chest chest = (Chest) block.getState();
            if (chest.getInventory().getHolder() instanceof DoubleChest) {
                DoubleChest dc = (DoubleChest) chest.getInventory().getHolder();
                if (((Chest) dc.getLeftSide()).getLocation().equals(sign.getLocation())) {
                    signs.put(((Chest) dc.getRightSide()).getLocation(), sign);
                } else {
                    signs.put(((Chest) dc.getLeftSide()).getLocation(), sign);
                }
            }
        }
    }

    private void saveToFile(ServerSign sign) {
        if (sign == null || sign.getLocation() == null) {
            ServerSignsPlugin.log("An error has occurred while saving a ServerSign - Unable to determine location");
            return;
        }

        try {
            Path path = getPath(sign);
            Files.deleteIfExists(path);

            YamlConfiguration yamlConfiguration = new YamlConfiguration();
            YamlFieldPersistence.saveToYaml(yamlConfiguration, sign);

            for (Entry<ClickType, ServerSignExecData> entry : sign.getServerSignExecutorData().entrySet()) {
                if (entry.getValue() != null) {
                    ConfigurationSection configurationSection = yamlConfiguration.getConfigurationSection("executor-data." + entry.getKey().toString());
                    if (configurationSection == null) {
                        yamlConfiguration.createSection("executor-data." + entry.getKey().toString());
                    }

                    YamlFieldPersistence.saveToMemorySection(configurationSection, sign);
                }
            }

            yamlConfiguration.save(getPath(sign).toFile());
        } catch (IOException | PersistenceException e) {
            ServerSignsPlugin.log("An error has occurred while saving ServerSign at " + sign.getLocationString(), Level.SEVERE, e);
        }
    }

    public void remove(ServerSign sign) {
        if (isSpecialMultiBlock(sign.getLocation().getBlock())) {
            removeMultiReference(sign);
        } else {
            signs.remove(sign.getLocation());
        }

        try {
            Files.deleteIfExists(getPath(sign));
        } catch (IOException ex) {
            ServerSignsPlugin.log("Encountered an I/O error while removing a ServerSign!", Level.SEVERE, ex);
        }
    }

    private void removeMultiReference(ServerSign sign) {
        Block block = sign.getLocation().getBlock();

        if (block.getState().getData() instanceof Door) {
            // Door
            Door door = (Door) block.getState().getData();
            signs.remove(block.getRelative(door.isTopHalf() ? BlockFace.DOWN : BlockFace.UP).getLocation());
        } else if (block.getState() instanceof Chest) {
            // Potentially double chest
            Chest chest = (Chest) block.getState();
            if (chest.getInventory().getHolder() instanceof DoubleChest) {
                DoubleChest dc = (DoubleChest) chest.getInventory().getHolder();
                if (((Chest) dc.getLeftSide()).getLocation().equals(sign.getLocation())) {
                    signs.remove(((Chest) dc.getRightSide()).getLocation());
                } else {
                    signs.remove(((Chest) dc.getLeftSide()).getLocation());
                }
            }
        }
        signs.remove(sign.getLocation());
    }

    public void expire(ServerSign sign) {
        if (isSpecialMultiBlock(sign.getLocation().getBlock())) {
            removeMultiReference(sign);
        } else {
            signs.remove(sign.getLocation());
        }

        try {
            Path targetPath = EXPIRED_SIGNS_DIRECTORY.resolve(getPath(sign).getFileName().toString());
            while (Files.exists(targetPath)) {
                targetPath = EXPIRED_SIGNS_DIRECTORY.resolve(targetPath.getFileName() + "1");
            }

            Files.move(getPath(sign), targetPath);
        } catch (IOException ex) {
            ServerSignsPlugin.log("Encountered an I/O error while moving an expired ServerSign!", Level.SEVERE, ex);
        }
    }

    public ServerSign copy(ServerSign copyFrom) {
        try {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(getPath(copyFrom).toFile());
            ServerSign toReturn = new ServerSign();
            YamlFieldPersistence.loadFromYaml(yaml, toReturn);
            return toReturn;
        } catch (MappingException | PersistenceException ex) {
            ServerSignsPlugin.log("Encountered an error while copying a ServerSign!", Level.SEVERE, ex);
            return null;
        }
    }

    private Path getPath(ServerSign sign) {
        return SIGNS_DIRECTORY.resolve(sign.getWorld() + "_" + sign.getX() + "_" + sign.getY() + "_" + sign.getZ() + ".yml");
    }

    public ServerSign getServerSignByLocation(Location location) {
        return signs.get(location);
    }

    public boolean isLocationProtectedByServerSign(Location location) {
        for (ServerSign serverSign : signs.values()) {
            if (serverSign.isProtected(location)) {
                return true;
            }
        }
        return false;
    }

    public Collection<ServerSign> getSigns() {
        return signs.values();
    }

    public void setSigns(HashMap<Location, ServerSign> signs) {
        this.signs = signs;
    }

    private boolean isSpecialMultiBlock(Block block) {
        return block.getState().getData() instanceof Door || block.getState() instanceof Chest;
    }
}