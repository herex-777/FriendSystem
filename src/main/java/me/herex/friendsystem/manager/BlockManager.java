/*
 * Decompiled with CFR 0.152.
 */
package me.herex.friendsystem.manager;

import me.herex.friendsystem.FriendSystem;
import me.herex.friendsystem.model.PlayerData;
import java.util.Set;
import java.util.UUID;

public class BlockManager {
    private final FriendSystem plugin;

    public BlockManager(FriendSystem plugin) {
        this.plugin = plugin;
    }

    public boolean blockPlayer(UUID blocker, UUID toBlock) {
        if (blocker.equals(toBlock)) {
            return false;
        }
        PlayerData data = this.plugin.getDataManager().getPlayerData(blocker);
        if (data.isBlocked(toBlock)) {
            return false;
        }
        data.blockPlayer(toBlock);
        this.plugin.getDataManager().savePlayerData(data);
        return true;
    }

    public boolean unblockPlayer(UUID blocker, UUID toUnblock) {
        PlayerData data = this.plugin.getDataManager().getPlayerData(blocker);
        if (!data.isBlocked(toUnblock)) {
            return false;
        }
        data.unblockPlayer(toUnblock);
        this.plugin.getDataManager().savePlayerData(data);
        return true;
    }

    public void unblockAll(UUID blocker) {
        PlayerData data = this.plugin.getDataManager().getPlayerData(blocker);
        data.unblockAll();
        this.plugin.getDataManager().savePlayerData(data);
    }

    public boolean isBlocked(UUID blocker, UUID target) {
        PlayerData data = this.plugin.getDataManager().getPlayerData(blocker);
        return data.isBlocked(target);
    }

    public Set<UUID> getBlockedPlayers(UUID blocker) {
        PlayerData data = this.plugin.getDataManager().getPlayerData(blocker);
        return data.getBlockedPlayers();
    }

    public int getBlockedCount(UUID blocker) {
        return this.getBlockedPlayers(blocker).size();
    }
}

