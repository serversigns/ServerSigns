package de.czymm.serversigns.parsing.operators;

import de.czymm.serversigns.ServerSignsPlugin;
import de.czymm.serversigns.signs.ClickType;
import de.czymm.serversigns.signs.ServerSign;
import de.czymm.serversigns.utils.NumberUtils;
import org.bukkit.entity.Player;

public class HasBalanceOperator extends ConditionalOperator {

    public HasBalanceOperator() {
        super("hasBalance", true);
    }

    @Override
    public ParameterValidityResponse checkParameterValidity(String params) {

        char operatorChar = getOperatorChar(params);
        if (operatorChar == '¬') {
            return new ParameterValidityResponse(false, "Parameter must be in the format <operator><amount> - where <operator> is > < or =");
        }

        String amountStr = params.substring(1);

        if (!NumberUtils.isDouble(amountStr)) {
            return new ParameterValidityResponse(false, "Parameter must be in the format <operator><amount> - where <amount> is a decimal number or integer");
        }

        return new ParameterValidityResponse(true);
    }

    @Override
    public boolean meetsConditions(Player executor, ServerSign executingSign, ClickType clickType, ServerSignsPlugin plugin) {
        if (params == null) {
            return false;
        }

        if (!plugin.hookManager.vault.isHooked() || !plugin.hookManager.vault.getHook().hasEconomy()) {
            return false;
        }

        char operatorChar = getOperatorChar(params);
        int amount = NumberUtils.parseInt(params.substring(1));

        switch (operatorChar) {
            case '>':
                return plugin.hookManager.vault.getHook().getEconomy().getBalance(executor) > amount;

            case '<':
                return plugin.hookManager.vault.getHook().getEconomy().getBalance(executor) < amount;

            case '=':
                return plugin.hookManager.vault.getHook().getEconomy().getBalance(executor) == amount;

            default:
                return false;
        }
    }

    private char getOperatorChar(String params) {
        switch (params.charAt(0)) {
            case '>':
            case '<':
            case '=':
                return params.charAt(0);
            default:
                return '¬';
        }
    }
}
