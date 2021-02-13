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

package de.czymm.serversigns.commands;

import de.czymm.serversigns.ServerSignsPlugin;
import de.czymm.serversigns.commands.core.SubCommand;
import de.czymm.serversigns.meta.SVSMetaKey;
import de.czymm.serversigns.meta.SVSMetaValue;
import de.czymm.serversigns.translations.Message;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SubCommandImport extends SubCommand {
    public SubCommandImport(ServerSignsPlugin plugin) {
        super(
                plugin,
                "import",
                "import <path to file>",
                "Import a text file of commands, 1 command per line without /svs",
                "import", "imp"
        );
    }

    @Override
    public void execute(boolean verbose) {
        if (!argSet(0)) {
            if (verbose) sendUsage();
            return;
        }

        String path = loopArgs(0);
        final Path fullPath = Paths.get(plugin.getDataFolder().getAbsolutePath()).resolve(path);

        // Check if file exists and is not a folder
        if (!Files.exists(fullPath) || fullPath.toFile().isDirectory()) {
            if (verbose) msg(Message.IMPORT_FILE_NOT_FOUND);
            return;
        }

        // Check the path is not outside of plugin data folder (plugins/ServerSigns/)
        if (!path.endsWith(".txt") || !fullPath.toAbsolutePath().normalize().startsWith(plugin.getDataFolder().getAbsolutePath())) {
            if (verbose) msg(Message.INVALID_FILE_PATH);
            return;
        }

        if (verbose) msg(Message.IMPORT_SELECT_SIGN);
        applyMeta(SVSMetaKey.IMPORT, new SVSMetaValue(path));
    }
}
