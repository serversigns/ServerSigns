package de.czymm.serversigns.commands;

import de.czymm.serversigns.ServerSignsPlugin;
import de.czymm.serversigns.commands.core.SubCommand;
import de.czymm.serversigns.meta.SVSMetaKey;
import de.czymm.serversigns.meta.SVSMetaValue;
import de.czymm.serversigns.signs.ClickType;
import de.czymm.serversigns.translations.Message;

public class SubCommandDefaultExecutor extends SubCommand {
    public SubCommandDefaultExecutor(ServerSignsPlugin plugin) {
        super(
                plugin,
                "default_executor",
                "defaultexecutor <left|right|none>",
                "Set which click-type executor should be defaulted to when a player clicks a ServerSign",
                "defaultexecutor", "defexec", "defex"
        );
    }

    @Override
    public void execute(boolean verbose) {
        if (!argSet(0)) {
            if (verbose) sendUsage();
            return;
        }

        String str = argStr(0);
        ClickType type;
        try {
            type = ClickType.valueOf(str.toUpperCase());
        } catch (IllegalArgumentException ex) {
            if (verbose) sendUsage();
            return;
        }

        applyMeta(SVSMetaKey.DEFAULT_CLICKTYPE, new SVSMetaValue(type));
        if (verbose) {
            msg(Message.CLICK_APPLY);
        }
    }
}
