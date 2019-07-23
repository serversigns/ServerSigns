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

package de.czymm.serversigns;

import de.czymm.serversigns.commands.CommandServerSigns;
import de.czymm.serversigns.commands.CommandServerSignsRemote;
import de.czymm.serversigns.commands.core.CommandException;
import de.czymm.serversigns.config.ConfigLoader;
import de.czymm.serversigns.config.ConfigLoadingException;
import de.czymm.serversigns.config.ServerSignsConfig;
import de.czymm.serversigns.hooks.HookManager;
import de.czymm.serversigns.listeners.AdminListener;
import de.czymm.serversigns.listeners.BlockListener;
import de.czymm.serversigns.listeners.PlayerListener;
import de.czymm.serversigns.signs.PlayerInputOptionsManager;
import de.czymm.serversigns.signs.ServerSign;
import de.czymm.serversigns.signs.ServerSignExecutor;
import de.czymm.serversigns.signs.ServerSignManager;
import de.czymm.serversigns.taskmanager.TaskManager;
import de.czymm.serversigns.translations.Message;
import de.czymm.serversigns.translations.MessageHandler;
import de.czymm.serversigns.translations.NoDefaultException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

public class ServerSignsPlugin extends JavaPlugin {
    private static Logger logger;

    public Updater update;
    public PluginManager pm;

    public PlayerListener playerListener;
    public BlockListener blockListener = new BlockListener(this);
    public AdminListener adminListener = new AdminListener(this);

    public ServerSignsConfig config;
    public MessageHandler msgHandler;

    public ServerSignManager serverSignsManager;
    public ServerSignExecutor serverSignExecutor;
    public TaskManager taskManager;
    public HookManager hookManager;

    public PlayerInputOptionsManager inputOptionsManager;

    public static final Random r = new Random();
    private static String serverVersion;

    public ServerSignsPlugin() {
        ServerSignsPlugin.serverVersion = String.join(".", Arrays.asList(this.getServer().getBukkitVersion().split("\\.")).subList(0, 2));
        this.playerListener  = new PlayerListener(this);
    }

    @Override
    public void onEnable() {
        try {
            logger = getLogger();
            Path dataFolder = Files.createDirectories(getDataFolder().toPath());
            loadConfig(dataFolder);

            taskManager = new TaskManager(this, dataFolder);
            taskManager.init();

            serverSignsManager = new ServerSignManager(this);
            final Set<ServerSign> preparedSigns = serverSignsManager.prepareServerSignsSet();

            Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                public void run() {
                    taskManager.start();
                    serverSignsManager.populateSignsMap(preparedSigns);
                }
            }, 1L);

            serverSignExecutor = new ServerSignExecutor(this);
            inputOptionsManager = new PlayerInputOptionsManager(this);

            hookManager = new HookManager(this);
            hookManager.tryInstantiateHooks(false);
            pm = getServer().getPluginManager();

            pm.registerEvents(this.adminListener, this);
            pm.registerEvents(this.playerListener, this);
            pm.registerEvents(this.blockListener, this);
            pm.registerEvents(this.inputOptionsManager, this);

            if (config.getCheckForUpdates()) {
                update = new Updater(this, 33254, this.getFile(), Updater.UpdateType.DEFAULT, true);
            } else {
                log("Update checking skipped - To enable this, set 'check_for_updates' to true in your config.yml");
            }

            if (config.getMetricsOptOut()) {
                log("You have decided to opt-out of Metrics statistic gathering. Enable this by setting 'metrics_opt_out' to false in the config.yml");
            } else {
                try {
                    new Metrics(this);
                } catch (IOException e) {
                    // Failed to submit the stats D:
                }
            }

            log("Version " + getDescription().getVersion() + " is now enabled.");
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Error while enabling " + getDescription().getFullName() + ". Disabling...", ex);
            setEnabled(false);
        }
    }

    @Override
    public void onDisable() {
        if (taskManager != null) {
            taskManager.stop();
        }
        log(getDescription().getName() + " is now disabled.");
    }

    public void loadConfig(Path dataFolder) throws ConfigLoadingException, NoDefaultException {
        config = ConfigLoader.loadConfig(dataFolder.resolve("config.yml"));

        msgHandler = new MessageHandler(this);
        msgHandler.setCurrentTranslation(config.getLanguage());
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String commandLabel, final String[] args) {
        try {
            de.czymm.serversigns.commands.core.Command cmd;
            if (command.getName().equalsIgnoreCase("serversigns")) {
                cmd = new CommandServerSigns(this);
            } else if (command.getName().equalsIgnoreCase("serversignsremote")) {
                cmd = new CommandServerSignsRemote(this);
            } else {
                return false;
            }

            try {
                cmd.run(getServer(), sender, commandLabel, command, args);
                return true;
            } catch (CommandException ex) {
                sender.sendMessage(ex.getMessage());
                return true;
            } catch (Exception ex) {
                send(sender, "Error: An internal error has occurred. If unexpected, please report this at http://dev.bukkit.org/server-mods/serversigns !");
                ex.printStackTrace();
                return true;
            }
        } catch (Throwable ex) {
            getLogger().log(Level.SEVERE, String.format("Failed to execute command '%s' [executor: %s]. Stack trace as follows:", commandLabel, sender.getName()), ex);
            return true;
        }
    }

    public static void log(String log) {
        log(log, Level.INFO, null);
    }

    public static void log(String log, Level level) {
        log(log, level, null);
    }

    public static void log(String log, Level level, Throwable thrown) {
        logger.log(level, log, thrown);
    }

    public void serverCommand(String command) {
        if (!config.getDisableCommandLogging()) {
            log("Executing command: " + command);
        }
        this.getServer().dispatchCommand(getServer().getConsoleSender(), command);
    }

    public void send(CommandSender sender, String message) {
        if (message.isEmpty()) return;
        sender.sendMessage((config.getMessagePrefix().isEmpty() ? "" : ChatColor.DARK_GREEN + config.getMessagePrefix() + " ") + ChatColor.YELLOW + config.getMessageColour() + ChatColor.translateAlternateColorCodes('&', message));
    }

    public void send(CommandSender to, Collection<String> messages) {
        for (String str : messages)
            send(to, str);
    }

    public void send(CommandSender to, Message message) {
        send(to, msgHandler.get(message));
    }

    public void send(CommandSender to, Message message, String... pairedStrings) {
        String msg = msgHandler.get(message);
        String buf = "";
        for (String str : pairedStrings) {
            if (buf.isEmpty()) {
                buf = str;
            } else {
                msg = msg.replaceAll(buf, Matcher.quoteReplacement(str));
                buf = "";
            }
        }
        send(to, msg);
    }

    public void sendBlank(CommandSender to, String message) {
        to.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public ServerSignsConfig getServerSignsConfig() {
        return config;
    }

    public static String getServerVersion() {
        return ServerSignsPlugin.serverVersion;
    }
}