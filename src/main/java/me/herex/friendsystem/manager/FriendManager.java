/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.entity.Player
 */
package me.herex.friendsystem.manager;

import me.herex.friendsystem.FriendSystem;
import me.herex.friendsystem.VersionHandler;
import me.herex.friendsystem.cmd.FriendCommand;
import me.herex.friendsystem.model.PlayerData;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class FriendManager {
    private final FriendSystem plugin;
    private final Map<String, Long> friendshipTimestamps = new ConcurrentHashMap<String, Long>();

    public FriendManager(FriendSystem plugin) {
        this.plugin = plugin;
    }

    public boolean addFriend(UUID player1, UUID player2) {
        PlayerData data1 = this.plugin.getDataManager().getPlayerData(player1);
        PlayerData data2 = this.plugin.getDataManager().getPlayerData(player2);
        if (data1.isFriend(player2)) {
            return false;
        }
        int maxFriends = 100;
        if (data1.getFriendCount() >= maxFriends || data2.getFriendCount() >= maxFriends) {
            return false;
        }
        data1.addFriend(player2);
        data2.addFriend(player1);
        long now = System.currentTimeMillis();
        this.setFriendshipTimestamp(player1, player2, now);
        this.setFriendshipTimestamp(player2, player1, now);
        data1.removeIncomingRequest(player2);
        data1.removeOutgoingRequest(player2);
        data2.removeIncomingRequest(player1);
        data2.removeOutgoingRequest(player1);
        this.plugin.getDataManager().savePlayerData(data1);
        this.plugin.getDataManager().savePlayerData(data2);
        return true;
    }

    public boolean removeFriend(UUID player1, UUID player2) {
        PlayerData data1 = this.plugin.getDataManager().getPlayerData(player1);
        PlayerData data2 = this.plugin.getDataManager().getPlayerData(player2);
        if (!data1.isFriend(player2)) {
            return false;
        }
        data1.removeFriend(player2);
        data2.removeFriend(player1);
        this.plugin.getRequestManager().clearSenderBlock(player1, player2);
        Player removedFriend = Bukkit.getPlayer((UUID)player2);
        if (removedFriend != null) {
            String removerDisplay = FriendCommand.getDisplayName(player1, data1, false, true);
            removedFriend.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
            removedFriend.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)(removerDisplay + " &cremoved you from their friends list!")));
            removedFriend.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
        }
        this.plugin.getDataManager().savePlayerData(data1);
        this.plugin.getDataManager().savePlayerData(data2);
        return true;
    }

    public boolean areFriends(UUID player1, UUID player2) {
        PlayerData data1 = this.plugin.getDataManager().getPlayerData(player1);
        return data1.isFriend(player2);
    }

    public boolean isBestFriend(UUID player1, UUID player2) {
        PlayerData data1 = this.plugin.getDataManager().getPlayerData(player1);
        return data1.getBestFriends().contains(player2);
    }

    public Set<UUID> getFriends(UUID playerUUID) {
        PlayerData data = this.plugin.getDataManager().getPlayerData(playerUUID);
        return data.getFriends();
    }

    public Set<Player> getOnlineFriends(UUID playerUUID) {
        HashSet<Player> onlineFriends = new HashSet<Player>();
        Set<UUID> friends = this.getFriends(playerUUID);
        for (UUID friendUUID : friends) {
            Player friend = VersionHandler.getOnlinePlayer(friendUUID);
            if (friend == null || !friend.isOnline()) continue;
            onlineFriends.add(friend);
        }
        return onlineFriends;
    }

    public Set<UUID> getOfflineFriends(UUID playerUUID) {
        HashSet<UUID> offlineFriends = new HashSet<UUID>();
        Set<UUID> friends = this.getFriends(playerUUID);
        for (UUID friendUUID : friends) {
            if (VersionHandler.isPlayerOnline(friendUUID)) continue;
            offlineFriends.add(friendUUID);
        }
        return offlineFriends;
    }

    public int getFriendCount(UUID playerUUID) {
        PlayerData data = this.plugin.getDataManager().getPlayerData(playerUUID);
        return data.getFriendCount();
    }

    public int getOnlineFriendCount(UUID playerUUID) {
        return this.getOnlineFriends(playerUUID).size();
    }

    public boolean hasReachedMaxFriends(UUID playerUUID) {
        int maxFriends = 100;
        return this.getFriendCount(playerUUID) >= maxFriends;
    }

    public Set<UUID> getPlayersWhoHaveAsFriend(UUID targetUUID) {
        HashSet<UUID> players = new HashSet<UUID>();
        for (PlayerData data : this.plugin.getDataManager().getAllPlayerData().values()) {
            if (!data.isFriend(targetUUID)) continue;
            players.add(data.getPlayerUUID());
        }
        return players;
    }

    public void notifyFriends(UUID playerUUID, String message) {
        Set<Player> onlineFriends = this.getOnlineFriends(playerUUID);
        for (Player friend : onlineFriends) {
            PlayerData friendData = this.plugin.getDataManager().getPlayerData(friend.getUniqueId());
            if (!friendData.getSettings().isNotifyOnline()) continue;
            friend.sendMessage(message);
        }
    }

    public String getFriendStatus(UUID playerUUID) {
        int totalFriends = this.getFriendCount(playerUUID);
        int onlineFriends = this.getOnlineFriendCount(playerUUID);
        int offlineFriends = totalFriends - onlineFriends;
        return String.format("Friends: %d online, %d offline (%d total)", onlineFriends, offlineFriends, totalFriends);
    }

    public boolean canAddMoreFriends(UUID playerUUID) {
        return !this.hasReachedMaxFriends(playerUUID);
    }

    public int getMaxFriends() {
        return 100;
    }

    public void setFriendshipTimestamp(UUID player1, UUID player2, long timestamp) {
        this.friendshipTimestamps.put(player1.toString() + ":" + player2.toString(), timestamp);
    }

    public long getFriendshipTimestamp(UUID player1, UUID player2) {
        return this.friendshipTimestamps.getOrDefault(player1.toString() + ":" + player2.toString(), 0L);
    }
}

