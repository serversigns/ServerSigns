package de.czymm.serversigns.signs;

import de.czymm.serversigns.itemdata.ItemSearchCriteria;
import de.czymm.serversigns.parsing.command.ServerSignCommand;
import de.czymm.serversigns.persist.PersistenceEntry;
import de.czymm.serversigns.persist.mapping.*;
import de.czymm.serversigns.utils.ItemUtils;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.*;

public class ServerSignExecData implements Cloneable, Serializable {

    public ServerSignExecData() {
    }

    public ServerSignExecData(ServerSignCommand command) {
        this.commands.add(command);
    }

    @PersistenceEntry
    private List<String> permissions = new ArrayList<>();
    @PersistenceEntry
    private String permissionMessage = "";

    @PersistenceEntry
    private String cancelPermission = "";
    @PersistenceEntry
    private String cancelPermissionMessage = "";

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

    @PersistenceEntry(configMapper = EnumMapper.class, configPath = "cancel_mode")
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

    @PersistenceEntry(configPath = "uses_limit")
    private int useLimit = 0; // 0 = no limit, >0 = limit
    @PersistenceEntry(configPath = "uses_tally")
    private int useTally = 0; // Tracks number of uses since useLimit was last set

    @PersistenceEntry
    private long timeLimitMaximum = 0;

    @PersistenceEntry
    private long timeLimitMinimum = 0;

    @PersistenceEntry(configMapper = PlayerInputOptionsMapper.class)
    private Set<PlayerInputOptions> playerInputOptions = new HashSet<>();

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
}
