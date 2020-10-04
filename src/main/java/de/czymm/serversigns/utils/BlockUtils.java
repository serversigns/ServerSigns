package de.czymm.serversigns.utils;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.material.Door;

import java.lang.reflect.InvocationTargetException;

public class BlockUtils {

    public static boolean isDoor(final Block block) {
        return (isOld()) ? old_isDoor(block.getState()) : latest_isDoor(block.getState());
    }

    public static boolean isTopHalf(final Block block) {
        return (isOld()) ? old_isTopHalf(block.getState()) : latest_isTopHalf(block.getState());
    }

    private static boolean isOld() {
        return Version.isLowerOrEqualsTo(Version.V1_12);
    }

    public static boolean old_isTopHalf(final BlockState state) {
        return old_isDoor(state) && ((Door) state.getData()).isTopHalf();
    }

    public static boolean latest_isTopHalf(final BlockState state) {
        if (latest_isDoor(state)) {
            try {
                final Object data = state.getClass().getMethod("getBlockData").invoke(state);
                final Class half = Class.forName("org.bukkit.block.data.Bisected$Half");
                final Object enumValue = Enum.valueOf(half, "TOP");

                return (data.getClass().getMethod("getHalf").invoke(data) == enumValue);
            } catch(NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean old_isDoor(final BlockState state) {
        return state.getData() instanceof Door;
    }

    public static boolean latest_isDoor(final BlockState state) {
        try {
            final Object data = state.getClass().getMethod("getBlockData").invoke(state);
            final Class door = Class.forName("org.bukkit.block.data.type.Door");

            return door.isInstance(data);
        } catch(NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }
}
