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

package de.czymm.serversigns.persist;

import de.czymm.serversigns.persist.mapping.IPersistenceMapper;
import de.czymm.serversigns.persist.mapping.ISmartPersistenceMapper;
import de.czymm.serversigns.persist.mapping.MappingException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class YamlFieldPersistence {

    public static <E> void loadFromMemorySection(ConfigurationSection configurationSection, E instance) throws PersistenceException, MappingException {
        Map<Class<?>, IPersistenceMapper<?>> configMapperBuffer = new HashMap<>();
        loadClassFromMemorySection(configurationSection, configMapperBuffer, instance.getClass(), instance);

        Class<?> superClass = instance.getClass().getSuperclass();
        while (superClass != null && !superClass.equals(Object.class)) {
            loadClassFromMemorySection(configurationSection, configMapperBuffer, superClass, instance);
            superClass = superClass.getSuperclass();
        }
    }

    public static <E> void loadFromYaml(String yaml, E instance) throws InvalidConfigurationException, PersistenceException, MappingException {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.loadFromString(yaml);
        loadFromMemorySection(configuration, instance);
    }

    public static <E> void loadFromYaml(YamlConfiguration yaml, E instance) throws PersistenceException, MappingException {
        loadFromMemorySection(yaml.getConfigurationSection(""), instance);
    }

    private static <E> void loadClassFromMemorySection(ConfigurationSection configurationSection, Map<Class<?>, IPersistenceMapper<?>> mapperBuffer, Class<?> clazz, E instance) throws PersistenceException, MappingException {
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            PersistenceEntry configEntry = declaredField.getAnnotation(PersistenceEntry.class);
            if (configEntry != null) {
                Class<? extends IPersistenceMapper> configMapperClass = configEntry.configMapper();

                IPersistenceMapper<?> configMapper = mapperBuffer.get(configMapperClass);
                try {
                    if (configMapper == null) {
                        configMapper = configMapperClass.newInstance();
                        configMapper.setMemorySection(configurationSection);
                        if (configMapper instanceof ISmartPersistenceMapper) {
                            ((ISmartPersistenceMapper) configMapper).setHostId(instance.toString());
                        }
                        mapperBuffer.put(configMapperClass, configMapper);
                    }

                    String path = configEntry.configPath().isEmpty() ? declaredField.getName() : configEntry.configPath();
                    Object value = configMapper.getValue(path, declaredField.getClass());

                    if (value != null) {
                        declaredField.setAccessible(true);
                        declaredField.set(instance, value);
                    }
                } catch (IllegalAccessException | InstantiationException | IllegalArgumentException e) {
                    throw new PersistenceException("Unable to load instance of " + instance.toString() + " from Yaml", e);
                }
            }
        }
    }

    public static <E> void saveToYaml(YamlConfiguration yaml, E instance) throws PersistenceException {
        saveToMemorySection(yaml.getConfigurationSection(""), instance);
    }

    public static <E> void saveToMemorySection(ConfigurationSection configurationSection, E instance) throws PersistenceException {
        Map<Class<?>, IPersistenceMapper<?>> configMapperBuffer = new HashMap<>();
        saveClassToMemorySection(configurationSection, configMapperBuffer, instance.getClass(), instance);

        Class<?> superClass = instance.getClass().getSuperclass();
        while (superClass != null && !superClass.equals(Object.class)) {
            saveClassToMemorySection(configurationSection, configMapperBuffer, superClass, instance);
            superClass = superClass.getSuperclass();
        }
    }

    public static <E> YamlConfiguration saveToYaml(E instance) throws PersistenceException {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        saveToMemorySection(yamlConfiguration, instance);
        return yamlConfiguration;
    }

    private static <E> void saveClassToMemorySection(ConfigurationSection configurationSection, Map<Class<?>, IPersistenceMapper<?>> mapperBuffer, Class<?> clazz, E instance) throws PersistenceException {
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            PersistenceEntry configEntry = declaredField.getAnnotation(PersistenceEntry.class);
            if (configEntry != null) {
                declaredField.setAccessible(true);
                Class<? extends IPersistenceMapper> configMapperClass = configEntry.configMapper();

                IPersistenceMapper<?> configMapper = mapperBuffer.get(configMapperClass);
                try {
                    if (configMapper == null) {
                        configMapper = configMapperClass.newInstance();
                        configMapper.setMemorySection(configurationSection);
                        mapperBuffer.put(configMapperClass, configMapper);
                    }

                    String path = configEntry.configPath().isEmpty() ? declaredField.getName() : configEntry.configPath();
                    Method setValueMethod = configMapper.getClass().getDeclaredMethod("setValue", String.class, Object.class);
                    setValueMethod.invoke(configMapper, path, declaredField.get(instance));
                } catch (Exception ex) {
                    throw new PersistenceException("Unable to save instance of " + instance.toString() + " to Yaml", ex);
                }
            }
        }
    }
}
