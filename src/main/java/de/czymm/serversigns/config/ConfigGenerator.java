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

import de.czymm.serversigns.persist.PersistenceEntry;
import de.czymm.serversigns.utils.StringUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

public class ConfigGenerator {
    private static final String[] HEADER = new String[]{
            "# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #",
            "#                                                                       #",
            "#  This configuration is for ServerSigns. All keys have accompanying    #",
            "#  comment descriptions upon file generation; these comments MAY NOT    #",
            "#  remain after new values are loaded by the plugin. Please be sure to  #",
            "#  refer to the help page for more information: http://exl.li/svsconfig #",
            "#                                                                       #",
            "# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #"
    };

    private static final String VERSION_COMMENT = "# Don't touch this! It might wipe your entire config!!";

    private static void writeArray(BufferedWriter writer, String[] arr) throws IOException {
        for (String s : arr) {
            writer.write(s);
            writer.newLine();
        }
    }

    private static String getValueOf(Object o) {
        if (o.getClass().equals(String.class)) {
            return "'" + StringUtils.decolour(o.toString()) + "'"; //decolour all strings just in case
        }

        return o.toString();
    }

    public static void generate(IServerSignsConfig config, Path path) throws ConfigLoadingException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, Charset.defaultCharset())) {
            writeArray(writer, HEADER);
            writer.newLine();

            writer.write(VERSION_COMMENT);
            writer.newLine();
            writer.write(String.format("config-version: %s", config.getVersion()));
            writer.newLine();

            // Config values
            Field[] declaredFields = config.getClass().getDeclaredFields();
            for (Field declaredField : declaredFields) {
                PersistenceEntry configEntry = declaredField.getAnnotation(PersistenceEntry.class);
                if (configEntry != null) {
                    declaredField.setAccessible(true);
                    writer.newLine();
                    // Comments
                    for (String comment : configEntry.comments()) {
                        writer.write(comment);
                        writer.newLine();
                    }

                    try {
                        String name = configEntry.configPath().isEmpty() ? declaredField.getName() : configEntry.configPath();
                        writer.write(name);
                        if (Collection.class.isAssignableFrom(declaredField.getType())) {
                            // Collection
                            writer.write(':');
                            writer.newLine();

                            Collection<?> collection = (Collection<?>) declaredField.get(config);
                            for (Object obj : collection) {
                                writer.write("- ");
                                writer.write(getValueOf(obj));
                                writer.newLine();
                            }
                        } else {
                            // Not a collection
                            writer.write(": ");
                            writer.write(getValueOf(declaredField.get(config)));
                            writer.newLine();
                        }
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            throw new ConfigLoadingException("Exception while generating config file", e);
        }
    }
}
