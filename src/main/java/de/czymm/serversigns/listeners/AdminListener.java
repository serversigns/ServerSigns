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

package de.czymm.serversigns.listeners;

import com.google.common.io.Files;
import de.czymm.serversigns.ServerSignsPlugin;
import de.czymm.serversigns.commands.ExecutableSVSR;
import de.czymm.serversigns.itemdata.ItemSearchCriteria;
import de.czymm.serversigns.meta.SVSMeta;
import de.czymm.serversigns.meta.SVSMetaKey;
import de.czymm.serversigns.meta.SVSMetaManager;
import de.czymm.serversigns.meta.SVSMetaValue;
import de.czymm.serversigns.parsing.command.ServerSignCommand;
import de.czymm.serversigns.signs.ClickType;
import de.czymm.serversigns.signs.PlayerInputOptions;
import de.czymm.serversigns.signs.ServerSign;
import de.czymm.serversigns.signs.ServerSignExecData;
import de.czymm.serversigns.translations.Message;
import de.czymm.serversigns.utils.ItemUtils;
import de.czymm.serversigns.utils.StringUtils;
import de.czymm.serversigns.utils.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class AdminListener implements Listener {
    private final ServerSignsPlugin plugin;

    public AdminListener(ServerSignsPlugin instance) {
        this.plugin = instance;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeveloperJoin(PlayerJoinEvent event) {
        if (event.getPlayer().getName().equalsIgnoreCase("ExLoki") || event.getPlayer().getName().equalsIgnoreCase("CalibeR93") || event.getPlayer().getName().equalsIgnoreCase("Steffencz")) {
            if (plugin.config.getBroadcastDevelopers()) {
                Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "[ServerSigns] " + ChatColor.YELLOW + event.getPlayer().getName() + " is an author of ServerSigns!");
            }
            event.getPlayer().sendMessage("Running version: " + plugin.getDescription().getVersion());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void adminChatCheck(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (SVSMetaManager.hasMeta(player)) {
            SVSMeta meta = SVSMetaManager.getMeta(player);
            if (meta.getKey().equals(SVSMetaKey.LONG)) {
                // Cancel event, append message to long list
                event.setCancelled(true);
                meta.addValue(new SVSMetaValue(message.trim()));

                // Message to let them know what's happening (in case they've forgotten)
                plugin.send(player, Message.LONG_COMMAND_AGAIN);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void adminInteractCheck(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (SVSMetaManager.hasExclusiveMeta(player, SVSMetaKey.YES) && (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.LEFT_CLICK_BLOCK))) {
            Block block = event.getClickedBlock();
            if (block == null) return;

            Location location = block.getLocation();
            handleAdminInteract(location, ClickType.fromAction(event.getAction()), player, player.getUniqueId());
            if (!SVSMetaManager.hasInclusiveMeta(player, SVSMetaKey.COPY))
                event.setCancelled(true); // Don't cancel interact on unsuccessful copies
        }
    }

    public void handleAdminInteract(Location clicked, ClickType clickType, CommandSender recipient, UUID adminUUID) {
        ServerSign sign = plugin.serverSignsManager.getServerSignByLocation(clicked);
        ServerSignExecData execData = sign == null ? null : sign.getServerSignExecutorData(clickType);

        if (sign == null && !plugin.config.getAnyBlock() && !plugin.config.getBlocks().contains(clicked.getBlock().getType()))
            return; // Not a sign, can't be a sign; we don't care

        SVSMeta meta = SVSMetaManager.getMeta(adminUUID);

        boolean saveRemoveExit = false;
        switch (meta.getKey()) {
            case ADD:
                if (execData != null) {
                    execData.addCommand(meta.getValue().asServerSignCommand());
                } else {
                    execData = new ServerSignExecData(meta.getValue().asServerSignCommand());
                    if (sign == null) {
                        sign = new ServerSign(clicked, clickType, execData);
                    } else {
                        sign.setServerSignExecutorData(clickType, execData);
                    }
                }

                plugin.send(recipient, Message.COMMAND_SET);
                saveRemoveExit = true;
                break;

            case CANCEL:
                if (execData == null) return;

                execData.setCancelMode(meta.getValue().asCancelMode());

                plugin.send(recipient, Message.SET_CANCEL_MODE, "<mode>", meta.getValue().asCancelMode().name());
                saveRemoveExit = true;
                break;

            case CANCEL_PERMISSION:
                if (execData == null) return;

                execData.setCancelPermission(meta.getValue().asString());
                if (meta.hasValue(1)) {
                    execData.setCancelPermissionMessage(meta.getValue(1).asString());
                }
                plugin.send(recipient, Message.CANCEL_PERMISSION_SET);
                saveRemoveExit = true;
                break;

            case CONFIRMATION:
                if (execData == null) return;
                execData.setConfirmation(meta.getValue().asBoolean());
                if (meta.hasValue(1)) execData.setConfirmationMessage(meta.getValue(1).asString());

                plugin.send(recipient, Message.CONFIRMATION_SET, "<boolean>", meta.getValue().asBoolean() + "");
                saveRemoveExit = true;
                break;

            case COOLDOWN_RESET:
                if (execData == null) return;
                execData.resetCooldowns();

                plugin.send(recipient, Message.RESET_COOLDOWN);
                saveRemoveExit = true;
                break;

            case COPY:
                ServerSign copiedSign = meta.hasValue(1) ? meta.getValue(1).asServerSign() : null;
                if (copiedSign == null && sign == null) return;

                if (sign == null) {
                    sign = plugin.serverSignsManager.copy(copiedSign);
                    if (sign == null) {
                        plugin.send(recipient, "An error occurred, please refer to console for details.");
                        SVSMetaManager.removeMeta(adminUUID);
                        return;
                    }
                    sign.setLocation(clicked);

                    plugin.serverSignsManager.save(sign);
                    plugin.send(recipient, Message.COPY_SUCCESS);
                    if (meta.getValue().asBoolean()) plugin.send(recipient, Message.CLICK_PASTE);
                    else SVSMetaManager.removeMeta(adminUUID);
                } else if (copiedSign == null) {
                    if (meta.hasValue(1)) meta.removeValue(1);
                    meta.addValue(new SVSMetaValue(sign));
                    plugin.send(recipient, Message.CLICK_PASTE);
                }
                // Don't allow existing signs to be overwritten
                return;

            case CREATE:
                if (sign != null) return;

                sign = new ServerSign();
                sign.setLocation(clicked);
                sign.setServerSignExecutorData(clickType, new ServerSignExecData());

                plugin.send(recipient, Message.CREATE_SUCCESS);
                saveRemoveExit = true;
                break;

            case DEFAULT_CLICKTYPE:
                if (sign == null) return;

                sign.setDefaultClickType(meta.getValue().asClickType());

                plugin.send(recipient, Message.DEFAULT_EXECUTOR_SET);
                saveRemoveExit = true;
                break;

            case EDIT:
                if (execData == null) return;

                int index = meta.getValue().asInt();
                ServerSignCommand cmd = meta.getValue(1).asServerSignCommand();

                if (index > execData.getCommands().size() || index < 1) {
                    plugin.send(recipient, Message.INVALID_INDEX);
                    return;
                }

                plugin.send(recipient, Message.COMMAND_EDITED);
                execData.editCommand(index - 1, cmd);
                saveRemoveExit = true;
                break;

            case GRANT:
                if (execData == null) return;

                if (meta.getValue().asBoolean()) {
                    execData.addGrantPermissions(meta.getValue(1).asString());
                    plugin.send(recipient, Message.PERMISSION_SET);
                } else {
                    execData.removeGrantPermissions();
                    plugin.send(recipient, Message.PERMISSION_REMOVED);
                }

                saveRemoveExit = true;
                break;

            case HOLDING:
                if (execData == null) return;

                if (meta.getValue().asObject() == null) {
                    execData.clearHeldItems();
                    plugin.send(recipient, Message.HOLDING_REMOVED);
                } else {
                    execData.addHeldItem(meta.getValue().asItemStack());
                    plugin.send(recipient, Message.HOLDING_SUCCESS);
                }

                saveRemoveExit = true;
                break;

            case HELD_ITEM_CRITERIA:
                if (execData == null) return;

                execData.setHIC(new ItemSearchCriteria(meta.getValue().asBoolean(), meta.getValue(2).asBoolean(),
                        meta.getValue(1).asBoolean(), meta.getValue(3).asBoolean()));

                plugin.send(recipient, Message.HELD_ITEM_CRITERIA_SET);
                saveRemoveExit = true;
                break;

            case IMPORT:
                if (execData == null) return;

                Path path = Paths.get(meta.getValue().asString());
                try {
                    List<String> commands = Files.readLines(path.toFile(), Charset.defaultCharset());
                    ExecutableSVSR svsr = new ExecutableSVSR(plugin);
                    for (String command : commands) {
                        svsr.execute(clicked, (Player) recipient, clickType, command);
                    }
                } catch (Exception ex) {
                    plugin.send(recipient, "An error occurred, please refer to console for details.");
                    ServerSignsPlugin.log("An error occurred while importing file '" + path.toString() + "'", Level.WARNING, ex);
                    return;
                }

                plugin.send(recipient, Message.IMPORT_SUCCESS, "<string>", path.toString());
                saveRemoveExit = true;
                break;

            case INSERT:
                if (execData == null) return;

                int insertIndex = meta.getValue().asInt();
                ServerSignCommand insertCmd = meta.getValue(1).asServerSignCommand();

                if (insertIndex > execData.getCommands().size()) {
                    plugin.send(recipient, Message.INVALID_INDEX);
                    return;
                }

                plugin.send(recipient, Message.COMMAND_SET);
                execData.getCommands().add(insertIndex - 1, insertCmd);
                saveRemoveExit = true;
                break;

            case LIST:
                plugin.send(recipient, String.format("&6Coordinates: &e%s&7, &e%d&7, &e%d&7, &e%d", clicked.getWorld().getName(), clicked.getBlockX(), clicked.getBlockY(), clicked.getBlockZ()));

                if (execData != null) {
                    if (!execData.getPermissions().isEmpty())
                        plugin.send(recipient, "&6Permissions: &e" + StringUtils.join(execData.getPermissions(), ", "));

                    if (!execData.getCancelPermission().isEmpty()) {
                        plugin.send(recipient, "&6Cancel Permission: &e" + execData.getCancelPermission());
                        if (!execData.getCancelPermissionMessage().isEmpty())
                            plugin.send(recipient, "&6Cancel Perm Message: &e" + execData.getCancelPermissionMessage());
                    }

                    if (!execData.getPermissionMessage().isEmpty())
                        plugin.send(recipient, "&6No Perm Message: &e" + execData.getPermissionMessage());

                    if (execData.getPrice() != 0)
                        plugin.send(recipient, "&6Price: &e" + execData.getPrice());

                    if (execData.getXP() != 0)
                        plugin.send(recipient, "&6Xp Cost: &e" + execData.getXP());

                    if (execData.isConfirmation())
                        plugin.send(recipient, "&6Confirmation: &etrue" + (execData.getConfirmationMessage().isEmpty() ? "" : ", &6Message: &e" + execData.getConfirmationMessage()));

                    if (execData.getCooldown() != 0)
                        plugin.send(recipient, "&6Cooldown: &e" + TimeUtils.getTimeSpan(execData.getCooldown() * 1000, TimeUtils.TimeUnit.SECONDS, TimeUtils.TimeUnit.YEARS, true, false));

                    if (execData.getGlobalCooldown() != 0)
                        plugin.send(recipient, "&6Global Cooldown: &e" + TimeUtils.getTimeSpan(execData.getGlobalCooldown() * 1000, TimeUtils.TimeUnit.SECONDS, TimeUtils.TimeUnit.YEARS, true, false));

                    if (sign.getLoops() >= 0) {
                        plugin.send(recipient, "&6Loop count: &e" + sign.getLoops());
                        plugin.send(recipient, "&6Loop delay: &e" + sign.getLoopDelayInSecs() + "s");
                    }

                    if (execData.getUseLimit() > 0) {
                        plugin.send(recipient, "&6Use limit: &e" + execData.getUseLimit());
                    }
                    plugin.send(recipient, "&6Use tally: &e" + execData.getUseTally());

                    if (!execData.getPriceItems().isEmpty()) {
                        plugin.send(recipient, "&6Price Items: ");
                        for (ItemStack stack : execData.getPriceItems()) {
                            plugin.send(recipient, ItemUtils.getDescription(stack, plugin.config.getMessageColour()));
                        }

                        plugin.send(recipient, "&6Price Item Criteria: &a&oTrue &c&oFalse");
                        plugin.send(recipient, execData.getPIC().getColouredString(ChatColor.GREEN, ChatColor.RED));
                    }

                    if (!execData.getHeldItems().isEmpty()) {
                        plugin.send(recipient, "&6Held Items: ");
                        for (ItemStack stack : execData.getHeldItems()) {
                            plugin.send(recipient, ItemUtils.getDescription(stack, plugin.config.getMessageColour()));
                        }

                        plugin.send(recipient, "&6Held Item Criteria: ");
                        plugin.send(recipient, execData.getHIC().getColouredString(ChatColor.GREEN, ChatColor.RED));
                    }

                    if (!execData.getGrantPermissions().isEmpty()) {
                        plugin.send(recipient, "&6Grant Permissions: ");
                        for (String str : execData.getGrantPermissions()) {
                            plugin.send(recipient, "- " + str);
                        }
                    }

                    if (!sign.shouldDisplayInternalMessages()) {
                        plugin.send(recipient, "&6Silent: &e" + "true");
                    }


                    if (execData.getTimeLimitMinimum() > 0) {
                        plugin.send(recipient, "&6Time Limit (min): &e" + TimeUtils.getFormattedTime(execData.getTimeLimitMinimum(), "d MMM yyyy hh:mm:ss a"));
                    }

                    if (execData.getTimeLimitMaximum() > 0) {
                        plugin.send(recipient, "&6Time Limit (max): &e" + TimeUtils.getFormattedTime(execData.getTimeLimitMaximum(), "d MMM yyyy hh:mm:ss a"));
                    }

                    if (!execData.getInputOptions().isEmpty()) {
                        plugin.send(recipient, "&6'Option Menus' (Q+As): ");
                        for (PlayerInputOptions options : execData.getInputOptions()) {
                            plugin.send(recipient, "&bID: " + options.getName());
                            plugin.send(recipient, "  &9" + options.getQuestion());
                            for (int k = 0; k < options.getAnswersLength(); k++) {
                                plugin.send(recipient, "  &3" + options.getAnswerLabel(k) + " - " + options.getAnswerDescription(k));
                            }
                        }
                    }

                    plugin.send(recipient, "&6Cancel interact event mode: &e" + execData.getCancelMode().name());

                    if (!execData.getCommands().isEmpty()) {
                        plugin.send(recipient, "&6Commands: ");
                        plugin.send(recipient, "&oLine #: &c&oType &a&oDelay &9&oPerms &d&o[ap] &f&oCommand");
                        StringBuilder builder;
                        int k = 1;
                        for (ServerSignCommand line : execData.getCommands()) {
                            builder = new StringBuilder();
                            builder.append(k++).append(": &c").append(line.getType().toString().toLowerCase()).append(" ");
                            if (line.getDelay() > 0) {
                                builder.append("&a").append(TimeUtils.getTimeSpan(line.getDelay() * 1000, TimeUtils.TimeUnit.SECONDS, TimeUtils.TimeUnit.YEARS, true, false)).append(" ");
                            }
                            if (!line.getGrantPermissions().isEmpty()) {
                                builder.append("&9");
                                for (String perm : line.getGrantPermissions()) {
                                    builder.append(perm).append(" ");
                                }
                            }
                            if (line.isAlwaysPersisted()) {
                                builder.append("&dtrue ");
                            }
                            builder.append("&f").append(line.getUnformattedCommand());
                            plugin.send(recipient, builder.toString().trim());
                        }
                    }

                    if (!SVSMetaManager.getMeta(adminUUID).getValue().asBoolean()) { // Check if they have persist mode on
                        SVSMetaManager.removeMeta(adminUUID); // Only remove meta if they clicked a ServerSign
                    }
                }
                break;

            case LONG:
                plugin.send(recipient, Message.LONG_COMMAND_AGAIN);
                break;

            case LOOP:
                if (execData == null) return;

                sign.setLoops(meta.getValue().asInt());
                if (meta.getValue().asInt() > -1) sign.setLoopDelay(meta.getValue(1).asInt());
                plugin.send(recipient, Message.SET_LOOPS);
                saveRemoveExit = true;
                break;

            case OPTION:
                if (execData == null) return;

                String optionName = meta.getValue().asString();
                int optionId = meta.getValue(1).asInt();

                if (optionId == 0) {
                    execData.setInputOptionQuestion(optionName, meta.getValue(2).asString());
                } else {
                    if (!execData.containsInputOption(optionName)) {
                        plugin.send(recipient, Message.OPTION_CREATE_W_QUESTION);
                        SVSMetaManager.removeMeta(adminUUID);
                        return;
                    }

                    if (optionId == 1) {
                        if (execData.getInputOption(optionName).isValidAnswerLabel(meta.getValue(2).asString())) {
                            plugin.send(recipient, Message.OPTION_LABEL_UNIQUE);
                            SVSMetaManager.removeMeta(adminUUID);
                            return;
                        }
                        execData.addInputOptionAnswer(optionName, meta.getValue(2).asString(), meta.getValue(3).asString());
                    } else if (optionId == 2) {
                        execData.removeInputOptionAnswer(optionName, meta.getValue(2).asString());
                    }
                }
                saveRemoveExit = true;
                plugin.send(recipient, Message.OPTION_SET);
                break;

            case PERMISSION:
                if (execData == null) return;

                if (meta.getValue().asObject() == null) {
                    execData.setPermissions(new ArrayList<String>());
                    plugin.send(recipient, Message.PERMISSIONS_REMOVED);
                } else {
                    if (!meta.getValue().asStringList().isEmpty()) { // Allows just the message to be set
                        execData.setPermissions(meta.getValue().asStringList());
                    }
                    if (meta.hasValue(1)) {
                        execData.setPermissionMessage(meta.getValue(1).asString());
                    }
                    plugin.send(recipient, Message.PERMISSION_SET);
                }

                saveRemoveExit = true;
                break;

            case PRICE:
                if (execData == null) return;

                execData.setPrice(meta.getValue().asDouble());
                plugin.send(recipient, Message.PRICE_SET);
                saveRemoveExit = true;
                break;

            case PRICE_ITEM:
                if (execData == null) return;

                if (meta.getValue().asObject() == null) {
                    execData.clearPriceItems();
                    plugin.send(recipient, Message.PRICE_ITEM_REMOVED);
                } else {
                    execData.addPriceItem(meta.getValue().asItemStack());
                    plugin.send(recipient, Message.PRICE_ITEM_SUCCESS);
                }

                saveRemoveExit = true;
                break;

            case PRICE_ITEM_CRITERIA:
                if (execData == null) return;

                execData.setPIC(new ItemSearchCriteria(meta.getValue().asBoolean(), meta.getValue(2).asBoolean(),
                        meta.getValue(1).asBoolean(), meta.getValue(3).asBoolean()));

                plugin.send(recipient, Message.PRICE_ITEM_CRITERIA_SET);
                saveRemoveExit = true;
                break;

            case REMOVE:
                if (execData == null) return;

                int lineNumber = meta.getValue().asInt();
                if (lineNumber < 0) {
                    plugin.serverSignsManager.remove(sign);

                    plugin.send(recipient, Message.COMMANDS_REMOVED);
                } else if (lineNumber > execData.getCommands().size() || lineNumber < 1) {
                    plugin.send(recipient, Message.LINE_NOT_FOUND);
                    return;
                } else {
                    execData.removeCommand(lineNumber - 1);
                    plugin.serverSignsManager.save(sign);
                    plugin.send(recipient, Message.COMMAND_REMOVED);
                }

                SVSMetaManager.removeMeta(adminUUID);
                break;

            case SELECT:
                if (execData == null) return;

                SVSMetaManager.removeMeta(adminUUID);
                SVSMetaManager.setSpecialMeta(adminUUID, new SVSMeta(SVSMetaKey.SELECT, new SVSMetaValue(clicked)));
                plugin.send(recipient, Message.SIGN_SELECTED);
                break;

            case SET_COOLDOWN:
                if (execData == null) return;

                execData.setCooldown(meta.getValue().asLong());
                plugin.send(recipient, Message.COOLDOWN_SET);
                saveRemoveExit = true;
                break;

            case SET_GLOBAL_COOLDOWN:
                if (execData == null) return;

                execData.setGlobalCooldown(meta.getValue().asLong());
                plugin.send(recipient, Message.COOLDOWN_SET);
                saveRemoveExit = true;
                break;

            case SILENT:
                if (execData == null) return;

                sign.setDisplayInternalMessages(meta.getValue().asBoolean());
                plugin.send(recipient, Message.SILENT_SUCCESS);
                saveRemoveExit = true;
                break;

            case TIME_LIMIT:
                if (execData == null) return;

                // -1 keep current value | 0 remove current value | >0 set as current value
                if (meta.getValue().asLong() >= 0) {
                    execData.setTimeLimitMinimum(meta.getValue().asLong());
                }
                if (meta.getValue(1).asLong() >= 0) {
                    execData.setTimeLimitMaximum(meta.getValue(1).asLong());
                }

                plugin.send(recipient, Message.TIMELIMIT_SUCCESS);
                saveRemoveExit = true;
                break;

            case USES:
                if (execData == null) return;

                execData.setUseLimit(meta.getValue().asInt());
                plugin.send(recipient, Message.USES_SUCCESS);
                saveRemoveExit = true;
                break;

            case XP:
                if (execData == null) return;

                int value = meta.getValue().asInt();
                if (value == 0) {
                    plugin.send(recipient, Message.XP_COST_REMOVED);
                } else {
                    plugin.send(recipient, Message.XP_SET);
                }

                execData.setXP(value);
                saveRemoveExit = true;
                break;

            case YES:
                break;
        }

        if (saveRemoveExit) {
            plugin.serverSignsManager.save(sign);
            SVSMetaManager.removeMeta(adminUUID);
        }
    }
}
