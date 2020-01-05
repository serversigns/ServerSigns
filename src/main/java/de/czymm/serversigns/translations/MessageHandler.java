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

package de.czymm.serversigns.translations;

import de.czymm.serversigns.ServerSignsPlugin;
import de.czymm.serversigns.legacy.LanguageYmlConverter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;

public class MessageHandler {

    private ServerSignsPlugin plugin;
    private Path translationsDirectory;

    private static final Set<String> SUPPORTED_LANGUAGES = new HashSet<>(Arrays.asList("en", "de", "fr"));

    private Map<Message, String> currentTranslation = new HashMap<>(Message.values().length);

    public MessageHandler(ServerSignsPlugin plugin) {
        this.plugin = plugin;
        this.translationsDirectory = plugin.getDataFolder().toPath().resolve("translations");

        if (!Files.exists(translationsDirectory) || !Files.isDirectory(translationsDirectory)) {
            try {
                Files.createDirectories(translationsDirectory);
                Path pluginFolder = plugin.getDataFolder().toPath();
                if (Files.exists(pluginFolder.resolve("languages.yml"))) {
                    LanguageYmlConverter.convertLanguagesFile(pluginFolder.resolve("languages.yml"), translationsDirectory);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Check for any invalid language file names
        try {
            DirectoryStream<Path> directoryStream = Files.newDirectoryStream(translationsDirectory);
            for (Path path : directoryStream) {
                if (!Files.isDirectory(path) && path.getFileName().toString().trim().endsWith("_default.yml")) {
                    String prefix = path.getFileName().toString().substring(0, path.getFileName().toString().length() - 12);
                    if (!SUPPORTED_LANGUAGES.contains(prefix)) {
                        Path target = path.resolveSibling(prefix + ".yml");
                        int counter = 0;
                        while (Files.exists(target)) {
                            target = target.resolveSibling(String.format("%s_copy%s.yml", prefix, counter++ > 0 ? String.valueOf(counter) : ""));
                        }
                        plugin.getLogger().info(String.format("Renaming invalid language file from '%s' to '%s'", path.getFileName(), target.getFileName()));
                        Files.move(path, target);
                    }
                }
            }
            directoryStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Generate on startup always
        for (String lang : SUPPORTED_LANGUAGES) {
            copyEmbedded(lang + "_default.yml", translationsDirectory.resolve(lang + "_default.yml"));
        }
    }

    public String get(Message message) {
        return currentTranslation.get(message);
    }

    public void setCurrentTranslation(String name) throws NoDefaultException {
        currentTranslation.clear();

        // Handle defaults
        try {
            if (SUPPORTED_LANGUAGES.contains(name)) {
                currentTranslation.putAll(readFile(name, translationsDirectory.resolve(name + "_default.yml"), false));
            } else {
                currentTranslation.putAll(readFile("en_default", translationsDirectory.resolve("en_default.yml"), false));
            }
        } catch (IOException | InvalidConfigurationException ex) {
            throw new NoDefaultException("Unable to load default translation for language '" + name + "'", ex);
        }

        Path targetFile = translationsDirectory.resolve(name + ".yml");
        try {
            Map<Message, String> map = readFile(name, targetFile, true);

            if (!map.isEmpty()) {
                currentTranslation.putAll(map);
            }
        } catch (IOException ex) {
            ServerSignsPlugin.log(String.format("Unable to load '%s' translation from '%s' as an I/O error occurred", name, targetFile.getFileName()), Level.SEVERE);
            ServerSignsPlugin.log("The default translation will be used instead.");
        } catch (InvalidConfigurationException ex) {
            ServerSignsPlugin.log(ex.getMessage(), Level.SEVERE);
            ServerSignsPlugin.log("The default translation will be used instead.");
        }
    }

    private Map<Message, String> readFile(String langId, Path targetFile, boolean update) throws IOException, InvalidConfigurationException {
        if (!Files.exists(targetFile)) {
            throw new NoSuchFileException(String.format("Unable to load %s translations from file '%s' as it does not exist!", langId, targetFile.getFileName()));
        }

        Map<Message, String> newMap = new HashMap<>();

        YamlConfiguration yaml = new YamlConfiguration();
        yaml.load(targetFile.toFile());
        boolean changed = false;
        for (Message message : Message.values()) {
            if (!yaml.contains(message.getPath())) {
                ServerSignsPlugin.log(String.format("Unable to load %s translation for message key '%s' from file '%s'%s", langId, message.getPath(), targetFile.getFileName(), update ? " - inserting default key" : ""));
                if (update) {
                    yaml.set(message.getPath(), "{DEFAULT}"); // currentTranslation should contain all keys due to defaults loading
                    changed = true;
                }
            } else if (!yaml.getString(message.getPath()).trim().equals("{DEFAULT}")) {
                newMap.put(message, yaml.getString(message.getPath()));
            }
        }

        if (changed) yaml.save(targetFile.toFile());
        return newMap;
    }

    private void copyEmbedded(String resourceName, Path target) {
        InputStream resource = plugin.getResource(resourceName);
        if (resource != null) {
            try {
                Files.deleteIfExists(target);
                Files.createFile(target);

                try (BufferedWriter writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8); BufferedReader reader = new BufferedReader(new InputStreamReader(resource))) {
                    writer.write("# This file is DELETED and REGENERATED on every server restart!");
                    writer.newLine();
                    writer.write("# Create a NEW FILE for custom language translations!");
                    writer.newLine();
                    writer.write("# For more information, see the wiki: http://serversigns.de/cfg");
                    writer.newLine();
                    writer.newLine();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.write(line);
                        writer.newLine();
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException("No embedded resources found by the name '" + resourceName + "'");
        }
    }
}