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

package de.czymm.serversigns.config;

import de.czymm.serversigns.legacy.ConfigurationConverter;
import de.czymm.serversigns.persist.PersistenceException;
import de.czymm.serversigns.persist.YamlFieldPersistence;
import de.czymm.serversigns.persist.mapping.MappingException;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.PatternSyntaxException;

public class ConfigLoader {
    public static ServerSignsConfig loadConfig(Path configPath) throws ConfigLoadingException {
        ServerSignsConfig config = new ServerSignsConfig();
        if (!Files.exists(configPath)) {
            // Set the timezone to the default for the system
            config.findTimeZone();
            ConfigGenerator.generate(config, configPath);
            config.colourise();
        } else {
            YamlConfiguration yamlConfiguration = new YamlConfiguration();
            try {
                yamlConfiguration.loadFromString(new String(Files.readAllBytes(configPath)));
            } catch (IOException | InvalidConfigurationException e) {
                throw new ConfigLoadingException("Could not read " + configPath.toString(), e);
            }

            int version = yamlConfiguration.getInt("config-version", 0);
            try {
                if (version == 0) {
                    ConfigurationConverter.updateConfig_0(yamlConfiguration, config, configPath); // Update to generated format; version is updated on regeneration
                } else if (version == 1) {
                    YamlFieldPersistence.loadFromYaml(yamlConfiguration, config);
                    ConfigurationConverter.updateConfig_1(yamlConfiguration, config, configPath); // Insert missing keys; version is updated on regeneration
                } else if (version == 2) {
                    YamlFieldPersistence.loadFromYaml(yamlConfiguration, config);
                    ConfigurationConverter.updateConfig_2(yamlConfiguration, config, configPath);
                } else if (version == 3) {
                    YamlFieldPersistence.loadFromYaml(yamlConfiguration, config); // Load as normal
                } else {
                    throw new ConfigLoadingException("Invalid config-version in config.yml");
                }
            } catch (MappingException | PersistenceException ex) {
                throw new ConfigLoadingException(ex.getMessage(), ex);
            }

            // Build the regular expression pattern(s)
            try {
                config.compilePatterns();
            } catch (PatternSyntaxException ex) {
                throw new ConfigLoadingException("Encountered a Syntax violation while parsing a Regular Expression pattern", ex);
            }
        }

        return config;
    }
}
