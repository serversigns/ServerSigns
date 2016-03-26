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

package de.czymm.serversigns.legacy;

import de.czymm.serversigns.config.IServerSignsConfig;
import de.czymm.serversigns.persist.PersistenceEntry;
import de.czymm.serversigns.persist.mapping.BlocksIdMapper;
import de.czymm.serversigns.persist.mapping.ColouredStringMapper;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class OldServerSignsConfig implements IServerSignsConfig {
    @PersistenceEntry(configMapper = BlocksIdMapper.class)
    private EnumSet<Material> blocks = EnumSet.of(Material.WALL_SIGN, Material.SIGN_POST);

    @PersistenceEntry
    private String language = "en";

    @PersistenceEntry(configMapper = ColouredStringMapper.class)
    private String messageTag = "&2[ServerSigns]";
    @PersistenceEntry(configMapper = ColouredStringMapper.class)
    private String messageColour = "&e";

    @PersistenceEntry
    private Boolean vault_grant = true;

    @PersistenceEntry
    private String permission_console_command_add = "pex user <player> add <permission>";
    @PersistenceEntry
    private String permission_console_command_remove = "pex user <player> remove <permission>";

    @PersistenceEntry
    private Boolean must_be_sneaking_to_destroy = true;

    @PersistenceEntry
    private Boolean show_funds_removed_message = true;

    @PersistenceEntry
    private String currency = "dollars";

    @PersistenceEntry
    private Boolean broadcastDevelopers = true;

    @PersistenceEntry
    private Boolean automaticUpdateChecks = true;

    @PersistenceEntry
    private Boolean disableConsoleCommandLogging = false;

    @PersistenceEntry
    private Boolean allowLeftClicking = false;

    @PersistenceEntry
    private Boolean send_payments_to_bank = false;
    @PersistenceEntry
    private String deposit_bank_name = "server";

    @PersistenceEntry
    private Boolean alternate_command_dispatching = false;

    @PersistenceEntry(configPath = "blocked-commands")
    private List<String> blockedCommands = Arrays.asList("op", "deop", "stop");


    public EnumSet<Material> getBlocks() {
        return blocks;
    }

    public String getLanguage() {
        return language;
    }

    public String getMessageTag() {
        return messageTag;
    }

    public String getMessageColour() {
        return messageColour;
    }

    public Boolean getVaultGrant() {
        return vault_grant;
    }

    public String getPermissionConsoleCommandAdd() {
        return permission_console_command_add;
    }

    public String getPermissionConsoleCommandRemove() {
        return permission_console_command_remove;
    }

    public Boolean getMustBeSneakingToDestroy() {
        return must_be_sneaking_to_destroy;
    }

    public Boolean getShowFundsRemovedMessage() {
        return show_funds_removed_message;
    }

    public String getCurrency() {
        return currency;
    }

    public Boolean getBroadcastDevelopers() {
        return broadcastDevelopers;
    }

    public Boolean getAutomaticUpdateChecks() {
        return automaticUpdateChecks;
    }

    public Boolean getDisableConsoleCommandLogging() {
        return disableConsoleCommandLogging;
    }

    public Boolean getAllowLeftClicking() {
        return allowLeftClicking;
    }

    public Boolean getSendPaymentsToBank() {
        return send_payments_to_bank;
    }

    public String getDepositBankName() {
        return deposit_bank_name;
    }

    public Boolean getAlternateCommandDispatching() {
        return alternate_command_dispatching;
    }

    public List<String> getBlockedCommands() {
        return blockedCommands;
    }

    @Override
    public int getVersion() {
        return 0;
    }
}
