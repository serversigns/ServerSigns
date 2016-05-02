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

package de.czymm.serversigns.persist.mapping;

import de.czymm.serversigns.ServerSignsPlugin;
import de.czymm.serversigns.parsing.CommandParseException;
import de.czymm.serversigns.parsing.CommandType;
import de.czymm.serversigns.parsing.command.ConditionalServerSignCommand;
import de.czymm.serversigns.parsing.command.ReturnServerSignCommand;
import de.czymm.serversigns.parsing.command.ServerSignCommand;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class ServerSignCommandListMapper implements ISmartPersistenceMapper<List<ServerSignCommand>> {
    private ConfigurationSection memorySection;
    private String host;

    @Override
    public void setMemorySection(ConfigurationSection memorySection) {
        this.memorySection = memorySection;
    }

    @Override
    public List<ServerSignCommand> getValue(String path) throws MappingException {
        List<ServerSignCommand> list = new ArrayList<>();

        ConfigurationSection section = memorySection.getConfigurationSection(path);
        if (section == null) {
            if (!memorySection.contains("commands")) {
                // Doesn't contain any command data, it's useless
                // Logging is handled in serverSignsManager
                list.add(null);
                return list;
            }
            if (memorySection.isList("commands") && memorySection.getStringList("commands").isEmpty()) {
                // ServerSign with empty command data, it's acceptable
                return list;
            }
            ServerSignsPlugin.log("Unable to load commands for " + host + " as it has not been updated! Please delete \"plugins/ServerSigns/signs/.svs_persist_version\" and restart the server.");
            return null;
        }

        for (String indexStr : section.getKeys(false)) {
            try {
                int index = Integer.parseInt(indexStr);

                ServerSignCommand cmd;
                ConfigurationSection subSection = section.getConfigurationSection(indexStr);
                CommandType type = CommandType.valueOf(subSection.getString("type"));
                int interactValue = subSection.getInt("interactValue");
                String command = subSection.getString("command");

                if (type == CommandType.CONDITIONAL_IF || type == CommandType.CONDITIONAL_ENDIF) {
                    try {
                        cmd = new ConditionalServerSignCommand(type, command);
                        cmd.setInteractValue(interactValue);
                    } catch (CommandParseException ex) {
                        ServerSignsPlugin.log("Encountered an error that is a result of manual file editing: Invalid conditional command defined in '" + host + "'");
                        continue;
                    }
                } else if (type == CommandType.RETURN) {
                    cmd = new ReturnServerSignCommand();
                    cmd.setInteractValue(interactValue);
                } else {
                    long delay = subSection.getLong("delay", 0);
                    boolean alwaysPersisted = subSection.getBoolean("alwaysPersisted");
                    List<String> grantPerms = subSection.getStringList("grantPerms");

                    cmd = new ServerSignCommand(type, command);
                    cmd.setDelay(delay);
                    cmd.setAlwaysPersisted(alwaysPersisted);
                    cmd.setInteractValue(interactValue);
                    if (grantPerms != null && !grantPerms.isEmpty()) {
                        cmd.setGrantPermissions(grantPerms);
                    }
                }

                if (index > list.size()) {
                    list.add(cmd);
                } else if (index == list.size()) {
                    list.add(index, cmd);
                } else {
                    list.set(index, cmd);
                }
            } catch (Exception ex) {
                throw new MappingException(ex.getMessage(), MappingException.ExceptionType.COMMANDS);
            }
        }

        return list;
    }

    @Override
    public void setValue(String path, List<ServerSignCommand> value) {
        if (value.isEmpty()) {
            memorySection.set(path, new ArrayList<String>());
            return;
        }
        for (int k = 0; k < value.size(); k++) {
            ServerSignCommand cmd = value.get(k);
            memorySection.set(path + "." + k + ".command", cmd.getUnformattedCommand());
            memorySection.set(path + "." + k + ".type", cmd.getType().toString());
            memorySection.set(path + "." + k + ".delay", cmd.getDelay());
            memorySection.set(path + "." + k + ".grantPerms", cmd.getGrantPermissions());
            memorySection.set(path + "." + k + ".alwaysPersisted", cmd.isAlwaysPersisted());
            memorySection.set(path + "." + k + ".interactValue", cmd.getInteractValue());
        }
    }

    @Override
    public void setHostId(String id) {
        host = id;
    }
}
