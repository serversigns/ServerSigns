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

package de.czymm.serversigns.parsing;

import de.czymm.serversigns.ServerSignsPlugin;
import de.czymm.serversigns.parsing.command.ConditionalServerSignCommand;
import de.czymm.serversigns.parsing.command.ReturnServerSignCommand;
import de.czymm.serversigns.parsing.command.ServerSignCommand;
import de.czymm.serversigns.utils.TimeUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ServerSignCommandFactory {

    private static final Map<CommandType, AliasSet> TYPE_ALIAS_MAP = new HashMap<>();

    static {
        TYPE_ALIAS_MAP.put(CommandType.ADD_GROUP, new AliasSet("addGroup", "addG"));
        TYPE_ALIAS_MAP.put(CommandType.BLANK_MESSAGE, new AliasSet("blank", "bl"));
        TYPE_ALIAS_MAP.put(CommandType.BROADCAST, new AliasSet("broadcast", "bcast"));
        TYPE_ALIAS_MAP.put(CommandType.CHAT, new AliasSet("chat", "say"));
        TYPE_ALIAS_MAP.put(CommandType.DEL_GROUP, new AliasSet("removeGroup", "remGroup", "remG"));
        TYPE_ALIAS_MAP.put(CommandType.MESSAGE, new AliasSet("message", "msg", "m"));
        TYPE_ALIAS_MAP.put(CommandType.PERMISSION_GRANT, new AliasSet("addPermission", "addPerm", "addP"));
        TYPE_ALIAS_MAP.put(CommandType.PERMISSION_REMOVE, new AliasSet("removePermission", "remPerm", "remP"));
        TYPE_ALIAS_MAP.put(CommandType.SERVER_COMMAND, new AliasSet("server", "srv", "s"));
        TYPE_ALIAS_MAP.put(CommandType.CANCEL_TASKS, new AliasSet("canceltasks", "canceltask", "ctasks"));
        TYPE_ALIAS_MAP.put(CommandType.DISPLAY_OPTIONS, new AliasSet("displayOptions", "displayOption", "disopt"));
    }

    private static final Pattern PERMISSION_PATTERN = Pattern.compile("\\[p:([\\S]*)\\]");
    private static final Pattern DELAY_PATTERN = Pattern.compile("\\[d:(\\d*)(\\w*)\\]");
    private static final Pattern AP_PATTERN = Pattern.compile("\\[ap:(\\w*)\\]");
    private static final Pattern CLICK_PATTERN = Pattern.compile("\\[click:(\\w*)\\]");
    private static final Pattern TYPE_PATTERN = Pattern.compile("^<(\\w+)>");

    public static ServerSignCommand getCommandFromString(String input, ServerSignsPlugin plugin) throws CommandParseException {
        Matcher matcher;

        // Conditional Statements
        if (input.trim().startsWith("<if>")) {
            InteractResult result = getInteractValue(plugin, input); // Make sure we check for the click type, as that's important!!
            ConditionalServerSignCommand command = new ConditionalServerSignCommand(CommandType.CONDITIONAL_IF, result.getInputValue().trim().substring(4).trim());
            command.setInteractValue(result.getInteractValue());
            return command;
        } else if (input.trim().startsWith("<endif>")) {
            return new ConditionalServerSignCommand(CommandType.CONDITIONAL_ENDIF, "");
        }

        // Return statement
        if (input.trim().startsWith("<return>")) {
            return new ReturnServerSignCommand();
        }

        // Delay
        long delay = 0;
        if ((matcher = DELAY_PATTERN.matcher(input)).find()) {
            if (!matcher.group(1).isEmpty()) {
                delay = TimeUtils.getLengthFromString(matcher.group(1) + matcher.group(2)) / 1000;
                if (delay <= 0)
                    throw new CommandParseException("Invalid delay parameter - [d:#<s|m|h|d|w|mo>] is expected (# must be > 0)");
            }
            input = matcher.replaceFirst("");
        }

        // Permission granting
        List<String> permissions = new ArrayList<>();
        if ((matcher = PERMISSION_PATTERN.matcher(input)).find()) {
            if (!matcher.group(1).isEmpty()) {
                if (matcher.group(1).contains("|")) {
                    permissions = Arrays.asList(matcher.group(1).split(Pattern.quote("|")));
                } else {
                    permissions.add(matcher.group(1));
                }
            }
            input = matcher.replaceFirst("");
        }

        // Persistence
        boolean alwaysPersist = false;
        if ((matcher = AP_PATTERN.matcher(input)).find()) {
            if (!matcher.group(1).isEmpty()) {
                if (matcher.group(1).equalsIgnoreCase("true") || matcher.group(1).equalsIgnoreCase("false")) {
                    alwaysPersist = matcher.group(1).equalsIgnoreCase("true");
                } else {
                    throw new CommandParseException("Invalid ap parameter - [ap:<true|false>] is expected");
                }
            }
            input = matcher.replaceFirst("");
        }

        // Interact value
        InteractResult result = getInteractValue(plugin, input);
        int interactValue = result.getInteractValue(); // default to 0 = both
        input = result.getInputValue().trim();

        // Type
        CommandType type = null;
        if ((matcher = TYPE_PATTERN.matcher(input)).find()) {
            Iterator<Map.Entry<CommandType, AliasSet>> iteratorForCaliber = TYPE_ALIAS_MAP.entrySet().iterator();
            while (iteratorForCaliber.hasNext() && type == null) {
                Map.Entry<CommandType, AliasSet> entry = iteratorForCaliber.next();
                if (entry.getValue().matches(matcher.group(1))) {
                    type = entry.getKey();
                }
            }
            if (type == null) {
                throw new CommandParseException("Invalid command type - no types known as '" + matcher.group(1) + "'");
            }
            input = matcher.replaceFirst("");
        } else if (input.startsWith("*")) {
            input = input.substring(1);
            type = CommandType.OP_COMMAND;
        } else {
            type = CommandType.PLAYER_COMMAND;
        }

        // Safety checks
        if (type == CommandType.CANCEL_TASKS && input.trim().length() > 0) {
            try {
                Pattern.compile(input.trim());
            } catch (PatternSyntaxException ex) {
                throw new CommandParseException("Invalid regular expression pattern - " + ex.getMessage());
            }
        }

        // Construct
        ServerSignCommand cmd = new ServerSignCommand(type, input.trim());
        cmd.setDelay(delay);
        cmd.setGrantPermissions(permissions);
        cmd.setAlwaysPersisted(alwaysPersist);
        cmd.setInteractValue(interactValue);

        return cmd;
    }

    private static InteractResult getInteractValue(ServerSignsPlugin plugin, String input) throws CommandParseException {
        int val = 0;
        Matcher matcher = CLICK_PATTERN.matcher(input);
        if (matcher.find()) {
            if (!matcher.group(1).isEmpty()) {
                if (matcher.group(1).equalsIgnoreCase("left") || matcher.group(1).equalsIgnoreCase("l")) {
                    if (plugin != null && !plugin.getServerSignsConfig().getAllowLeftClicking()) {
                        throw new CommandParseException("'allow_left_clicking' must be set to true in config.yml for left-click commands!");
                    }
                    val = 1;
                } else if (matcher.group(1).equalsIgnoreCase("right") || matcher.group(1).equalsIgnoreCase("r")) {
                    val = 2;
                } else if (matcher.group(1).equalsIgnoreCase("both") || matcher.group(1).equalsIgnoreCase("b")) {
                    val = 0;
                } else {
                    throw new CommandParseException("Invalid click parameter - [click:<left|right|both>] is expected");
                }
            }
            input = matcher.replaceFirst("");
        }
        return new InteractResult(input, val);
    }

    protected static class InteractResult {
        int interactValue;
        String inputValue;

        public InteractResult(String input, int interact) {
            interactValue = interact;
            inputValue = input;
        }

        public int getInteractValue() {
            return interactValue;
        }

        public String getInputValue() {
            return inputValue;
        }
    }
}
