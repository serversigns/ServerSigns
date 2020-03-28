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
package de.czymm.serversigns.parsing.operators;

import de.czymm.serversigns.ServerSignsPlugin;
import de.czymm.serversigns.signs.ServerSign;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScoreboardOperator extends ConditionalOperator {
    private Pattern scoreboardPattern = Pattern.compile("(hasTeam)=(.+)", Pattern.CASE_INSENSITIVE);

    public ScoreboardOperator() {
        super("scoreboard", true);
    }

    @Override
    public ParameterValidityResponse checkParameterValidity(String params) {
        final Matcher matcher = scoreboardPattern.matcher(params);
        return new ParameterValidityResponse(matcher.find(), "Parameter must be in the format hasTeam=<match>[|<match>...]");
    }

    @Override
    public boolean meetsConditions(Player executor, ServerSign executingSign, ServerSignsPlugin plugin) {
        if (executor == null) {
            return true; // Console
        }

        final Matcher matcher = scoreboardPattern.matcher(params);

        if (params == null || !matcher.find()) {
            return false;
        }

        final List<String> values = Arrays.asList(matcher.group(2).split("\\|"));

        return hasTeam(executor, values);
    }

    /**
     * Return if the player has join one of specified teams
     *
     * @param player Player to check
     * @param teams  Teams to verify
     */
    private boolean hasTeam(final Player player, final List<String> teams) {
        final Team playerTeam = getPlayerTeam(player);
        return playerTeam != null && teams.contains(playerTeam.getName());
    }

    /**
     * Get the team of a player
     *
     * @param player Player to check
     */
    private Team getPlayerTeam(Player player) {
        try {
            return player.getScoreboard().getEntryTeam(player.getName());
        } catch (NoSuchMethodError e) {
            return player.getScoreboard().getPlayerTeam(player);
        }
    }
}
