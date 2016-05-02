package de.czymm.serversigns.signs;

import org.bukkit.event.block.Action;

public enum ClickType {
    LEFT,
    RIGHT,
    BOTH;

    public static ClickType fromAction(Action action) {
        switch (action) {
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                return ClickType.RIGHT;

            case LEFT_CLICK_AIR:
            case LEFT_CLICK_BLOCK:
                return ClickType.LEFT;

            default:
                return ClickType.RIGHT;
        }
    }
}
