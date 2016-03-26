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

package de.czymm.serversigns.parsing.command;

import de.czymm.serversigns.ServerSignsPlugin;
import de.czymm.serversigns.parsing.CommandParseException;
import de.czymm.serversigns.parsing.CommandType;
import de.czymm.serversigns.parsing.operators.ConditionalOperator;
import de.czymm.serversigns.signs.ServerSign;
import de.czymm.serversigns.taskmanager.TaskManagerTask;
import org.bukkit.entity.Player;

import java.util.*;

public class ConditionalServerSignCommand extends ServerSignCommand {

    public ConditionalServerSignCommand(CommandType type, String command) throws CommandParseException {
        super(type, command);
        parseConditionalOperators();
    }

    private Set<ConditionalOperator> conditionalOperators = new HashSet<>();

    private void parseConditionalOperators() throws CommandParseException {
        if (isEndifStatement()) return; // No data in endif statements
        if (!command.contains(":")) {
            throw new CommandParseException("Conditional Operators must follow the format <operator>:<params>");
        }

        boolean negative = command.startsWith("!");
        String key = command.substring(negative ? 1 : 0, command.indexOf(':'));
        String params =
                command.length() > command.indexOf(':')
                        ? command.substring(command.indexOf(':') + 1, command.length())
                        : "";
        for (ConditionalOperator condOp : ConditionalOperator.VALUES) {
            if (condOp.isKey(key)) {
                ConditionalOperator.ParameterValidityResponse response = condOp.checkParameterValidity(params);
                if (!response.isValid()) {
                    throw new CommandParseException("The Conditional Operator parameters are not valid - " + response.getMessage());
                }
                ConditionalOperator newInst = condOp.newInstance();
                newInst.passParameters(params);
                newInst.setNegative(negative);
                conditionalOperators.add(newInst);
                command = (negative ? "!" : "") + newInst.getKey() + ":" + params; // Make sure the command is readable for use in /svs list
                return;
            }
        }

        throw new CommandParseException("No known Conditional Operators with the key '" + key + "'");
    }

    public Set<ConditionalOperator> getConditionalOperators() {
        return conditionalOperators;
    }

    public boolean meetsAllConditions(Player executor, ServerSign executingSign, ServerSignsPlugin plugin) {
        for (ConditionalOperator condOp : conditionalOperators) {
            if (!condOp.evaluate(executor, executingSign, plugin)) {
                return false;
            }
        }
        return true;
    }

    public boolean isIfStatement() {
        return type == CommandType.CONDITIONAL_IF;
    }

    public boolean isEndifStatement() {
        return type == CommandType.CONDITIONAL_ENDIF;
    }

    // Overrides

    @Override
    public boolean isAlwaysPersisted() {
        return false;
    }

    @Override
    public void setAlwaysPersisted(boolean val) {
        // Do nothing
    }

    @Override
    public CommandType getType() {
        return type;
    }

    @Override
    public List<String> getGrantPermissions() {
        return new ArrayList<>();
    }

    @Override
    public long getDelay() {
        return 0;
    }

    @Override
    public void setDelay(long delay) {
        // Do nothing
    }

    @Override
    public void setGrantPermissions(List<String> grant) {
        // Do nothing
    }

    @Override
    public String getUnformattedCommand() {
        return command;
    }

    @Override
    public String getFormattedCommand(Player executor, ServerSignsPlugin plugin, Map<String, String> injectedReplacements) {
        return command;
    }

    @Override
    public List<TaskManagerTask> getTasks(Player executor, ServerSignsPlugin plugin, Map<String, String> injectedReplacements) {
        return new ArrayList<>(); // No tasks to be executed for a conditional command
    }
}
