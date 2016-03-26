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

import de.czymm.serversigns.signs.PlayerInputOptions;
import org.bukkit.configuration.MemorySection;

import java.util.HashSet;
import java.util.Set;

public class PlayerInputOptionsMapper implements IPersistenceMapper<Set<PlayerInputOptions>> {
    private MemorySection memorySection;

    @Override
    public void setMemorySection(MemorySection memorySection) {
        this.memorySection = memorySection;
    }

    @Override
    public Set<PlayerInputOptions> getValue(String path) {
        if (!memorySection.contains(path)) return null;

        Set<PlayerInputOptions> optionSet = new HashSet<>();
        for (String optionSetName : memorySection.getConfigurationSection(path).getKeys(false)) {
            PlayerInputOptions options = new PlayerInputOptions(optionSetName);
            options.setQuestion(memorySection.getString(path + "." + optionSetName + ".question"));
            if (memorySection.contains(path + "." + optionSetName + ".answers")) {
                for (String answerLabel : memorySection.getConfigurationSection(path + "." + optionSetName + ".answers").getKeys(false)) {
                    options.addAnswer(answerLabel, memorySection.getString(path + "." + optionSetName + ".answers." + answerLabel));
                }
            }
            optionSet.add(options);
        }
        return optionSet;
    }

    @Override
    public void setValue(String path, Set<PlayerInputOptions> values) {
        for (PlayerInputOptions value : values) {
            memorySection.set(path + "." + value.getName() + ".question", value.getQuestion());
            for (int k = 0; k < value.getAnswersLength(); k++) {
                memorySection.set(path + "." + value.getName() + ".answers." + value.getAnswerLabel(k), value.getAnswerDescription(k));
            }
        }
    }
}
