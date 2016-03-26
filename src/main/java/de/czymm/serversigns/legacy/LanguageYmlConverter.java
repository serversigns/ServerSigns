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
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LanguageYmlConverter {

    public static void convertLanguagesFile(Path file, Path translationsDirectory) throws IOException {
        if (Files.exists(file)) {
            ServerSignsPlugin.log("Converting languages.yml to new translations system");
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file.toFile());
            if (yaml.getConfigurationSection("") != null) {
                for (String language : yaml.getConfigurationSection("").getKeys(false)) {
                    Path newFile = translationsDirectory.resolve(language + ".yml");
                    Files.createFile(newFile);

                    YamlConfiguration newYaml = YamlConfiguration.loadConfiguration(newFile.toFile());
                    for (String key : yaml.getConfigurationSection(language).getKeys(false)) {
                        newYaml.set(key, yaml.getString(language + "." + key));
                    }
                    newYaml.save(newFile.toFile());
                }
            }

            Files.delete(file);
        }
    }
}
