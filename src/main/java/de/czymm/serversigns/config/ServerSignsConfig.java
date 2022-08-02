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

import de.czymm.serversigns.legacy.OldServerSignsConfig;
import de.czymm.serversigns.persist.PersistenceEntry;
import de.czymm.serversigns.persist.mapping.BlocksMapper;
import de.czymm.serversigns.persist.mapping.ColouredStringMapper;
import de.czymm.serversigns.utils.Version;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ServerSignsConfig implements IServerSignsConfig {
    @PersistenceEntry(configMapper = BlocksMapper.class, comments = {
            "# A list of material names (should be in the Bukkit/Spigot Material enum form)",
            "# These materials define the blocks which can be used with ServerSigns",
            "# Refer to this page for the list: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html"})
    private EnumSet<Material> blocks;

    @PersistenceEntry(comments = {"# Whether or not any block can be used with ServerSigns (overrides 'blocks' list)"})
    private boolean any_block = false;

    @PersistenceEntry(comments = {
            "# The language key which should be used to find the languages file",
            "# For example, 'en' will resolve to the 'ServerSigns/translations/en.yml' file"})
    private String language = "en_default";

    @PersistenceEntry(configMapper = ColouredStringMapper.class, comments = {"# The prefix used in most ServerSigns messages"})
    private String message_prefix = "&2[ServerSigns]";
    @PersistenceEntry(configMapper = ColouredStringMapper.class, comments = {"# The colour for most ServerSigns messages"})
    private String message_colour = "&e";

    @PersistenceEntry(comments = {"# Whether or not Vault should be used for permissions granting"})
    private boolean vault_grant = true;

    @PersistenceEntry(comments = {"# The command to execute from console for setting permissions without Vault"})
    private String permission_add_command = "pex user <player> add <permission>";
    @PersistenceEntry(comments = {"# The command to execute from console for removing permissions without Vault"})
    private String permission_remove_command = "pex user <player> remove <permission>";

    @PersistenceEntry(comments = {"# Whether or not admins must be sneaking to destroy ServerSigns"})
    private boolean sneak_to_destroy = true;

    @PersistenceEntry(comments = {"# Whether or not to display a message to players when funds are removed from account"})
    private boolean show_funds_removed_message = true;

    @PersistenceEntry(comments = {"# The currency string or symbol for use in messages"})
    private String currency_string = "dollars";

    @PersistenceEntry(comments = {"# Whether or not the plugin should announce ServerSigns developers joining your server"})
    private boolean broadcast_developers = true;

    @PersistenceEntry(comments = {"# Whether or not the plugin should automatically check for the latest version and download it"})
    private boolean check_for_updates = true;

    @PersistenceEntry(comments = {"# Whether command logging to the console should be disabled"})
    private boolean disable_command_logging = false;

    @PersistenceEntry(comments = {
            "# Whether the plugin should listen for left clicks as well as right clicks for ServerSign activation",
            "# This option must be enabled to allow ServerSigns to execute different commands for left & right clicks"})
    private boolean allow_left_clicking = true;

    @PersistenceEntry(comments = {"# Whether funds remove via /svs setprice should be sent to a server bank"})
    private boolean send_payments_to_bank = false;
    @PersistenceEntry(comments = {"# The server bank name (used if send_payments_to_bank is set to true)"})
    private String deposit_bank_name = "server";

    @PersistenceEntry(comments = {
            "# Whether the Player#chat() function should be used instead of Player#performCommand() for commands",
            "# If enabled, this means commands executed through ServerSigns will fire the command pre-process event"})
    private boolean alternate_command_dispatching = false;

    @PersistenceEntry(comments = {"# Whether or not you want to opt-out of Metrics statistic gathering through www.mcstats.org"})
    private boolean metrics_opt_out = false;

    @PersistenceEntry(comments = {"# A list of commands which cannot be attached to ServerSigns (to prevent console-only command exploits)"})
    private List<String> blocked_commands = Arrays.asList("op", "deop", "stop");

    @PersistenceEntry(comments = {"# Defines the task delay threshold (in seconds) above which tasks will be persisted to disk"})
    private long task_persist_threshold = 10;

    @PersistenceEntry(comments = {"# Whether or not you want to have tasks that match the defined regex pattern cancelled on player death",
            "# Please note that in servers with over 10,000 queued tasks, the regex search can affect performance"})
    private boolean cancel_tasks_on_death = false;

    @PersistenceEntry(comments = {"# The Regular Expression pattern used when determining which tasks to cancel upon a player's death",
            "# This Regex pattern will be used to compare each pending command a player has on their death; matching commands will be cancelled"})
    private String cancel_tasks_regex_pattern = "warp .*";

    @PersistenceEntry(comments = {"# The number of hours your timezone is offset from GMT/UTC - must be an integer between -12 and 12"})
    private int time_zone_offset = 0;

    ServerSignsConfig() {
        if (Version.isLowerOrEqualsTo(Version.V1_12)) {
            this.blocks = EnumSet.of(
                Material.getMaterial("WALL_SIGN"),
                Material.getMaterial("SIGN_POST")
            );
        } else if (Version.isEqualsTo(Version.V1_13)) {
            this.blocks = EnumSet.of(
                Material.getMaterial("WALL_SIGN"),
                Material.getMaterial("SIGN")
            );
        } else {
            this.blocks = EnumSet.of(
                Material.getMaterial("OAK_SIGN"),
                Material.getMaterial("OAK_WALL_SIGN"),
                Material.getMaterial("ACACIA_SIGN"),
                Material.getMaterial("ACACIA_WALL_SIGN"),
                Material.getMaterial("BIRCH_SIGN"),
                Material.getMaterial("BIRCH_WALL_SIGN"),
                Material.getMaterial("DARK_OAK_SIGN"),
                Material.getMaterial("DARK_OAK_WALL_SIGN"),
                Material.getMaterial("JUNGLE_SIGN"),
                Material.getMaterial("JUNGLE_WALL_SIGN"),
                Material.getMaterial("SPRUCE_SIGN"),
                Material.getMaterial("SPRUCE_WALL_SIGN")
            );
        }
        if (Version.isHigherOrEqualsTo(Version.V1_16)) {
            this.blocks.addAll(EnumSet.of(
                Material.getMaterial("CRIMSON_SIGN"),
                Material.getMaterial("CRIMSON_WALL_SIGN"),
                Material.getMaterial("WARPED_SIGN"),
                Material.getMaterial("WARPED_WALL_SIGN")
            ));
        }
        if (Version.isHigherOrEqualsTo(Version.V1_19)) {
            this.blocks.addAll(EnumSet.of(
                Material.getMaterial("MANGROVE_SIGN"),
                Material.getMaterial("MANGROVE_WALL_SIGN")
            ));
        }
    }

    public EnumSet<Material> getBlocks() {
        return blocks;
    }

    public boolean getAnyBlock() {
        return any_block;
    }

    public String getLanguage() {
        return language;
    }

    public String getMessagePrefix() {
        return message_prefix;
    }

    public String getMessageColour() {
        return message_colour;
    }

    public void colourise() {
        message_prefix = ChatColor.translateAlternateColorCodes('&', message_prefix);
        message_colour = ChatColor.translateAlternateColorCodes('&', message_colour);
    }

    public boolean getVaultGrant() {
        return vault_grant;
    }

    public String getPermissionAddCommand() {
        return permission_add_command;
    }

    public String getPermissionRemoveCommand() {
        return permission_remove_command;
    }

    public boolean getSneakToDestroy() {
        return sneak_to_destroy;
    }

    public boolean getShowFundsRemovedMessage() {
        return show_funds_removed_message;
    }

    public String getCurrencyString() {
        return currency_string;
    }

    public boolean getBroadcastDevelopers() {
        return broadcast_developers;
    }

    public boolean getCheckForUpdates() {
        return check_for_updates;
    }

    public boolean getDisableCommandLogging() {
        return disable_command_logging;
    }

    public boolean getAllowLeftClicking() {
        return allow_left_clicking;
    }

    public boolean getSendPaymentsToBank() {
        return send_payments_to_bank;
    }

    public String getDepositBankName() {
        return deposit_bank_name;
    }

    public boolean getAlternateCommandDispatching() {
        return alternate_command_dispatching;
    }

    public boolean getMetricsOptOut() {
        return metrics_opt_out;
    }

    public List<String> getBlockedCommands() {
        return blocked_commands;
    }

    public long getTaskPersistThreshold() {
        return task_persist_threshold;
    }

    public boolean getCancelTasksOnDeath() {
        return cancel_tasks_on_death;
    }

    public int getTimeZoneOffset() {
        return time_zone_offset;
    }

    private transient TimeZone timeZone;

    public TimeZone getTimeZone() {
        return timeZone == null ? (timeZone = TimeZone.getTimeZone("GMT" + (getTimeZoneOffset() > -1 ? "+" + getTimeZoneOffset() : getTimeZoneOffset()))) : timeZone;
    }

    private transient Pattern compiledCancelTaskPattern;

    public Pattern getCompiledCancelTaskPattern() {
        return compiledCancelTaskPattern;
    }

    public void compilePatterns() throws PatternSyntaxException {
        if (cancel_tasks_regex_pattern.isEmpty()) return;
        compiledCancelTaskPattern = Pattern.compile(cancel_tasks_regex_pattern);
    }

    public void findTimeZone() {
        time_zone_offset = (int) TimeUnit.MILLISECONDS.toHours(TimeZone.getDefault().getRawOffset());
    }

    public void setFromOldConfig(OldServerSignsConfig oldConfig) {
        blocks = oldConfig.getBlocks();
        language = oldConfig.getLanguage();
        message_prefix = oldConfig.getMessageTag();
        message_colour = oldConfig.getMessageColour();
        vault_grant = oldConfig.getVaultGrant();
        permission_add_command = oldConfig.getPermissionConsoleCommandAdd();
        permission_remove_command = oldConfig.getPermissionConsoleCommandRemove();
        sneak_to_destroy = oldConfig.getMustBeSneakingToDestroy();
        show_funds_removed_message = oldConfig.getShowFundsRemovedMessage();
        currency_string = oldConfig.getCurrency();
        broadcast_developers = oldConfig.getBroadcastDevelopers();
        check_for_updates = oldConfig.getAutomaticUpdateChecks();
        disable_command_logging = oldConfig.getDisableConsoleCommandLogging();
        allow_left_clicking = oldConfig.getAllowLeftClicking();
        send_payments_to_bank = oldConfig.getSendPaymentsToBank();
        deposit_bank_name = oldConfig.getDepositBankName();
        alternate_command_dispatching = oldConfig.getAlternateCommandDispatching();
        blocked_commands = oldConfig.getBlockedCommands();
        colourise();
    }

    @Override
    public int getVersion() {
        return 3;
    }
}
