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
import de.czymm.serversigns.itemdata.ItemSearchCriteria;
import de.czymm.serversigns.parsing.command.ServerSignCommand;
import de.czymm.serversigns.persist.PersistenceEntry;
import de.czymm.serversigns.persist.mapping.*;
import de.czymm.serversigns.utils.BlockUtils;
import de.czymm.serversigns.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Attachable;
import org.bukkit.material.Door;
import org.bukkit.material.MaterialData;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ServerSign implements Cloneable, Serializable {
    @PersistenceEntry
    private List<String> permissions = new ArrayList<>();
    @PersistenceEntry
    private String permissionMessage = "";

    @PersistenceEntry
    private String cancelPermission = "";
    @PersistenceEntry
    private String cancelPermissionMessage = "";

    @PersistenceEntry
    private String world;
    @PersistenceEntry(configPath = "X")
    private int x;
    @PersistenceEntry(configPath = "Y")
    private int y;
    @PersistenceEntry(configPath = "Z")
    private int z;

    @PersistenceEntry
    private double price = 0.0;
    @PersistenceEntry(configPath = "exp")
    private int xp = 0;

    @PersistenceEntry
    private long cooldown = 0L;
    @PersistenceEntry
    private long globalCooldown = 0L;
    @PersistenceEntry
    private long lastGlobalUse = 0L;
    @PersistenceEntry(configMapper = StringLongHashMapper.class)
    private HashMap<String, Long> lastUse = new HashMap<>();

    @PersistenceEntry
    private boolean confirmation = false;
    @PersistenceEntry
    private String confirmationMessage = "";

    @PersistenceEntry(configMapper = CancelEnumMapper.class, configPath = "cancel_mode")
    private CancelMode cancel = CancelMode.ALWAYS;

    @PersistenceEntry(configMapper = ServerSignCommandListMapper.class)
    private ArrayList<ServerSignCommand> commands = new ArrayList<>();

    @PersistenceEntry
    private ArrayList<String> grantPermissions = new ArrayList<>();

    @PersistenceEntry(configMapper = ItemStackListMapper.class)
    private ArrayList<ItemStack> priceItems = new ArrayList<>();
    @PersistenceEntry(configMapper = ItemStackListMapper.class)
    private ArrayList<ItemStack> heldItems = new ArrayList<>();

    @PersistenceEntry(configMapper = ItemSearchCriteriaMapper.class, configPath = "pi_criteria")
    private ItemSearchCriteria pic_options = new ItemSearchCriteria(false, false, false, false);
    @PersistenceEntry(configMapper = ItemSearchCriteriaMapper.class, configPath = "hi_criteria")
    private ItemSearchCriteria hic_options = new ItemSearchCriteria(false, false, false, false);

    @PersistenceEntry
    private int loops = -1; // -1 = no loops, 0 = infinite loop, >0 = number of loops
    @PersistenceEntry(configPath = "loop_delay")
    private int loopDelayInSecs = 1;
    private int currentLoop = 0;

    @PersistenceEntry(configPath = "uses_limit")
    private int useLimit = 0; // 0 = no limit, >0 = limit
    @PersistenceEntry(configPath = "uses_tally")
    private int useTally = 0; // Tracks number of uses since useLimit was last set

    @PersistenceEntry(configMapper = LocationSetMapper.class)
    private Set<Location> protectedBlocks = new HashSet<>();

    @PersistenceEntry
    private boolean displayInternalMessages = true;

    @PersistenceEntry
    private long timeLimitMaximum = 0;

    @PersistenceEntry
    private long timeLimitMinimum = 0;

    @PersistenceEntry(configMapper = PlayerInputOptionsMapper.class)
    private Set<PlayerInputOptions> playerInputOptions = new HashSet<>();

    public ServerSign() {
    }

    public ServerSign(Location location, ServerSignCommand command) {
        setLocation(location);
        this.commands.add(command);
    }

    // Location information

    public void setLocation(Location location) {
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.world = location.getWorld().getName();

        // Update protected blocks
        updateProtectedBlocks();
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    public String getLocationString() {
        return world + ", " + x + ", " + y + ", " + z;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    // Commands

    public ArrayList<ServerSignCommand> getCommands() {
        return commands;
    }

    public void setCommands(ArrayList<ServerSignCommand> commands) {
        this.commands = commands;
    }

    public void addCommand(ServerSignCommand command) {
        this.commands.add(command);
    }

    public void removeCommand(int index) throws IndexOutOfBoundsException {
        this.commands.remove(index);
    }

    public void editCommand(int index, ServerSignCommand command) {
        this.commands.set(index, command);
    }

    // Grant permissions

    public void addGrantPermissions(String permission) {
        this.grantPermissions.add(permission);
    }

    public void removeGrantPermissions() {
        this.grantPermissions.clear();
    }

    public ArrayList<String> getGrantPermissions() {
        return grantPermissions;
    }

    public void setGrantPermissions(ArrayList<String> grantPermissions) {
        this.grantPermissions = grantPermissions;
    }

    // Permission

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public String getPermissionMessage() {
        return permissionMessage;
    }

    public void setPermissionMessage(String message) {
        permissionMessage = message;
    }

    // Cancel Permission

    public String getCancelPermission() {
        return cancelPermission;
    }

    public void setCancelPermission(String permission) {
        this.cancelPermission = permission;
    }

    public String getCancelPermissionMessage() {
        return cancelPermissionMessage;
    }

    public void setCancelPermissionMessage(String message) {
        cancelPermissionMessage = message;
    }

    // Price

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    // Confirmation

    public boolean isConfirmation() {
        return confirmation;
    }

    public void setConfirmation(boolean confirmation) {
        this.confirmation = confirmation;
    }

    public void setConfirmationMessage(String message) {
        this.confirmationMessage = message;
    }

    public String getConfirmationMessage() {
        return this.confirmationMessage;
    }

    // Global cooldown

    public long getGlobalCooldown() {
        return globalCooldown;
    }

    public void setGlobalCooldown(long globalcooldown) {
        this.globalCooldown = globalcooldown;
    }

    public long getLastGlobalUse() {
        return lastGlobalUse;
    }

    public void setLastGlobalUse(long lastGlobalUse) {
        if (globalCooldown > 0) {
            this.lastGlobalUse = lastGlobalUse;
        }
    }

    // Cooldown

    public long getCooldown() {
        return cooldown;
    }

    public void setCooldown(long cooldown) {
        this.cooldown = cooldown;
    }

    public HashMap<String, Long> getLastUse() {
        return lastUse;
    }

    public void setLastUse(HashMap<String, Long> lastUse) {
        this.lastUse = lastUse;
    }

    public void addLastUse(UUID player) {
        if (this.cooldown > 0) {
            this.lastUse.put(player.toString(), System.currentTimeMillis());
        }
    }

    public void removeLastUse(UUID player) {
        this.lastUse.remove(player.toString());
    }

    public long getLastUse(UUID player) {
        if (this.lastUse.containsKey(player.toString())) {
            return this.lastUse.get(player.toString());
        }

        return 0;
    }

    // Reset cooldowns

    public void resetCooldowns() {
        lastGlobalUse = 0L;
        lastUse.clear();
    }

    // Price items

    public void addPriceItem(ItemStack item) {
        this.priceItems.add(item);
    }

    public ArrayList<ItemStack> getPriceItems() {
        return this.priceItems;
    }

    public ArrayList<String> getPriceItemStrings() {
        ArrayList<String> list = new ArrayList<>(this.priceItems.size());
        for (ItemStack stack : this.priceItems)
            list.add(ItemUtils.getStringFromItemStack(stack));

        return list;
    }

    public void setPriceItem(ArrayList<ItemStack> array) {
        this.priceItems = array;
    }

    public void clearPriceItems() {
        this.priceItems.clear();
    }

    // Price Item criteria

    public void setPIC(ItemSearchCriteria options) {
        pic_options = options;
    }

    public ItemSearchCriteria getPIC() {
        return pic_options;
    }

    // XP

    public void setXP(Integer cost) {
        this.xp = cost;
    }

    public int getXP() {
        return this.xp;
    }

    // Cancel event

    public void setCancelMode(CancelMode mode) {
        this.cancel = mode;
    }

    public CancelMode getCancelMode() {
        return this.cancel;
    }

    // Loops

    public void setLoops(int numberOfLoops) {
        this.loops = numberOfLoops;
    }

    public int getLoops() {
        return this.loops;
    }

    public void setLoopDelay(int delayInSecs) {
        this.loopDelayInSecs = delayInSecs;
    }

    public int getLoopDelayInSecs() {
        return loopDelayInSecs;
    }

    public int getCurrentLoop() {
        return currentLoop;
    }

    public void setCurrentLoop(int loop) {
        currentLoop = loop;
    }

    // Held items

    public ArrayList<ItemStack> getHeldItems() {
        return heldItems;
    }

    public void addHeldItem(ItemStack item) {
        this.heldItems.add(item);
    }

    public void setHeldItems(ArrayList<ItemStack> items) {
        this.heldItems = items;
    }

    public void clearHeldItems() {
        this.heldItems.clear();
    }

    public ArrayList<String> getHeldItemStrings() {
        ArrayList<String> list = new ArrayList<>(this.heldItems.size());
        for (ItemStack stack : this.heldItems)
            list.add(ItemUtils.getStringFromItemStack(stack));

        return list;
    }

    // Held item criteria

    public void setHIC(ItemSearchCriteria options) {
        hic_options = options;
    }

    public ItemSearchCriteria getHIC() {
        return hic_options;
    }

    // Use limit / Use tally

    public int getUseLimit() {
        return useLimit;
    }

    public void setUseLimit(int newVal) {
        useLimit = newVal;
        useTally = 0;
    }

    public int getUseTally() {
        return useTally;
    }

    public void setUseTally(int newVal) {
        useTally = newVal;
    }

    public void incrementUseTally() {
        useTally++;
    }

    // Protected Blocks

    public Set<Location> getProtectedBlocks() {
        return protectedBlocks;
    }

    public void setProtectedBlocks(Set<Location> blocks) {
        protectedBlocks = blocks;
    }

    public void clearProtectedBlocks() {
        protectedBlocks.clear();
    }

    public void addProtectedBlock(final Block block) {
        protectedBlocks.add(block.getLocation());
    }

    public boolean isProtected(Location location) {
        return protectedBlocks.contains(location);
    }

    /**
     * Get attached block and add it to the list of protected blocks
     */
    public void updateProtectedBlocks() {
        protectedBlocks.clear();
        final Block block = getLocation().getBlock();
        Block attachedBlock = null;

        switch (ServerSignsPlugin.getServerVersion()) {
            case "1.7":
            case "1.8":
            case "1.9":
            case "1.10":
            case "1.11":
            case "1.12":
                old_updateProtectedBlocks(block);
                break;
            default:
                latest_updateProtectedBlocks(block);
        }
    }

    private void old_updateProtectedBlocks(final Block block) {
        final MaterialData data = block.getState().getData();

        if (data instanceof Attachable) {
            final Attachable attachable = (Attachable) data;
            addProtectedBlock(block.getRelative(attachable.getAttachedFace()));
        } else if (data instanceof Door) {
            if (((Door)data).isTopHalf()) {
                addProtectedBlock(block.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN));
            } else {
                addProtectedBlock(block.getRelative(BlockFace.DOWN));
            }
        }
    }

    private void latest_updateProtectedBlocks(final Block block) {
        final BlockState state = block.getState();

        try {
            final Method getBlockData = state.getClass().getMethod("getBlockData");
            final Object data = getBlockData.invoke(state);
            final Class directional = Class.forName("org.bukkit.block.data.Directional");
            final Class door = Class.forName("org.bukkit.block.data.type.Door");

            if (door.isInstance(data)) {
                if (BlockUtils.latest_isTopHalf(state)) {
                    addProtectedBlock(block.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN));
                } else {
                    addProtectedBlock(block.getRelative(BlockFace.DOWN));
                }
            } else if (directional.isInstance(data)) {
                final BlockFace face = (BlockFace)data.getClass().getMethod("getFacing").invoke(data);
                addProtectedBlock(block.getRelative(face.getOppositeFace()));
            } else {
                addProtectedBlock(block.getRelative(BlockFace.DOWN));
            }
        } catch(InvocationTargetException | IllegalAccessException | NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Display internal messages

    public boolean shouldDisplayInternalMessages() {
        return displayInternalMessages;
    }

    public void setDisplayInternalMessages(boolean val) {
        displayInternalMessages = val;
    }

    // Time limits

    public long getTimeLimitMaximum() {
        return timeLimitMaximum;
    }

    public long getTimeLimitMinimum() {
        return timeLimitMinimum;
    }

    public void setTimeLimit(long maximum, long minimum) {
        setTimeLimitMinimum(minimum);
        setTimeLimitMaximum(maximum);
    }

    public void setTimeLimitMaximum(long value) {
        timeLimitMaximum = value;
    }

    public void setTimeLimitMinimum(long value) {
        timeLimitMinimum = value;
    }

    // Player input options

    public void setInputOptionQuestion(String id, String question) {
        Iterator<PlayerInputOptions> iterator = playerInputOptions.iterator();
        while (iterator.hasNext()) {
            PlayerInputOptions options = iterator.next();
            if (options.getName().equalsIgnoreCase(id)) {
                options.setQuestion(question);
                return;
            }
        }

        PlayerInputOptions options = new PlayerInputOptions(id);
        options.setQuestion(question);
        playerInputOptions.add(options);
    }

    public void addInputOptionAnswer(String id, String label, String description) {
        Iterator<PlayerInputOptions> iterator = playerInputOptions.iterator();
        while (iterator.hasNext()) {
            PlayerInputOptions options = iterator.next();
            if (options.getName().equalsIgnoreCase(id)) {
                options.addAnswer(label, description);
                return;
            }
        }
    }

    public void removeInputOptionAnswer(String id, String label) {
        Iterator<PlayerInputOptions> iterator = playerInputOptions.iterator();
        while (iterator.hasNext()) {
            PlayerInputOptions options = iterator.next();
            if (options.getName().equalsIgnoreCase(id)) {
                iterator.remove();
                return;
            }
        }
    }

    public boolean containsInputOption(String id) {
        for (PlayerInputOptions option : playerInputOptions) {
            if (option.getName().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    public PlayerInputOptions getInputOption(String id) {
        for (PlayerInputOptions option : playerInputOptions) {
            if (option.getName().equalsIgnoreCase(id)) {
                return option;
            }
        }
        return null;
    }

    public Set<PlayerInputOptions> getInputOptions() {
        return playerInputOptions;
    }

    // Interface

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return "ServerSign@" + getLocationString();
    }
}
