package de.czymm.serversigns.parsing.operators;

import org.bukkit.entity.Player;

import de.czymm.serversigns.ServerSignsPlugin;
import de.czymm.serversigns.signs.ServerSign;
import me.clip.placeholderapi.PlaceholderAPI;

public class PlaceholderOperator extends ConditionalOperator {

    public PlaceholderOperator() {
        super("placeholder", true);
    }

    @Override
    public ParameterValidityResponse checkParameterValidity(String params) {
        boolean isValid = params.indexOf('=') > 0 && params.length() - 2 >= params.indexOf('=');
        return new ParameterValidityResponse(isValid, "Parameter must be in the format <placeholder>=<match>");
    }

    @Override
    public boolean meetsConditions(Player executor, ServerSign executingSign, ServerSignsPlugin plugin) {
        if (params == null || params.indexOf('=') < 1) {
            return false;
        }

        if (!plugin.hookManager.placeholderAPI.isHooked()) {
            return false;
        }

        if (executor == null) {
            return true; // Console
        }

        String[] paramSplit = params.split("=");
        String[] ors = paramSplit[1].split("\\|");

        if (ors.length > 1) {
            for (String or : ors) {
                if (PlaceholderAPI.setPlaceholders(executor, paramSplit[0]).equals(or)) {
                    return true;
                }
            }
        }

        return PlaceholderAPI.setPlaceholders(executor, paramSplit[0]).equals(paramSplit[1]);
    }

}
