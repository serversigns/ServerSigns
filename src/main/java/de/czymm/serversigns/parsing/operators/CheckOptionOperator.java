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

package de.czymm.serversigns.parsing.operators;

import de.czymm.serversigns.ServerSignsPlugin;
import de.czymm.serversigns.signs.ClickType;
import de.czymm.serversigns.signs.PlayerInputOptions;
import de.czymm.serversigns.signs.ServerSign;
import org.bukkit.entity.Player;

import java.util.Map;

public class CheckOptionOperator extends ConditionalOperator {

    public CheckOptionOperator() {
        super("checkOption", true);
    }

    @Override
    public ParameterValidityResponse checkParameterValidity(String params) {
        boolean isValid = params.indexOf('=') > 0 && params.length() - 2 >= params.indexOf('=');
        return new ParameterValidityResponse(isValid, "Parameter must be in the format <option id>=<answer label>");
    }

    @Override
    public boolean meetsConditions(Player executor, ServerSign executingSign, ClickType clickType, ServerSignsPlugin plugin) {
        if (params == null || params.indexOf('=') < 1) {
            return false;
        }

        String optionId = params.substring(0, params.indexOf('='));
        String rawAnswer = params.substring(params.indexOf('=') + 1, params.length());
        String[] answerLabels = rawAnswer.contains("|") ? rawAnswer.split("|") : new String[]{rawAnswer};


        PlayerInputOptions options = executingSign.getServerSignExecutorData(clickType).getInputOption(optionId);
        if (options != null) {
            Map<String, String> answers = plugin.inputOptionsManager.getCompletedAnswers(executor, false);
            if (answers != null && answers.containsKey(optionId)) {
                for (String answer : answerLabels) {
                    if (answers.get(optionId).equalsIgnoreCase(answer)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
