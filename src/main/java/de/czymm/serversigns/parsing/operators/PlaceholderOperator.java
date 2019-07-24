package de.czymm.serversigns.parsing.operators;

import org.bukkit.entity.Player;

import de.czymm.serversigns.ServerSignsPlugin;
import de.czymm.serversigns.signs.ServerSign;
import me.clip.placeholderapi.PlaceholderAPI;

public class PlaceholderOperator extends ConditionalOperator{

	public PlaceholderOperator() {
        super("placeholder", true);
    }

	@Override
    public ParameterValidityResponse checkParameterValidity(String params) {
        return new ParameterValidityResponse(true); // Params are irrelevant
    }

    @Override
    public boolean meetsConditions(Player executor, ServerSign executingSign, ServerSignsPlugin plugin) {
    	if (params == null) {
            return false;
        }
        if (executor == null) {
            return true; // Console
        }
        
        String[] paramSplit = params.split("=");
        
        if(paramSplit.length < 2) {
        	return false;
        }

        return PlaceholderAPI.setPlaceholders(executor, paramSplit[0]).equals(paramSplit[1]);
    }
	
}
