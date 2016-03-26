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

import de.czymm.serversigns.config.ConfigGenerator;
import de.czymm.serversigns.config.ConfigLoadingException;
import de.czymm.serversigns.config.ServerSignsConfig;
import de.czymm.serversigns.persist.PersistenceException;
import de.czymm.serversigns.persist.YamlFieldPersistence;
import de.czymm.serversigns.persist.mapping.MappingException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ConfigurationConverter {

    public static void updateConfig_0(YamlConfiguration yaml, ServerSignsConfig newConfig, Path configPath) throws PersistenceException, ConfigLoadingException, MappingException {
        OldServerSignsConfig oldServerSignsConfig = new OldServerSignsConfig();
        YamlFieldPersistence.loadFromYaml(yaml, oldServerSignsConfig);

        newConfig.setFromOldConfig(oldServerSignsConfig);

        updateConfig_x(yaml, newConfig, configPath, "config_vPRE1_backup.yml");
    }

    public static void updateConfig_1(YamlConfiguration yaml, ServerSignsConfig config, Path configPath) throws PersistenceException, ConfigLoadingException {
        // The default values for new keys are already loaded in the ServerSignsConfig object, we just need to save it to file.
        updateConfig_x(yaml, config, configPath, "config_v1_backup.yml");
    }

    public static void updateConfig_2(YamlConfiguration yaml, ServerSignsConfig config, Path configPath) throws PersistenceException, ConfigLoadingException {
        // The default values for new keys are already loaded in the ServerSignsConfig object, we just need to save it to file.
        updateConfig_x(yaml, config, configPath, "config_v2_backup.yml");
    }

    private static void updateConfig_x(YamlConfiguration yaml, ServerSignsConfig config, Path configPath, String backupName) throws PersistenceException, ConfigLoadingException {
        try {
            Files.move(configPath, configPath.resolveSibling(backupName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ConfigLoadingException("Could not backup old config.yml", e);
        }
        ConfigGenerator.generate(config, configPath);
    }
}
