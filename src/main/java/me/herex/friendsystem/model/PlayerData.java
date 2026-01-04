/*
 * Decompiled with CFR 0.152.
 */
package me.herex.friendsystem.model;

import me.herex.friendsystem.model.PlayerSettings;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerData {
    private final UUID playerUUID;
    private String playerName;
    private final Set<UUID> friends;
    private final Map<UUID, Long> incomingRequests;
    private final Map<UUID, Long> outgoingRequests;
    private PlayerSettings settings;
    private final Set<UUID> blockedPlayers;
    private final Set<UUID> bestFriends;
    private final Map<UUID, String> nicknames;
    private String lastKnownPrefix = "";
    private String lastKnownColor = "&f";
    private long lastLogoutTime = 0L;
    private long appearOfflineSince = 0L;
    private long friendshipStarted;

    public PlayerData(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.playerName = "";
        this.friends = new HashSet<UUID>();
        this.incomingRequests = new HashMap<UUID, Long>();
        this.outgoingRequests = new HashMap<UUID, Long>();
        this.settings = new PlayerSettings();
        this.blockedPlayers = new LinkedHashSet<UUID>();
        this.bestFriends = new HashSet<UUID>();
        this.nicknames = new HashMap<UUID, String>();
        this.lastKnownPrefix = "";
        this.lastKnownColor = "&f";
        this.lastLogoutTime = 0L;
        this.appearOfflineSince = 0L;
        this.friendshipStarted = 0L;
    }

    public PlayerData(UUID playerUUID, String playerName, Set<UUID> friends, Map<UUID, Long> incomingRequests, Map<UUID, Long> outgoingRequests, PlayerSettings settings, Set<UUID> blockedPlayers, Set<UUID> bestFriends, Map<UUID, String> nicknames) {
        this.playerUUID = playerUUID;
        this.playerName = playerName != null ? playerName : "";
        this.friends = friends != null ? friends : new HashSet();
        this.incomingRequests = incomingRequests != null ? incomingRequests : new HashMap();
        this.outgoingRequests = outgoingRequests != null ? outgoingRequests : new HashMap();
        this.settings = settings != null ? settings : new PlayerSettings();
        this.blockedPlayers = blockedPlayers != null ? blockedPlayers : new LinkedHashSet();
        this.bestFriends = bestFriends != null ? bestFriends : new HashSet();
        this.nicknames = nicknames != null ? nicknames : new HashMap();
        this.lastKnownPrefix = "";
        this.lastKnownColor = "&f";
        this.lastLogoutTime = 0L;
        this.appearOfflineSince = 0L;
        this.friendshipStarted = 0L;
    }

    public PlayerData(UUID playerUUID, String playerName) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.friends = new HashSet<UUID>();
        this.blockedPlayers = new LinkedHashSet<UUID>();
        this.settings = new PlayerSettings();
        this.incomingRequests = new HashMap<UUID, Long>();
        this.outgoingRequests = new HashMap<UUID, Long>();
        this.appearOfflineSince = 0L;
        this.bestFriends = new HashSet<UUID>();
        this.nicknames = new HashMap<UUID, String>();
        this.lastKnownPrefix = "";
        this.lastKnownColor = "&f";
        this.lastLogoutTime = 0L;
        this.friendshipStarted = 0L;
    }

    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName != null ? playerName : "";
    }

    public Set<UUID> getFriends() {
        return new HashSet<UUID>(this.friends);
    }

    public void addFriend(UUID friendUUID) {
        this.friends.add(friendUUID);
    }

    public void removeFriend(UUID friendUUID) {
        this.friends.remove(friendUUID);
    }

    public boolean isFriend(UUID friendUUID) {
        return this.friends.contains(friendUUID);
    }

    public int getFriendCount() {
        return this.friends.size();
    }

    public Map<UUID, Long> getIncomingRequests() {
        return new HashMap<UUID, Long>(this.incomingRequests);
    }

    public void addIncomingRequest(UUID fromUUID) {
        this.incomingRequests.put(fromUUID, System.currentTimeMillis());
    }

    public void removeIncomingRequest(UUID fromUUID) {
        this.incomingRequests.remove(fromUUID);
    }

    public boolean hasIncomingRequest(UUID fromUUID) {
        return this.incomingRequests.containsKey(fromUUID);
    }

    public Map<UUID, Long> getOutgoingRequests() {
        return new HashMap<UUID, Long>(this.outgoingRequests);
    }

    public void addOutgoingRequest(UUID toUUID) {
        this.outgoingRequests.put(toUUID, System.currentTimeMillis());
    }

    public void removeOutgoingRequest(UUID toUUID) {
        this.outgoingRequests.remove(toUUID);
    }

    public boolean hasOutgoingRequest(UUID toUUID) {
        return this.outgoingRequests.containsKey(toUUID);
    }

    public int getIncomingRequestCount() {
        return this.incomingRequests.size();
    }

    public int getOutgoingRequestCount() {
        return this.outgoingRequests.size();
    }

    public PlayerSettings getSettings() {
        return this.settings;
    }

    public void setSettings(PlayerSettings settings) {
        this.settings = settings;
    }

    public void clearExpiredRequests(long timeoutMinutes) {
        long currentTime = System.currentTimeMillis();
        long timeoutMillis = timeoutMinutes * 60L * 1000L;
        this.incomingRequests.entrySet().removeIf(entry -> currentTime - (Long)entry.getValue() > timeoutMillis);
        this.outgoingRequests.entrySet().removeIf(entry -> currentTime - (Long)entry.getValue() > timeoutMillis);
    }

    public Set<UUID> getBlockedPlayers() {
        return new LinkedHashSet<UUID>(this.blockedPlayers);
    }

    public void blockPlayer(UUID uuid) {
        this.blockedPlayers.add(uuid);
    }

    public void unblockPlayer(UUID uuid) {
        this.blockedPlayers.remove(uuid);
    }

    public boolean isBlocked(UUID uuid) {
        return this.blockedPlayers.contains(uuid);
    }

    public void unblockAll() {
        this.blockedPlayers.clear();
    }

    public Set<UUID> getBestFriends() {
        return new HashSet<UUID>(this.bestFriends);
    }

    public boolean isBestFriend(UUID uuid) {
        return this.bestFriends.contains(uuid);
    }

    public void addBestFriend(UUID uuid) {
        this.bestFriends.add(uuid);
    }

    public void removeBestFriend(UUID uuid) {
        this.bestFriends.remove(uuid);
    }

    public void toggleBestFriend(UUID uuid) {
        if (this.bestFriends.contains(uuid)) {
            this.bestFriends.remove(uuid);
        } else {
            this.bestFriends.add(uuid);
        }
    }

    public Map<UUID, String> getNicknames() {
        return new HashMap<UUID, String>(this.nicknames);
    }

    public String getNickname(UUID uuid) {
        return this.nicknames.get(uuid);
    }

    public void setNickname(UUID uuid, String nickname) {
        if (nickname == null || nickname.isEmpty()) {
            this.nicknames.remove(uuid);
        } else {
            this.nicknames.put(uuid, nickname);
        }
    }

    public void removeNickname(UUID uuid) {
        this.nicknames.remove(uuid);
    }

    public String getLastKnownPrefix() {
        return this.lastKnownPrefix;
    }

    public void setLastKnownPrefix(String prefix) {
        this.lastKnownPrefix = prefix != null ? prefix : "";
    }

    public String getLastKnownColor() {
        return this.lastKnownColor;
    }

    public void setLastKnownColor(String color) {
        this.lastKnownColor = color != null ? color : "&f";
    }

    public long getLastLogoutTime() {
        return this.lastLogoutTime;
    }

    public void setLastLogoutTime(long lastLogoutTime) {
        this.lastLogoutTime = lastLogoutTime;
    }

    public long getAppearOfflineSince() {
        return this.appearOfflineSince;
    }

    public void setAppearOfflineSince(long appearOfflineSince) {
        this.appearOfflineSince = appearOfflineSince;
    }

    public long getFriendshipStarted() {
        return this.friendshipStarted;
    }

    public void setFriendshipStarted(long friendshipStarted) {
        this.friendshipStarted = friendshipStarted;
    }

    public String toString() {
        return "PlayerData{playerUUID=" + this.playerUUID + ", friends=" + this.friends.size() + ", blockedPlayers=" + this.blockedPlayers.size() + ", incomingRequests=" + this.incomingRequests.size() + ", outgoingRequests=" + this.outgoingRequests.size() + ", settings=" + this.settings + '}';
    }
}

