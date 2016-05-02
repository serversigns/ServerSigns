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

import de.czymm.serversigns.parsing.command.ServerSignCommand;
import de.czymm.serversigns.persist.PersistenceEntry;
import de.czymm.serversigns.persist.mapping.ClickTypeServerSignExecDataHashMapper;
import de.czymm.serversigns.persist.mapping.LocationSetMapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Attachable;
import org.bukkit.material.Door;

import java.io.Serializable;
import java.util.*;

public class ServerSign implements Cloneable, Serializable {

    @PersistenceEntry
    private String world;
    @PersistenceEntry(configPath = "X")
    private int x;
    @PersistenceEntry(configPath = "Y")
    private int y;
    @PersistenceEntry(configPath = "Z")
    private int z;

    @PersistenceEntry(configMapper = LocationSetMapper.class)
    private Set<Location> protectedBlocks = new HashSet<>();

    @PersistenceEntry
    private boolean displayInternalMessages = true;

    @PersistenceEntry
    private int loops = -1; // -1 = no loops, 0 = infinite loop, >0 = number of loops
    @PersistenceEntry(configPath = "loop_delay")
    private int loopDelayInSecs = 1;
    private int currentLoop = 0;

    @PersistenceEntry(configMapper = ClickTypeServerSignExecDataHashMapper.class, configPath = "executor-data")
    private Map<ClickType, ServerSignExecData> execDataMap = new HashMap<>();

    public ServerSign() {
    }

    public ServerSign(Location location, ClickType clickType, ServerSignExecData execData) {
        setLocation(location);
        setServerSignExecutorData(clickType, execData);
    }

    public ServerSign(Location location, ClickType clickType, ServerSignCommand command) {
        setLocation(location);
        setServerSignExecutorData(clickType, new ServerSignExecData(command));
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

    // Executor data

    public ArrayList<ServerSignCommand> getServerSignCommands(ClickType clickType) {
        if (execDataMap.containsKey(clickType)) {
            return execDataMap.get(clickType).getCommands();
        } else {
            return new ArrayList<>();
        }
    }

    public ServerSignExecData getServerSignExecutorData(ClickType clickType) {
        return execDataMap.get(clickType);
    }

    public Map<ClickType, ServerSignExecData> getServerSignExecutorData() {
        return execDataMap;
    }

    public void setServerSignExecutorData(ClickType clickType, ServerSignExecData serverSignExecData) {
        execDataMap.put(clickType, serverSignExecData);
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

    public void addProtectedBlock(Location location) {
        protectedBlocks.add(location);
    }

    public boolean isProtected(Location location) {
        return protectedBlocks.contains(location);
    }

    public void updateProtectedBlocks() {
        protectedBlocks.clear();
        Block block = getLocation().getBlock();

        if (block.getState().getData() instanceof Attachable) {
            Attachable attachable = (Attachable) block.getState().getData();
            addProtectedBlock(block.getRelative(attachable.getAttachedFace()).getLocation());
        } else if (block.getState().getData() instanceof Door) {
            Door door = (Door) block.getState().getData();
            if (door.isTopHalf()) {
                addProtectedBlock(block.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).getLocation());
            } else {
                addProtectedBlock(block.getRelative(BlockFace.DOWN).getLocation());
            }
        }
    }

    // Display internal messages

    public boolean shouldDisplayInternalMessages() {
        return displayInternalMessages;
    }

    public void setDisplayInternalMessages(boolean val) {
        displayInternalMessages = val;
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
