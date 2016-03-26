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

package de.czymm.serversigns.signs;

import de.czymm.serversigns.ServerSignsPlugin;
import de.czymm.serversigns.meta.SVSMeta;
import de.czymm.serversigns.meta.SVSMetaKey;
import de.czymm.serversigns.meta.SVSMetaManager;
import de.czymm.serversigns.meta.SVSMetaValue;
import de.czymm.serversigns.parsing.CommandType;
import de.czymm.serversigns.parsing.command.ConditionalServerSignCommand;
import de.czymm.serversigns.parsing.command.ReturnServerSignCommand;
import de.czymm.serversigns.parsing.command.ServerSignCommand;
import de.czymm.serversigns.taskmanager.TaskManagerTask;
import de.czymm.serversigns.taskmanager.tasks.PermissionGrantPlayerTask;
import de.czymm.serversigns.taskmanager.tasks.PermissionRemovePlayerTask;
import de.czymm.serversigns.translations.Message;
import de.czymm.serversigns.utils.InventoryUtils;
import de.czymm.serversigns.utils.ItemUtils;
import de.czymm.serversigns.utils.StringUtils;
import de.czymm.serversigns.utils.TimeUtils;
import de.czymm.serversigns.utils.TimeUtils.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;

public class ServerSignExecutor {
    private ServerSignsPlugin plugin;

    public ServerSignExecutor(ServerSignsPlugin instance) {
        plugin = instance;
    }

    public void executeSignFull(Player player, ServerSign sign, PlayerInteractEvent event) {
        try {
            // Should we cancel this event regardless of the outcome?
            if (event != null && sign.getCancelMode().equals(CancelMode.ALWAYS)) {
                event.setCancelled(true);
            }

            // Pre-execution checks
            if (!isReady(player, sign)) {
                // Event cancel mode
                if (event != null && sign.getCancelMode().equals(CancelMode.FAIL_ONLY)) {
                    event.setCancelled(true);
                }
                return;
            }

            // Loop check
            if (sign.getCurrentLoop() > 0) {
                if (sign.shouldDisplayInternalMessages()) {
                    plugin.send(player, Message.LOOP_MUST_FINISH);
                }
                return;
            }
            boolean looped = sign.getLoops() > -1;

            // Format and execute commands
            if (!looped) {
                if (plugin.hookManager.noCheatPlus.isHooked()) {
                    plugin.hookManager.noCheatPlus.getHook().exemptTemporarily(player, "CHAT_COMMANDS", 40L); // 2 seconds should cover any spam issues
                }

                List<TaskManagerTask> tasks = new ArrayList<>();

                // Pre-execution granted permissions
                List<PermissionGrantPlayerTask> grantTasks = null;
                if (sign.getGrantPermissions() != null && !sign.getGrantPermissions().isEmpty()) {
                    if ((!plugin.hookManager.vault.isHooked() || !plugin.hookManager.vault.getHook().hasPermissions()) && (plugin.config.getPermissionAddCommand().isEmpty() || plugin.config.getPermissionRemoveCommand().isEmpty())) {
                        if (sign.shouldDisplayInternalMessages()) {
                            plugin.send(player, Message.FEATURES_NOT_AVAILABLE);
                        }
                        ServerSignsPlugin.log("ServerSign at " + sign.getLocationString() + " has been activated, but cannot execute as it is attempting to grant permissions but no Vault hook or config-defined commands exist");
                        return;
                    }
                    grantTasks = grantPermissions(player.getUniqueId(), 0, sign.getGrantPermissions(), tasks);
                }

                // Commands
                createCommandTasks(sign, tasks, player, event == null ? null : event.getAction());
                if (plugin.inputOptionsManager.hasCompletedAnswers(player)) { // Clear completed answers as they're no longer needed for this execution
                    plugin.inputOptionsManager.getCompletedAnswers(player, true);
                }

                // Remove granted permissions
                if (sign.getGrantPermissions() != null && !sign.getGrantPermissions().isEmpty()) {
                    removePermissions(player.getUniqueId(), 0, grantTasks, tasks);
                }

                // Bulked so we can halt execution of the whole sign if necessary
                if (sign.getCommands().size() > 0 && tasks.isEmpty()) {
                    // No commands applicable for this interaction, halt execution
                    return;
                }
                for (TaskManagerTask task : tasks) {
                    plugin.taskManager.addTask(task);
                }
            }

            // Update usage
            sign.setLastGlobalUse(System.currentTimeMillis());
            if (sign.getCooldown() > 0) {
                sign.addLastUse(player.getUniqueId()); // Only store if we need to
            }

            // Post-execution function calls
            removePriceItems(player, sign);
            removeXP(player, sign);
            removeMoney(player, sign);

            // Event cancel mode
            if (event != null && sign.getCancelMode().equals(CancelMode.SUCCESS_ONLY)) {
                event.setCancelled(true);
            }

            // Uses limit
            sign.incrementUseTally();
            if (sign.getUseLimit() > 0) {
                if (sign.getUseTally() >= sign.getUseLimit()) {
                    ServerSignsPlugin.log("ServerSign at '" + sign.getLocationString() + "' has reached its uses limit and has expired");
                    plugin.serverSignsManager.expire(sign);
                    return;
                }
            }

            // Loops
            if (looped) {
                executeSignLooped(sign, player);
                return;
            }

            // Save
            plugin.serverSignsManager.save(sign);
        } catch (Exception ex) {
            ServerSignsPlugin.log("Exception generated during execution of a ServerSign!", Level.SEVERE, ex);
            ex.printStackTrace();
        }
    }

    public void executeSignLooped(ServerSign sign, Player executor) {
        try {
            if (sign == null || plugin.serverSignsManager.getServerSignByLocation(sign.getLocation()) == null) return;

            // Loops check
            int loops = sign.getLoops();
            int currentLoop = sign.getCurrentLoop() > 0 ? sign.getCurrentLoop() : 1;
            int loopDelay = sign.getLoopDelayInSecs();

            // Loops scheduling
            sign.setCurrentLoop(currentLoop);

            // Format and execute commands
            for (TaskManagerTask task : createCommandTasks(sign, null, executor, null)) {
                plugin.taskManager.addTask(task);
            }

            if (loops == 0) {
                // Scheduled infinity until restart
                executeLoopRunnable(sign, executor, loopDelay);
            } else if (loops > -1 && currentLoop < loops) {
                sign.setCurrentLoop(++currentLoop);
                executeLoopRunnable(sign, executor, loopDelay);
            } else {
                sign.setCurrentLoop(0); // Done
            }

            // Save
            plugin.serverSignsManager.save(sign);
        } catch (Exception ex) {
            ServerSignsPlugin.log("Exception generated during execution of a looped ServerSign!", Level.SEVERE, ex);
        }
    }

    private void executeLoopRunnable(final ServerSign sign, final Player executor, long loopDelay) {
        new BukkitRunnable() {
            public void run() {
                executeSignLooped(sign,
                        executor == null
                                ? null
                                : executor.isOnline()
                                ? executor
                                : null); // We don't want looped commands to be queued if the player logs off, as a precaution
            }
        }.runTaskLater(plugin, loopDelay * 20);
    }

    private List<TaskManagerTask> createCommandTasks(ServerSign sign, List<TaskManagerTask> existingList, Player executor, Action eventAction) {
        List<TaskManagerTask> tasks = existingList == null ? new ArrayList<TaskManagerTask>() : existingList;

        ProcessingData processingData = new ProcessingData();
        for (ServerSignCommand command : sign.getCommands()) {
            // Event Action check
            if (eventAction != null) {
                if (command.getInteractValue() != 0 &&
                        (command.getInteractValue() == 1 && !eventAction.equals(Action.LEFT_CLICK_BLOCK)) ||
                        (command.getInteractValue() == 2 && !eventAction.equals(Action.RIGHT_CLICK_BLOCK))) {
                    continue;
                }
            }

            // Conditional Command Checks
            processingData = processConditionalCommand(sign, executor, command, processingData);
            if (processingData.lastResult == 1) {
                continue;
            } else if (processingData.lastResult == 2) {
                break;
            }

            tasks.addAll(command.getTasks(executor, plugin, getInjectedCommandReplacements(sign, true)));
        }

        return tasks;
    }

    private class ProcessingData {
        public int ifLevel = 0,
                endifLevel = 0,
                skipUntilLevel = -1;
        public int lastResult = -1;
    }

    // 0 = flow, 1 = continue, 2 = break
    private ProcessingData processConditionalCommand(ServerSign sign, Player executor, ServerSignCommand command, ProcessingData data) {
        // Skip commands within false blocks
        if (data.skipUntilLevel > 0) {
            // Check if this is an endif statement
            if (command instanceof ConditionalServerSignCommand && ((ConditionalServerSignCommand) command).isEndifStatement()) {
                data.endifLevel++;
                if (data.skipUntilLevel == data.endifLevel) {
                    data.skipUntilLevel = -1;
                }
                data.lastResult = 1;
                return data;
            } else {
                data.lastResult = 1;
                return data;
            }
        }

        // Check for return command
        if (command instanceof ReturnServerSignCommand) {
            data.lastResult = 2;
            return data;
        }

        // Check for conditional commands
        if (command instanceof ConditionalServerSignCommand) {
            ConditionalServerSignCommand condCommand = (ConditionalServerSignCommand) command;
            if (condCommand.isIfStatement()) {
                data.ifLevel++;
                if (!condCommand.meetsAllConditions(executor, sign, plugin)) {
                    // Skip all commands within this statement
                    data.skipUntilLevel = data.ifLevel;
                    data.lastResult = 1;
                    return data;
                }
            } else if (condCommand.isEndifStatement()) {
                data.endifLevel++;
            }
        }

        data.lastResult = 0;
        return data;
    }

    // Execution-time variables

    /*
    <usesLeft> = remaining uses
    <usesTally> = tally of current uses
    <usesLimit> = the use limit
    <loopCount> = current loop number
    <loopsLeft> = number of loops remaining
    <signLoc> = sign location in world,x,y,z format
    */

    private Map<String, String> getInjectedCommandReplacements(ServerSign sign, boolean looped) {
        Map<String, String> replacementMap = new HashMap<>();

        replacementMap.put("<usesTally>", sign.getUseTally() + "");
        replacementMap.put("<signLoc>", sign.getWorld() + "," + sign.getX() + "," + sign.getY() + "," + sign.getZ());
        if (sign.getUseLimit() > 0) {
            replacementMap.put("<usesLeft>", sign.getUseLimit() - sign.getUseTally() + "");
            replacementMap.put("<usesLimit>", sign.getUseLimit() + "");
        }
        if (looped) {
            replacementMap.put("<loopCount>", sign.getCurrentLoop() + "");
            replacementMap.put("<loopsLeft>", sign.getLoops() - sign.getCurrentLoop() + "");
        }

        return replacementMap;
    }

    //-----------------------------------------------------
    // PRE-EXECUTION CHECKS
    //-----------------------------------------------------

    private boolean isReady(Player player, ServerSign sign) {
        // Cancel any previous confirmations pending if this is a new sign
        if (SVSMetaManager.hasInclusiveMeta(player, SVSMetaKey.YES) &&
                !SVSMetaManager.getMeta(player).getValue().asServerSign().equals(sign)) {
            SVSMetaManager.removeMeta(player);
        }

        if (sign.getTimeLimitMinimum() > 0 && System.currentTimeMillis() < sign.getTimeLimitMinimum()) {
            plugin.send(player, Message.TIMELIMIT_WAITING, "<string>", TimeUtils.getTimeSpan(sign.getTimeLimitMinimum() - System.currentTimeMillis(), TimeUnit.SECONDS, TimeUnit.YEARS, false, false));
            return false;
        }
        if (sign.getTimeLimitMaximum() > 0 && System.currentTimeMillis() >= sign.getTimeLimitMaximum()) {
            plugin.send(player, Message.TIMELIMIT_EXPIRED, "<string>", TimeUtils.getTimeSpan(System.currentTimeMillis() - sign.getTimeLimitMaximum(), TimeUnit.SECONDS, TimeUnit.YEARS, false, false));
            return false;
        }

        if (!sign.getCancelPermission().isEmpty()) {
            if (player.hasPermission(sign.getCancelPermission())) {
                if (sign.shouldDisplayInternalMessages()) {
                    if (sign.getCancelPermissionMessage().isEmpty()) {
                        plugin.send(player, Message.CANCELLED_DUE_TO_PERMISSION);
                    } else {
                        plugin.send(player, StringUtils.colour(sign.getCancelPermissionMessage()));
                    }
                }
                return false;
            }
        }

        if (!hasPermission(player, sign)) {
            if (sign.shouldDisplayInternalMessages()) {
                if (sign.getPermissionMessage().isEmpty()) {
                    plugin.send(player, Message.NOT_ENOUGH_PERMISSIONS);
                } else {
                    plugin.send(player, StringUtils.colour(sign.getPermissionMessage()));
                }
            }
            return false;
        }

        if (!canUse(player, sign)) {
            if (sign.shouldDisplayInternalMessages()) {
                plugin.send(player, Message.NOT_READY, "<cooldown>", getCooldownLeft(player, sign));
            }
            return false;
        }

        return !(needConfirmation(player, sign) || !canAffordXP(player, sign) || !canAffordCost(player, sign) || !hasAnsweredQuestions(player, sign)
                || (hasHeldRequirements(sign) && !meetsHeldItemRequirements(player, sign))
                || (hasPriceItem(sign) && !canAffordPriceItem(player, sign)));
    }

    // Questions

    private boolean hasAnsweredQuestions(Player executor, ServerSign sign) {
        List<String> displayedOptionIds = new ArrayList<>();

        ProcessingData processingData = new ProcessingData();
        for (ServerSignCommand command : sign.getCommands()) {
            // Conditional Command Checks
            processingData = processConditionalCommand(sign, executor, command, processingData);
            if (processingData.lastResult == 1) {
                continue;
            } else if (processingData.lastResult == 2) {
                break;
            }

            if (command.getType() == CommandType.DISPLAY_OPTIONS) {
                String data = command.getUnformattedCommand();
                PlayerInputOptions options = sign.getInputOption(data);
                if (options != null && options.getAnswersLength() > 0) {
                    displayedOptionIds.add(data);
                } else {
                    ServerSignsPlugin.log("An invalid '/svs option' question has been encountered at " + sign.getLocationString());
                }
            }
        }

        if (displayedOptionIds.size() > 0) {
            if (!plugin.inputOptionsManager.hasCompletedAnswers(executor)) {
                // Gather options from player before they can continue again
                if (!plugin.inputOptionsManager.isSuspended(executor)) {
                    plugin.inputOptionsManager.suspend(executor, displayedOptionIds, sign);
                }
                return false;
            } else {
                if (plugin.inputOptionsManager.getCompletedAnswers(executor, false).size() < displayedOptionIds.size()) {
                    // Questions still pending
                    Map<String, String> currentlyAnswered = plugin.inputOptionsManager.getCompletedAnswers(executor, false);
                    for (Map.Entry<String, String> entry : currentlyAnswered.entrySet()) {
                        if (displayedOptionIds.contains(entry.getKey())) {
                            displayedOptionIds.remove(entry.getKey());
                        }
                    }
                    //plugin.inputOptionsManager.release(executor, false);
                    plugin.inputOptionsManager.suspend(executor, displayedOptionIds, sign);
                    return false;
                }
            }
        }

        return true;
    }

    // Permissions

    private boolean hasPermission(Player player, ServerSign sign) {
        if (player.hasPermission("serversigns.admin") || player.hasPermission("serversigns.use.*")) {
            return true;
        }

        if (sign.getPermissions().isEmpty()) {
            return player.hasPermission("serversigns.use");
        }

        for (String perm : sign.getPermissions()) {
            if (!player.hasPermission("serversigns.use." + perm)) {
                return false;
            }
        }

        return true;
    }

    // Cooldowns

    private boolean canUse(Player player, ServerSign sign) {
        if (sign.getGlobalCooldown() == 0 || System.currentTimeMillis() - sign.getLastGlobalUse() >= sign.getGlobalCooldown() * 1000) {
            if (sign.getCooldown() == 0 || System.currentTimeMillis() - sign.getLastUse(player.getUniqueId()) >= sign.getCooldown() * 1000) {
                return true;
            }
        }
        return false;
    }

    private String getCooldownLeft(Player player, ServerSign sign) {
        long globalLeft = sign.getGlobalCooldown() * 1000 - (System.currentTimeMillis() - sign.getLastGlobalUse());
        long normalLeft = sign.getCooldown() * 1000 - (System.currentTimeMillis() - sign.getLastUse(player.getUniqueId()));
        long applicable = globalLeft > normalLeft ? globalLeft : normalLeft;
        String toRet = TimeUtils.getTimeSpan(applicable, TimeUnit.SECONDS, TimeUnit.YEARS, true, false);
        return toRet.isEmpty() ? "<1s" : toRet;
    }

    // Confirmation

    @SuppressWarnings("unchecked")
    private boolean needConfirmation(Player player, ServerSign sign) {
        if (sign.isConfirmation()) {
            if (SVSMetaManager.hasInclusiveMeta(player, SVSMetaKey.YES)) {
                // If they have the meta still, it must be for the same sign
                SVSMetaManager.removeMeta(player);
                return false;
            }

            if (sign.shouldDisplayInternalMessages()) {
                if (!sign.getHeldItems().isEmpty()) {
                    plugin.send(player, Message.NEED_CONFIRMATION_HELD_ITEMS);
                    for (ItemStack item : sign.getHeldItems()) {
                        plugin.send(player, ItemUtils.getDescription(item, plugin.config.getMessageColour()));
                    }
                }

                if (!sign.getPriceItems().isEmpty()) {
                    plugin.send(player, Message.NEED_CONFIRMATION_PRICE_ITEMS);
                    for (ItemStack pi : sign.getPriceItems()) {
                        plugin.send(player, ItemUtils.getDescription(pi, plugin.config.getMessageColour()));
                    }
                }

                if (sign.getXP() > 0) {
                    plugin.send(player, Message.NEED_CONFIRMATION_XP, "<integer>", sign.getXP() + "");
                }

                if (!sign.getConfirmationMessage().isEmpty()) {
                    plugin.send(player, sign.getConfirmationMessage());
                }

                if (sign.getPrice() > 0 && plugin.hookManager.vault.isHooked() && plugin.hookManager.vault.getHook().hasEconomy()) {
                    plugin.send(player, Message.NEED_CONFIRMATION_COST, "<price>", sign.getPrice() + "", "<currency>", plugin.config.getCurrencyString());
                } else {
                    plugin.send(player, Message.NEED_CONFIRMATION);
                }
            }

            SVSMetaManager.setMeta(player, new SVSMeta(SVSMetaKey.YES, new SVSMetaValue(sign)));
            return true;
        }

        return false;
    }

    // Exp

    private boolean canAffordXP(Player player, ServerSign sign) {
        if (sign.getXP() > 0) {
            if (player.getLevel() >= sign.getXP()) {
                return true;
            }

            if (sign.shouldDisplayInternalMessages()) {
                plugin.send(player, Message.NOT_ENOUGH_XP);
                plugin.send(player, Message.LEVELS_NEEDED, "<integer>", sign.getXP() - player.getLevel() + "");
            }
            return false;
        }
        return true;
    }

    // Money

    private boolean canAffordCost(Player player, ServerSign sign) {
        if (plugin.hookManager.vault.isHooked() && plugin.hookManager.vault.getHook().hasEconomy()) {
            double price = sign.getPrice();
            if (price > 0) {
                if (plugin.hookManager.vault.getHook().getEconomy().has(player, price)) {
                    return true;
                }

                if (sign.shouldDisplayInternalMessages()) {
                    plugin.send(player, Message.NOT_ENOUGH_MONEY);
                }
                return false;
            }
        }

        return true;
    }

    // Price items

    private boolean hasPriceItem(ServerSign sign) {
        return !sign.getPriceItems().isEmpty();
    }

    private boolean canAffordPriceItem(Player player, ServerSign sign) {
        ItemStack[] items = sign.getPriceItems().toArray(new ItemStack[1]);
        Collection<ItemStack> leftover = InventoryUtils.scan(player.getInventory(), sign.getPIC(), false, items);

        if (!leftover.isEmpty()) {
            if (sign.shouldDisplayInternalMessages()) {
                plugin.send(player, Message.NOT_ENOUGH_ITEMS);
                for (ItemStack required : leftover) {
                    plugin.send(player, ItemUtils.getDescription(required, plugin.config.getMessageColour()));
                }
            }
            return false;
        }

        return true;
    }

    // Held items

    private boolean hasHeldRequirements(ServerSign sign) {
        return !sign.getHeldItems().isEmpty();
    }

    private boolean meetsHeldItemRequirements(Player player, ServerSign sign) {
        ItemStack held = player.getItemInHand();
        if (held != null && !held.getType().equals(Material.AIR)) {
            for (ItemStack stack : sign.getHeldItems()) {
                if (ItemUtils.compare(stack, held, sign.getHIC()))
                    return true;
            }
        }

        if (sign.shouldDisplayInternalMessages()) {
            plugin.send(player, Message.MUST_BE_HOLDING);
            for (ItemStack required : sign.getHeldItems()) {
                plugin.send(player, ItemUtils.getDescription(required, plugin.config.getMessageColour()));
            }
        }
        return false;
    }

    //-----------------------------------------------------
    // POST-EXECUTION FUNCTIONS
    //-----------------------------------------------------

    //  Exp

    private void removeXP(Player player, ServerSign sign) {
        if (sign.getXP() > 0) {
            player.setLevel(player.getLevel() - sign.getXP());
            if (sign.shouldDisplayInternalMessages()) {
                plugin.send(player, Message.XP_REMOVED, "<levels>", sign.getXP() + "");
            }
        }
    }

    // Money

    private boolean removeMoney(Player player, ServerSign sign) {
        if (sign.getPrice() > 0) {
            if (!plugin.hookManager.vault.isHooked() || !plugin.hookManager.vault.getHook().hasEconomy()) {
                ServerSignsPlugin.log("Unable to remove money from " + player.getName() + " because no Economy plugins exist!");
                return false;
            }

            plugin.hookManager.vault.getHook().getEconomy().withdrawPlayer(player, sign.getPrice());
            if (plugin.config.getShowFundsRemovedMessage() && sign.shouldDisplayInternalMessages()) {
                String message = plugin.msgHandler.get(Message.FUNDS_WITHDRAWN);
                if (message.contains("<number>")) {
                    message = message.replaceAll("<number>", String.valueOf(sign.getPrice()));
                }

                if (message.contains("<currency>") && !plugin.config.getCurrencyString().isEmpty()) {
                    String repl = String.valueOf(plugin.config.getCurrencyString());
                    message = message.replaceAll("<currency>", Matcher.quoteReplacement(repl));
                }

                plugin.send(player, message);
            }
            if (plugin.config.getSendPaymentsToBank()) {
                String bank = plugin.config.getDepositBankName();
                if (bank.isEmpty()) return true;

                plugin.hookManager.vault.getHook().getEconomy().bankDeposit(bank, sign.getPrice());
            }
            return true;

        }
        return false;
    }

    // Price items

    private void removePriceItems(Player player, ServerSign sign) {
        if (sign.getPriceItems().isEmpty()) return;

        ItemStack[] items = sign.getPriceItems().toArray(new ItemStack[1]);
        if (!InventoryUtils.scan(player.getInventory(), sign.getPIC(), true, items).isEmpty()) {
            Bukkit.getLogger().warning("A player has managed to execute a ServerSign without paying all the price items! (Location: " + sign.getLocation().toString() + ")");
        }

        player.updateInventory();
    }

    //-----------------------------------------------------
    // UTILITY FUNCTIONS
    //-----------------------------------------------------

    private List<PermissionGrantPlayerTask> grantPermissions(UUID player, long timestamp, List<String> permissions, List<TaskManagerTask> tasks) {
        assert !permissions.isEmpty();
        List<PermissionGrantPlayerTask> list = new ArrayList<>();

        for (String perm : permissions) {
            PermissionGrantPlayerTask grantTask = new PermissionGrantPlayerTask(timestamp, perm, player, true);
            tasks.add(grantTask);
            list.add(grantTask);
        }

        return list;
    }

    private void removePermissions(UUID player, long timestamp, List<PermissionGrantPlayerTask> grantTasks, List<TaskManagerTask> tasks) {
        for (PermissionGrantPlayerTask grantTask : grantTasks) {
            tasks.add(new PermissionRemovePlayerTask(timestamp, player, grantTask, true));
        }
    }
}