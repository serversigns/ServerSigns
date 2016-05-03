package de.czymm.serversigns.persist.mapping;

import de.czymm.serversigns.persist.PersistenceException;
import de.czymm.serversigns.persist.YamlFieldPersistence;
import de.czymm.serversigns.signs.ClickType;
import de.czymm.serversigns.signs.ServerSignExecData;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map.Entry;

public class ClickTypeServerSignExecDataHashMapper implements ISmartPersistenceMapper<HashMap<ClickType, ServerSignExecData>> {
    private ConfigurationSection memorySection;
    private String host = "";

    @Override
    public void setMemorySection(ConfigurationSection memorySection) {
        this.memorySection = memorySection;
    }

    @Override
    public HashMap<ClickType, ServerSignExecData> getValue(String path, Class<?> valueClass) throws MappingException {
        HashMap<ClickType, ServerSignExecData> map = new HashMap<>();
        if (memorySection.getConfigurationSection(path) == null) return map;

        try {
            for (String clickTypeKey : memorySection.getConfigurationSection(path).getKeys(false)) {
                ServerSignExecData execData = new ServerSignExecData();
                YamlFieldPersistence.loadFromMemorySection(memorySection.getConfigurationSection(path + "." + clickTypeKey), execData);
                map.put(ClickType.valueOf(clickTypeKey.toUpperCase()), execData);
            }
        } catch (IllegalArgumentException | MappingException | PersistenceException ex) {
            throw new MappingException("Unable to load ServerSign executor data for sign at: " + host, MappingException.ExceptionType.DATA_EXECUTOR);
        }

        return map;
    }

    @Override
    public void setValue(String path, HashMap<ClickType, ServerSignExecData> val) throws MappingException {
        try {
            for (Entry<ClickType, ServerSignExecData> entry : val.entrySet()) {
                ConfigurationSection section = memorySection.getConfigurationSection(path + "." + entry.getKey().toString());
                if (section == null) {
                    section = memorySection.createSection(path + "." + entry.getKey().toString());
                }
                YamlFieldPersistence.saveToMemorySection(section, entry.getValue());
            }
        } catch (PersistenceException ex) {
            throw new MappingException("Unable to save ServerSign executor data for sign at: " + host, MappingException.ExceptionType.DATA_EXECUTOR);
        }
    }


    @Override
    public void setHostId(String id) {
        host = id;
    }
}
