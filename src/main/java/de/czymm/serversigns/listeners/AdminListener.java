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
import de.czymm.serversigns.signs.CancelMode;
import de.czymm.serversigns.signs.PlayerInputOptions;
import de.czymm.serversigns.signs.ServerSign;
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

import java.nio.charset.StandardCharsets;
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

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
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

        if (SVSMetaManager.hasExclusiveMeta(player, SVSMetaKey.YES) && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Block block = event.getClickedBlock();
            if (block == null) return;

            Location location = block.getLocation();
            handleAdminInteract(location, player, player.getUniqueId());
            if (!SVSMetaManager.hasInclusiveMeta(player, SVSMetaKey.COPY))
                event.setCancelled(true); // Don't cancel interact on unsuccessful copies
        }
    }

    public void handleAdminInteract(Location clicked, CommandSender recipient, UUID adminUUID) {
        ServerSign sign = plugin.serverSignsManager.getServerSignByLocation(clicked);
        if (sign == null && !plugin.config.getAnyBlock() && !plugin.config.getBlocks().contains(clicked.getBlock().getType()))
            return; // Not a sign, can't be a sign; we don't care

        SVSMeta meta = SVSMetaManager.getMeta(adminUUID);

        boolean saveRemoveExit = false;
        switch (meta.getKey()) {
            case ADD:
                if (sign != null) sign.addCommand(meta.getValue().asServerSignCommand());
                else sign = new ServerSign(clicked, meta.getValue().asServerSignCommand());

                plugin.send(recipient, Message.COMMAND_SET);
                saveRemoveExit = true;
                break;

            case CANCEL:
                if (sign == null) return;
                String raw = meta.getValue().asString();
                CancelMode mode = CancelMode.valueOf(raw);
                sign.setCancelMode(mode);

                plugin.send(recipient, Message.SET_CANCEL_MODE, "<mode>", raw);
                saveRemoveExit = true;
                break;

            case CANCEL_PERMISSION:
                if (sign == null) return;

                sign.setCancelPermission(meta.getValue().asString());
                if (meta.hasValue(1)) {
                    sign.setCancelPermissionMessage(meta.getValue(1).asString());
                }
                plugin.send(recipient, Message.CANCEL_PERMISSION_SET);
                saveRemoveExit = true;
                break;

            case CONFIRMATION:
                if (sign == null) return;
                sign.setConfirmation(meta.getValue().asBoolean());
                if (meta.hasValue(1)) sign.setConfirmationMessage(meta.getValue(1).asString());

                plugin.send(recipient, Message.CONFIRMATION_SET, "<boolean>", meta.getValue().asBoolean() + "");
                saveRemoveExit = true;
                break;

            case COOLDOWN_RESET:
                if (sign == null) return;
                sign.resetCooldowns();

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
                    if (meta.getValue().asBoolean()) plugin.send(recipient, Message.RIGHT_CLICK_PASTE);
                    else SVSMetaManager.removeMeta(adminUUID);
                } else if (copiedSign == null) {
                    if (meta.hasValue(1)) meta.removeValue(1);
                    meta.addValue(new SVSMetaValue(sign));
                    plugin.send(recipient, Message.RIGHT_CLICK_PASTE);
                }
                // Don't allow existing signs to be overwritten
                return;

            case CREATE:
                sign = new ServerSign();
                sign.setLocation(clicked);

                plugin.send(recipient, Message.CREATE_SUCCESS);
                saveRemoveExit = true;
                break;

            case EDIT:
                if (sign == null) return;

                int index = meta.getValue().asInt();
                ServerSignCommand cmd = meta.getValue(1).asServerSignCommand();

                if (index > sign.getCommands().size() || index < 1) {
                    plugin.send(recipient, Message.INVALID_INDEX);
                    return;
                }

                plugin.send(recipient, Message.COMMAND_EDITED);
                sign.editCommand(index - 1, cmd);
                saveRemoveExit = true;
                break;

            case GRANT:
                if (sign == null) return;

                if (meta.getValue().asBoolean()) {
                    sign.addGrantPermissions(meta.getValue(1).asString());
                    plugin.send(recipient, Message.PERMISSION_SET);
                } else {
                    sign.removeGrantPermissions();
                    plugin.send(recipient, Message.PERMISSION_REMOVED);
                }

                saveRemoveExit = true;
                break;

            case HOLDING:
                if (sign == null) return;

                if (meta.getValue().asObject() == null) {
                    sign.clearHeldItems();
                    plugin.send(recipient, Message.HOLDING_REMOVED);
                } else {
                    sign.addHeldItem(meta.getValue().asItemStack());
                    plugin.send(recipient, Message.HOLDING_SUCCESS);
                }

                saveRemoveExit = true;
                break;

            case HELD_ITEM_CRITERIA:
                if (sign == null) return;

                sign.setHIC(new ItemSearchCriteria(meta.getValue().asBoolean(), meta.getValue(2).asBoolean(),
                        meta.getValue(1).asBoolean(), meta.getValue(3).asBoolean()));

                plugin.send(recipient, Message.HELD_ITEM_CRITERIA_SET);
                saveRemoveExit = true;
                break;

            case IMPORT:
                if (sign == null) return;

                Path path = Paths.get(meta.getValue().asString());
                try {
                    List<String> commands = Files.readLines(path.toFile(), StandardCharsets.UTF_8);
                    ExecutableSVSR svsr = new ExecutableSVSR(plugin);
                    for (String command : commands) {
                        svsr.execute(clicked, (Player) recipient, command);
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
                if (sign == null) return;

                int insertIndex = meta.getValue().asInt();
                ServerSignCommand insertCmd = meta.getValue(1).asServerSignCommand();

                if (insertIndex > sign.getCommands().size()) {
                    plugin.send(recipient, Message.INVALID_INDEX);
                    return;
                }

                plugin.send(recipient, Message.COMMAND_SET);
                sign.getCommands().add(insertIndex - 1, insertCmd);
                saveRemoveExit = true;
                break;

            case LIST:
                plugin.send(recipient, String.format("&6Coordinates: &e%s&7, &e%d&7, &e%d&7, &e%d", clicked.getWorld().getName(), clicked.getBlockX(), clicked.getBlockY(), clicked.getBlockZ()));

                if (sign != null) {
                    if (!sign.getPermissions().isEmpty())
                        plugin.send(recipient, "&6Permissions: &e" + StringUtils.join(sign.getPermissions(), ", "));

                    if (!sign.getCancelPermission().isEmpty()) {
                        plugin.send(recipient, "&6Cancel Permission: &e" + sign.getCancelPermission());
                        if (!sign.getCancelPermissionMessage().isEmpty())
                            plugin.send(recipient, "&6Cancel Perm Message: &e" + sign.getCancelPermissionMessage());
                    }

                    if (!sign.getPermissionMessage().isEmpty())
                        plugin.sendBasic(recipient, "&6No Perm Message: &e" + sign.getPermissionMessage());

                    if (sign.getPrice() != 0)
                        plugin.send(recipient, "&6Price: &e" + sign.getPrice());

                    if (sign.getXP() != 0)
                        plugin.send(recipient, "&6Xp Cost: &e" + sign.getXP());

                    if (sign.isConfirmation())
                        plugin.sendBasic(recipient, "&6Confirmation: &etrue" + (sign.getConfirmationMessage().isEmpty() ? "" : ", &6Message: &e" + sign.getConfirmationMessage()));

                    if (sign.getCooldown() != 0)
                        plugin.send(recipient, "&6Cooldown: &e" + TimeUtils.getTimeSpan(sign.getCooldown() * 1000, TimeUtils.TimeUnit.SECONDS, TimeUtils.TimeUnit.YEARS, true, false));

                    if (sign.getGlobalCooldown() != 0)
                        plugin.send(recipient, "&6Global Cooldown: &e" + TimeUtils.getTimeSpan(sign.getGlobalCooldown() * 1000, TimeUtils.TimeUnit.SECONDS, TimeUtils.TimeUnit.YEARS, true, false));

                    if (sign.getLoops() >= 0) {
                        plugin.send(recipient, "&6Loop count: &e" + sign.getLoops());
                        plugin.send(recipient, "&6Loop delay: &e" + sign.getLoopDelayInSecs() + "s");
                    }

                    if (sign.getUseLimit() > 0) {
                        plugin.send(recipient, "&6Use limit: &e" + sign.getUseLimit());
                    }
                    plugin.send(recipient, "&6Use tally: &e" + sign.getUseTally());

                    if (!sign.getPriceItems().isEmpty()) {
                        plugin.send(recipient, "&6Price Items: ");
                        for (ItemStack stack : sign.getPriceItems()) {
                            plugin.sendBasic(recipient, ItemUtils.getDescription(stack, plugin.config.getMessageColour()));
                        }

                        plugin.send(recipient, "&6Price Item Criteria: &a&oTrue &c&oFalse");
                        plugin.send(recipient, sign.getPIC().getColouredString(ChatColor.GREEN, ChatColor.RED));
                    }

                    if (!sign.getHeldItems().isEmpty()) {
                        plugin.send(recipient, "&6Held Items: ");
                        for (ItemStack stack : sign.getHeldItems()) {
                            plugin.send(recipient, ItemUtils.getDescription(stack, plugin.config.getMessageColour()));
                        }

                        plugin.send(recipient, "&6Held Item Criteria: ");
                        plugin.send(recipient, sign.getHIC().getColouredString(ChatColor.GREEN, ChatColor.RED));
                    }

                    if (!sign.getGrantPermissions().isEmpty()) {
                        plugin.send(recipient, "&6Grant Permissions: ");
                        for (String str : sign.getGrantPermissions()) {
                            plugin.send(recipient, "- " + str);
                        }
                    }

                    if (!sign.shouldDisplayInternalMessages()) {
                        plugin.send(recipient, "&6Silent: &e" + "true");
                    }


                    if (sign.getTimeLimitMinimum() > 0) {
                        plugin.send(recipient, "&6Time Limit (min): &e" + TimeUtils.getFormattedTime(sign.getTimeLimitMinimum(), "d MMM yyyy hh:mm:ss a"));
                    }

                    if (sign.getTimeLimitMaximum() > 0) {
                        plugin.send(recipient, "&6Time Limit (max): &e" + TimeUtils.getFormattedTime(sign.getTimeLimitMaximum(), "d MMM yyyy hh:mm:ss a"));
                    }

                    if (!sign.getInputOptions().isEmpty()) {
                        plugin.sendBasic(recipient, "&6'Option Menus' (Q+As): ");
                        for (PlayerInputOptions options : sign.getInputOptions()) {
                            plugin.sendBasic(recipient, "&bID: " + options.getName());
                            plugin.sendBasic(recipient, "  &9" + options.getQuestion());
                            for (int k = 0; k < options.getAnswersLength(); k++) {
                                plugin.sendBasic(recipient, "  &3" + options.getAnswerLabel(k) + " - " + options.getAnswerDescription(k));
                            }
                        }
                    }

                    plugin.send(recipient, "&6Cancel interact event mode: &e" + sign.getCancelMode().name());

                    if (!sign.getCommands().isEmpty()) {
                        plugin.send(recipient, "&6Commands: ");
                        plugin.send(recipient, "&oLine #: &c&oType &a&oDelay &9&oPerms &d&o[ap] &7&oClick &f&oCommand");
                        StringBuilder builder;
                        int k = 1;
                        for (ServerSignCommand line : sign.getCommands()) {
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
                            builder.append("&7").append(line.getInteractValue() == 0 ? "both " : line.getInteractValue() == 1 ? "left " : "right ");
                            builder.append("&f").append(line.getUnformattedCommand());
                            plugin.sendBasic(recipient, builder.toString().trim());
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
                if (sign == null) return;

                sign.setLoops(meta.getValue().asInt());
                if (meta.getValue().asInt() > -1) sign.setLoopDelay(meta.getValue(1).asInt());
                plugin.send(recipient, Message.SET_LOOPS);
                saveRemoveExit = true;
                break;

            case OPTION:
                if (sign == null) return;

                String optionName = meta.getValue().asString();
                int optionId = meta.getValue(1).asInt();

                if (optionId == 0) {
                    sign.setInputOptionQuestion(optionName, meta.getValue(2).asString());
                } else {
                    if (!sign.containsInputOption(optionName)) {
                        plugin.send(recipient, Message.OPTION_CREATE_W_QUESTION);
                        SVSMetaManager.removeMeta(adminUUID);
                        return;
                    }

                    if (optionId == 1) {
                        if (sign.getInputOption(optionName).isValidAnswerLabel(meta.getValue(2).asString())) {
                            plugin.send(recipient, Message.OPTION_LABEL_UNIQUE);
                            SVSMetaManager.removeMeta(adminUUID);
                            return;
                        }
                        sign.addInputOptionAnswer(optionName, meta.getValue(2).asString(), meta.getValue(3).asString());
                    } else if (optionId == 2) {
                        sign.removeInputOptionAnswer(optionName, meta.getValue(2).asString());
                    }
                }
                saveRemoveExit = true;
                plugin.send(recipient, Message.OPTION_SET);
                break;

            case PERMISSION:
                if (sign == null) return;

                if (meta.getValue().asObject() == null) {
                    sign.setPermissions(new ArrayList<String>());
                    plugin.send(recipient, Message.PERMISSIONS_REMOVED);
                } else {
                    if (!meta.getValue().asStringList().isEmpty()) { // Allows just the message to be set
                        sign.setPermissions(meta.getValue().asStringList());
                    }
                    if (meta.hasValue(1)) {
                        sign.setPermissionMessage(meta.getValue(1).asString());
                    }
                    plugin.send(recipient, Message.PERMISSION_SET);
                }

                saveRemoveExit = true;
                break;

            case PRICE:
                if (sign == null) return;

                sign.setPrice(meta.getValue().asDouble());
                plugin.send(recipient, Message.PRICE_SET);
                saveRemoveExit = true;
                break;

            case PRICE_ITEM:
                if (sign == null) return;

                if (meta.getValue().asObject() == null) {
                    sign.clearPriceItems();
                    plugin.send(recipient, Message.PRICE_ITEM_REMOVED);
                } else {
                    sign.addPriceItem(meta.getValue().asItemStack());
                    plugin.send(recipient, Message.PRICE_ITEM_SUCCESS);
                }

                saveRemoveExit = true;
                break;

            case PRICE_ITEM_CRITERIA:
                if (sign == null) return;

                sign.setPIC(new ItemSearchCriteria(meta.getValue().asBoolean(), meta.getValue(2).asBoolean(),
                        meta.getValue(1).asBoolean(), meta.getValue(3).asBoolean()));

                plugin.send(recipient, Message.PRICE_ITEM_CRITERIA_SET);
                saveRemoveExit = true;
                break;

            case REMOVE:
                if (sign == null) return;

                int lineNumber = meta.getValue().asInt();
                if (lineNumber < 0) {
                    plugin.serverSignsManager.remove(sign);

                    plugin.send(recipient, Message.COMMANDS_REMOVED);
                } else if (lineNumber > sign.getCommands().size() || lineNumber < 1) {
                    plugin.send(recipient, Message.LINE_NOT_FOUND);
                    return;
                } else {
                    sign.removeCommand(lineNumber - 1);
                    plugin.serverSignsManager.save(sign);
                    plugin.send(recipient, Message.COMMAND_REMOVED);
                }

                SVSMetaManager.removeMeta(adminUUID);
                break;

            case SELECT:
                if (sign == null) return;

                SVSMetaManager.removeMeta(adminUUID);
                SVSMetaManager.setSpecialMeta(adminUUID, new SVSMeta(SVSMetaKey.SELECT, new SVSMetaValue(clicked)));
                plugin.send(recipient, Message.SIGN_SELECTED);
                break;

            case SET_COOLDOWN:
                if (sign == null) return;

                sign.setCooldown(meta.getValue().asLong());
                plugin.send(recipient, Message.COOLDOWN_SET);
                saveRemoveExit = true;
                break;

            case SET_GLOBAL_COOLDOWN:
                if (sign == null) return;

                sign.setGlobalCooldown(meta.getValue().asLong());
                plugin.send(recipient, Message.COOLDOWN_SET);
                saveRemoveExit = true;
                break;

            case SILENT:
                if (sign == null) return;

                sign.setDisplayInternalMessages(meta.getValue().asBoolean());
                plugin.send(recipient, Message.SILENT_SUCCESS);
                saveRemoveExit = true;
                break;

            case TIME_LIMIT:
                if (sign == null) return;

                // -1 keep current value | 0 remove current value | >0 set as current value
                if (meta.getValue().asLong() >= 0) {
                    sign.setTimeLimitMinimum(meta.getValue().asLong());
                }
                if (meta.getValue(1).asLong() >= 0) {
                    sign.setTimeLimitMaximum(meta.getValue(1).asLong());
                }

                plugin.send(recipient, Message.TIMELIMIT_SUCCESS);
                saveRemoveExit = true;
                break;

            case USES:
                if (sign == null) return;

                sign.setUseLimit(meta.getValue().asInt());
                plugin.send(recipient, Message.USES_SUCCESS);
                saveRemoveExit = true;
                break;

            case XP:
                if (sign == null) return;

                int value = meta.getValue().asInt();
                if (value == 0) {
                    plugin.send(recipient, Message.XP_COST_REMOVED);
                } else {
                    plugin.send(recipient, Message.XP_SET);
                }

                sign.setXP(value);
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
