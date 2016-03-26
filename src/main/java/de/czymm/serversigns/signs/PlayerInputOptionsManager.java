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
import de.czymm.serversigns.translations.Message;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;
import java.util.logging.Level;

public class PlayerInputOptionsManager implements Listener {

    private Map<UUID, List<String>> pendingOptionDisplays = new HashMap<>();
    private Map<UUID, Data> pendingPlayerData = new HashMap<>();

    private Map<UUID, Map<String, String>> completedAnswers = new HashMap<>(); // <option id, answer>

    private ServerSignsPlugin plugin;

    public PlayerInputOptionsManager(ServerSignsPlugin plugin) {
        this.plugin = plugin;
    }

    public void suspend(Player player, List<String> optionDisplayNames, ServerSign sign) {
        if (optionDisplayNames.size() < 1) return;

        askQuestion(player, sign.getInputOption(optionDisplayNames.get(0)));
        pendingOptionDisplays.put(player.getUniqueId(), optionDisplayNames);
        pendingPlayerData.put(player.getUniqueId(), new Data(sign, optionDisplayNames));
    }

    public boolean isSuspended(Player player) {
        return pendingOptionDisplays.containsKey(player.getUniqueId());
    }

    public boolean hasCompletedAnswers(Player player) {
        return completedAnswers.containsKey(player.getUniqueId());
    }

    public Map<String, String> getCompletedAnswers(Player player, boolean removeOnReturn) {
        Map<String, String> answers = completedAnswers.get(player.getUniqueId());
        if (removeOnReturn) {
            completedAnswers.remove(player.getUniqueId());
        }
        return answers;
    }

    public void processAnswer(Player player, String answer) {
        if (!pendingPlayerData.containsKey(player.getUniqueId())) return;

        // Check if this answer is valid
        List<String> pendingToDisplay = pendingOptionDisplays.get(player.getUniqueId());
        Data playerData = pendingPlayerData.get(player.getUniqueId());
        PlayerInputOptions options = playerData.sign.getInputOption(pendingToDisplay.get(0));

        if (!options.isValidAnswerLabel(answer)) {
            plugin.send(player, Message.OPTION_INVALID_ANSWER);
            askQuestion(player, options);
            return;
        }

        playerData.answers.add(answer);
        pendingToDisplay.remove(0);
        plugin.send(player, "&7&oOK!"); // Useful for players to know their answer is submitted correctly

        if (pendingToDisplay.size() > 0) {
            askQuestion(player, playerData.sign.getInputOption(pendingToDisplay.get(0)));
        } else {
            release(player, true);
        }
    }

    private void askQuestion(Player player, PlayerInputOptions inputOptions) {
        if (inputOptions == null) {
            ServerSignsPlugin.log("Player " + player.getName() + " has encountered an incorrectly labelled \"/svs option\" question! " +
                    "The player's current location is: " + player.getLocation().toString(), Level.SEVERE);
            plugin.send(player, "This ServerSign has been setup incorrectly, please alert an Administrator!");
            release(player, false);
            return;
        }

        plugin.send(player, inputOptions.getQuestion());
        for (int k = 0; k < inputOptions.getAnswersLength(); k++) {
            plugin.send(player, inputOptions.getAnswerLabel(k) + plugin.msgHandler.get(Message.OPTION_LABEL_DESC_SEPARATOR) + inputOptions.getAnswerDescription(k));
        }
    }

    public void release(Player player, boolean continueProcessing) {
        pendingOptionDisplays.remove(player.getUniqueId());

        if (continueProcessing) {
            Data data = pendingPlayerData.get(player.getUniqueId());
            if (data != null) {
                Map<String, String> map = new HashMap<>();
                for (int k = 0; k < data.originalQuestionIds.size(); k++) {
                    map.put(data.originalQuestionIds.get(k), data.answers.get(k));
                }
                completedAnswers.put(player.getUniqueId(), map);
                plugin.serverSignExecutor.executeSignFull(player, data.sign, null);
                // Are there more questions pending?
                if (pendingOptionDisplays.containsKey(player.getUniqueId())) {
                    Data newData = pendingPlayerData.get(player.getUniqueId());
                    data.originalQuestionIds.addAll(newData.originalQuestionIds);
                    pendingPlayerData.put(player.getUniqueId(), data);
                } else {
                    pendingPlayerData.remove(player.getUniqueId());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String firstWord = event.getMessage().contains(" ") ? event.getMessage().split(" ")[0] : event.getMessage();

        if (isSuspended(player)) {
            if (firstWord.equalsIgnoreCase("exit")) {
                plugin.send(player, "Exiting...");
                release(player, false);
            } else {
                processAnswer(player, firstWord);
            }
            event.setCancelled(true);
        }
    }

    private class Data {
        public Data(ServerSign sign, List<String> originalQuestions) {
            this.sign = sign;
            this.originalQuestionIds = new ArrayList<>();
            this.originalQuestionIds.addAll(originalQuestions);
        }

        public ServerSign sign;
        public List<String> originalQuestionIds;
        public List<String> answers = new ArrayList<>();
    }
}
