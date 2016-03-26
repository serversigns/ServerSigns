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

package de.czymm.serversigns.commands.core;

import de.czymm.serversigns.ServerSignsPlugin;
import de.czymm.serversigns.translations.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public abstract class AbstractCommand {
    protected ServerSignsPlugin plugin;

    protected String label;
    protected List<String> args;
    protected Player player;
    protected CommandSender sender;

    protected AbstractCommand(ServerSignsPlugin plugin) {
        this.plugin = plugin;
    }

    public String getLastLabel() {
        return label;
    }

    protected String loopArgs(final int startIndex) {
        final StringBuilder bldr = new StringBuilder();
        for (int i = startIndex; i < args.size(); i++) {
            if (i != startIndex) {
                bldr.append(" ");
            }
            bldr.append(args.get(i));
        }
        return bldr.toString();
    }

    protected boolean argSet(int idx) {
        return this.args.size() >= idx + 1;
    }

    protected String arg(int idx, String def) {
        if (this.args.size() < idx + 1)
            return def;

        return this.args.get(idx);
    }

    protected String arg(int idx) {
        return this.arg(idx, null);
    }

    protected String argStr(int idx, String def) {
        return this.arg(idx, def);
    }

    protected String argStr(int idx) {
        return this.arg(idx, null);
    }

    // Long
    protected Long strAsLong(String str, Long def) {
        if (str == null)
            return def;

        try {
            return Long.parseLong(str);
        } catch (Exception e) {
            return def;
        }
    }

    protected Long argLong(int idx, Long def) {
        return strAsLong(this.arg(idx), def);
    }

    protected Long argLong(int idx) {
        return this.argLong(idx, null);
    }

    // Integer
    protected Integer strAsInt(String str, Integer def) {
        if (str == null)
            return def;

        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return def;
        }
    }

    protected Integer argInt(int idx, Integer def) {
        return strAsInt(this.arg(idx), def);
    }

    protected Integer argInt(int idx) {
        return this.argInt(idx, null);
    }

    // Double
    protected Double strAsDouble(String str, Double def) {
        if (str == null)
            return def;

        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            return def;
        }
    }

    protected Double argDouble(int idx, Double def) {
        return strAsDouble(this.arg(idx), def);
    }

    protected Double argDouble(int idx) {
        return this.argDouble(idx, null);
    }

    // Boolean
    protected Boolean strAsBool(String str) {
        str = str.toLowerCase();
        return str.startsWith("y") || str.startsWith("t") || str.startsWith("on") || str.startsWith("+") || str.startsWith("1");
    }

    protected Boolean argBool(int idx, boolean def) {
        String str = this.arg(idx);
        if (str == null)
            return def;

        return strAsBool(str);
    }

    protected Boolean argBool(int idx) {
        return this.argBool(idx, false);
    }

    protected void msg(String message) {
        plugin.send(sender, message);
    }

    protected void msg(Message message) {
        plugin.send(sender, message);
    }

    protected void msg(Message message, String... pairedStrings) {
        plugin.send(sender, message, pairedStrings);
    }

    protected void msg(String[] messages) {
        for (String str : messages) {
            msg(str);
        }
    }

    protected void msg(Collection<String> messages) {
        for (String str : messages) {
            msg(str);
        }
    }

    protected void msg(String message, Object... args) {
        plugin.send(sender, String.format(message, args));
    }

    protected void msg(String[] messages, Object... args) {
        for (String str : messages) {
            msg(str, args);
        }
    }

    protected void msg(Collection<String> messages, Object... args) {
        for (String str : messages) {
            msg(str, args);
        }
    }
}
