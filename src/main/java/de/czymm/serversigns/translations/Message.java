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

package de.czymm.serversigns.translations;

public enum Message {


    COMMAND_SET("set_command"),
    BLOCKED_COMMAND("blocked_command"),
    PRICE_SET("set_price"),
    PERMISSION_SET("set_permission"),
    COOLDOWN_SET("set_cooldown"),
    COMMAND_REMOVED("del_command"),
    PERMISSION_REMOVED("del_permission"),
    COMMANDS_REMOVED("commands_removed"),
    COMMAND_EDITED("edit_command"),
    NO_NUMBER("no_number"),
    NOT_READY("not_ready"),
    LINE_NOT_FOUND("line_not_found"),
    NOT_ENOUGH_PERMISSIONS("not_enough_permissions"),
    NOT_ENOUGH_MONEY("not_enough_money"),
    CANNOT_DESTROY("cannot_destroy"),
    NEED_CONFIRMATION_COST("need_confirmation_cost"),
    NEED_CONFIRMATION("need_confirmation"),
    RESET_COOLDOWN("reset_cooldown"),
    ALL_COOLDOWNS_RESET("all_cooldowns_reset"),
    RELOAD_SUCCESS("reload_success"),
    INVALID_ITEMID("invalid_itemid"),
    PRICE_ITEM_SUCCESS("priceitem_success"),
    PRICE_ITEM_BIND("priceitem_bind"),
    NOT_ENOUGH_ITEMS("not_enough_items"),
    PRICE_ITEM_REMOVE("priceitem_remove"),
    PRICE_ITEM_REMOVED("priceitem_removed"),
    XP_COST_BIND("xpcost_bind"),
    XP_SET("set_xp"),
    NOT_ENOUGH_XP("not_enough_xp"),
    FUNDS_WITHDRAWN("funds_withdrawn"),
    PERMISSIONS_REQUIRED("permissions_required"),
    ITEM_CRITERIA_BOOLEAN("itemcriteria_boolean"),
    PRICE_ITEM_CRITERIA_BIND("priceitemcriteria_bind"),
    PRICE_ITEM_CRITERIA_SET("priceitemcriteria_set"),
    PERSISTENCE_ON("persistance_on"),
    PERSISTENCE_OFF("persistance_off"),
    MUST_SNEAK("must_sneak"),
    CONFIRMATION_SET("set_confirmation"),
    SET_CANCEL_MODE("set_cancel_mode"),
    INVALID_HAND_ITEM("invalid_hand_item"),
    INVALID_INDEX("invalid_index"),
    DELAY_GREATER_THAN_ZERO("delay_greater_than_zero"),
    SET_LOOPS("set_loops"),
    LOOP_MUST_FINISH("loop_must_finish"),
    NEED_CONFIRMATION_PRICE_ITEMS("need_confirmation_price_items"),
    NEED_CONFIRMATION_XP("need_confirmation_xp"),
    LEVELS_NEEDED("levels_needed"),
    HOLDING_BIND("holding_bind"),
    HOLDING_SUCCESS("holding_success"),
    HOLDING_REMOVE("holding_remove"),
    HOLDING_REMOVED("holding_removed"),
    HELD_ITEM_CRITERIA_BIND("helditemcriteria_bind"),
    HELD_ITEM_CRITERIA_SET("helditemcriteria_set"),
    NEED_CONFIRMATION_HELD_ITEMS("need_confirmation_held_items"),
    MUST_BE_HOLDING("must_be_holding"),
    PLAYER_NOT_FOUND("player_not_found"),
    COOLDOWNS_RESET("cooldowns_reset"),
    LONG_COMMAND_AGAIN("long_command_again"),
    LONG_TYPE_TO_CHAT("long_type_to_chat"),
    LONG_CANCELLED("long_cancelled"),
    USES_GREATER_ZERO("uses_greater_zero"),
    USES_SUCCESS("uses_success"),
    CANCEL_PERMISSION_SET("set_cancel_permission"),
    CANCELLED_DUE_TO_PERMISSION("cancelled_due_to_permission"),
    RELOAD_CONFIG_SUCCESS("reload_config_success"),
    RELOAD_SIGNS_SUCCESS("reload_signs_success"),
    FEATURES_NOT_AVAILABLE("features_not_available"),
    INVALID_COMMAND("invalid_command"),
    RELOAD_SIGNS_FAIL("reload_signs_fail"),
    RELOAD_CONFIG_FAIL("reload_config_fail"),
    COPY_SUCCESS("copy_success"),
    CREATE_SUCCESS("create_success"),
    BLOCK_IS_PROTECTED("block_protected"),
    LIST_PERSIST_ON("list_persist_on"),
    LIST_PERSIST_OFF("list_persist_off"),
    XP_REMOVED("xp_removed"),
    SILENT_SUCCESS("silent_success"),
    SIGN_SELECTED("sign_selected"),
    SIGN_DESELECTED("sign_deselected"),
    TIMELIMIT_INVALID("timelimit_invalid"),
    TIMELIMIT_SUCCESS("timelimit_success"),
    TIMELIMIT_EXPIRED("timelimit_expired"),
    TIMELIMIT_WAITING("timelimit_waiting"),
    OPTION_CREATE_W_QUESTION("option_create_w_question"),
    OPTION_INVALID_ANSWER("option_invalid_answer"),
    OPTION_SET("option_set"),
    OPTION_LABEL_UNIQUE("option_label_unique"),
    UNABLE_TO_EXECUTE_CMD("unable_to_execute_cmd"),
    IMPORT_FILE_NOT_FOUND("import_file_not_found"),
    IMPORT_SUCCESS("import_success"),
    IMPORT_SELECT_SIGN("import_select_sign"),
    OPTION_LABEL_DESC_SEPARATOR("option_label_desc_separator"),
    XP_COST_REMOVED("xp_cost_removed"),
    XP_COST_INVALID("xp_cost_invalid"),
    PERMISSIONS_REMOVED("permissions_removed"),
    DEFAULT_EXECUTOR_SET("default_executor_set"),
    CLICK_BIND_CMD("click_bind_command"),
    CLICK_LIST("click_list"),
    CLICK_SET_PRICE("click_set_price"),
    CLICK_BIND_PERMISSION("click_bind_permission"),
    CLICK_SET_COOLDOWN("click_set_cooldown"),
    CLICK_REMOVE_COMMAND("click_del_command"),
    CLICK_DEL_PERMISSION("click_del_permission"),
    CLICK_COPY("click_copy"),
    CLICK_PASTE("click_paste"),
    CLICK_RESET_COOLDOWN("click_reset_cooldown"),
    CLICK_REMOVE_USES("click_remove_uses"),
    CLICK_SET_USES("click_set_uses"),
    CLICK_BIND_LOOPS("click_bind_loops"),
    CLICK_BIND_CANCEL_PERMISSION("click_bind_cancel_permission"),
    CLICK_CREATE("click_create"),
    CLICK_SELECT("click_select"),
    CLICK_APPLY("click_apply"),
    CLICK_VIEW_LIST("click_view_list"),
    CLICK_INFO("click_info"),;


    private String path;

    Message(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
