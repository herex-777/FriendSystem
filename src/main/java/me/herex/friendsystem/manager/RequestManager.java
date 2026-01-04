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
import me.herex.friendsystem.model.FriendRequest;
import me.herex.friendsystem.model.PlayerData;
import me.herex.friendsystem.model.PlayerSettings;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class RequestManager {
    private final FriendSystem plugin;
    private final Map<UUID, Map<UUID, FriendRequest>> activeRequests;
    private final Map<UUID, Set<UUID>> blockedSenders = new ConcurrentHashMap<UUID, Set<UUID>>();

    public RequestManager(FriendSystem plugin) {
        this.plugin = plugin;
        this.activeRequests = new ConcurrentHashMap<UUID, Map<UUID, FriendRequest>>();
    }

    public RequestResult sendRequest(UUID fromUUID, UUID toUUID) {
        String targetName;
        if (fromUUID.equals(toUUID)) {
            return RequestResult.SELF_REQUEST;
        }
        if (this.plugin.getBlockManager().isBlocked(toUUID, fromUUID)) {
            return RequestResult.TARGET_BLOCKED_YOU;
        }
        if (this.plugin.getBlockManager().isBlocked(fromUUID, toUUID)) {
            return RequestResult.YOU_BLOCKED_TARGET;
        }
        if (this.plugin.getFriendManager().areFriends(fromUUID, toUUID)) {
            return RequestResult.ALREADY_FRIENDS;
        }
        if (this.hasActiveRequest(fromUUID, toUUID)) {
            return RequestResult.REQUEST_ALREADY_SENT;
        }
        if (this.plugin.getDataManager().hasPlayerData(toUUID)) {
            PlayerData targetData = this.plugin.getDataManager().getPlayerData(toUUID);
            PlayerSettings settings = targetData.getSettings();
            switch (settings.getRequestPrivacy()) {
                case MAX: 
                case HIGH: {
                    Player sender = Bukkit.getPlayer((UUID)fromUUID);
                    if (sender != null && sender.hasPermission("friendsystem.staff")) break;
                    return RequestResult.PRIVACY_LEVEL_DENIED;
                }
            }
        }
        int maxRequests = 10;
        if (this.getOutgoingRequestCount(fromUUID) >= maxRequests) {
            return RequestResult.MAX_REQUESTS_SENT;
        }
        FriendRequest request = new FriendRequest(fromUUID, toUUID);
        this.activeRequests.computeIfAbsent(fromUUID, k -> new HashMap()).put(toUUID, request);
        PlayerData fromData = this.plugin.getDataManager().getPlayerData(fromUUID);
        PlayerData toData = this.plugin.getDataManager().getPlayerData(toUUID);
        if (toData.getPlayerName().isEmpty() && (targetName = Bukkit.getOfflinePlayer((UUID)toUUID).getName()) != null) {
            toData.setPlayerName(targetName);
        }
        fromData.addOutgoingRequest(toUUID);
        toData.addIncomingRequest(fromUUID);
        this.plugin.getDataManager().savePlayerData(fromData);
        this.plugin.getDataManager().savePlayerData(toData);
        return RequestResult.SUCCESS;
    }

    public boolean acceptRequest(UUID fromUUID, UUID toUUID) {
        FriendRequest request = this.getActiveRequest(fromUUID, toUUID);
        if (request == null || !request.isPending()) {
            return false;
        }
        request.setStatus(FriendRequest.RequestStatus.ACCEPTED);
        boolean success = this.plugin.getFriendManager().addFriend(fromUUID, toUUID);
        if (success) {
            this.removeActiveRequest(fromUUID, toUUID);
        }
        return success;
    }

    public boolean denyRequest(UUID fromUUID, UUID toUUID) {
        FriendRequest request = this.getActiveRequest(fromUUID, toUUID);
        if (request == null || !request.isPending()) {
            return false;
        }
        request.setStatus(FriendRequest.RequestStatus.DENIED);
        this.removeActiveRequest(fromUUID, toUUID);
        PlayerData fromData = this.plugin.getDataManager().getPlayerData(fromUUID);
        PlayerData toData = this.plugin.getDataManager().getPlayerData(toUUID);
        fromData.removeOutgoingRequest(toUUID);
        toData.removeIncomingRequest(fromUUID);
        this.plugin.getDataManager().savePlayerData(fromData);
        this.plugin.getDataManager().savePlayerData(toData);
        return true;
    }

    public boolean hasActiveRequest(UUID fromUUID, UUID toUUID) {
        Map<UUID, FriendRequest> requests = this.activeRequests.get(fromUUID);
        if (requests == null) {
            return false;
        }
        FriendRequest request = requests.get(toUUID);
        return request != null && request.isPending();
    }

    public FriendRequest getActiveRequest(UUID fromUUID, UUID toUUID) {
        Map<UUID, FriendRequest> requests = this.activeRequests.get(fromUUID);
        if (requests == null) {
            return null;
        }
        return requests.get(toUUID);
    }

    public void removeActiveRequest(UUID fromUUID, UUID toUUID) {
        Map<UUID, FriendRequest> requests = this.activeRequests.get(fromUUID);
        if (requests != null) {
            requests.remove(toUUID);
            if (requests.isEmpty()) {
                this.activeRequests.remove(fromUUID);
            }
        }
    }

    public Map<UUID, FriendRequest> getIncomingRequests(UUID playerUUID) {
        HashMap<UUID, FriendRequest> incoming = new HashMap<UUID, FriendRequest>();
        for (Map<UUID, FriendRequest> requests : this.activeRequests.values()) {
            for (Map.Entry<UUID, FriendRequest> entry : requests.entrySet()) {
                FriendRequest request = entry.getValue();
                if (!request.getTo().equals(playerUUID) || !request.isPending()) continue;
                incoming.put(request.getFrom(), request);
            }
        }
        return incoming;
    }

    public Map<UUID, FriendRequest> getOutgoingRequests(UUID playerUUID) {
        HashMap<UUID, FriendRequest> outgoing = new HashMap<UUID, FriendRequest>();
        Map<UUID, FriendRequest> requests = this.activeRequests.get(playerUUID);
        if (requests != null) {
            for (Map.Entry<UUID, FriendRequest> entry : requests.entrySet()) {
                FriendRequest request = entry.getValue();
                if (!request.isPending()) continue;
                outgoing.put(request.getTo(), request);
            }
        }
        return outgoing;
    }

    public int getIncomingRequestCount(UUID playerUUID) {
        return this.getIncomingRequests(playerUUID).size();
    }

    public int getOutgoingRequestCount(UUID playerUUID) {
        return this.getOutgoingRequests(playerUUID).size();
    }

    public void clearExpiredRequests() {
        long timeoutMinutes = 5L;
        for (Map<UUID, FriendRequest> requests : this.activeRequests.values()) {
            requests.entrySet().removeIf(entry -> {
                FriendRequest request = (FriendRequest)entry.getValue();
                if (request.isExpired(timeoutMinutes)) {
                    Player fromPlayer = VersionHandler.getOnlinePlayer(request.getFrom());
                    Player toPlayer = VersionHandler.getOnlinePlayer(request.getTo());
                    if (fromPlayer != null) {
                        PlayerData toData = this.plugin.getDataManager().getPlayerData(request.getTo());
                        String toName = FriendCommand.getDisplayName(request.getTo(), toData, false, true);
                        fromPlayer.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
                        fromPlayer.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)("&eYour friend request to " + toName + " &ehas expired.")));
                        fromPlayer.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
                    }
                    PlayerData fromData = this.plugin.getDataManager().getPlayerData(request.getFrom());
                    PlayerData toData = this.plugin.getDataManager().getPlayerData(request.getTo());
                    fromData.removeOutgoingRequest(request.getTo());
                    toData.removeIncomingRequest(request.getFrom());
                    this.plugin.getDataManager().savePlayerData(fromData);
                    this.plugin.getDataManager().savePlayerData(toData);
                    return true;
                }
                return false;
            });
        }
        this.activeRequests.entrySet().removeIf(entry -> ((Map)entry.getValue()).isEmpty());
    }

    public boolean canSendRequest(UUID playerUUID) {
        int maxRequests = 10;
        return this.getOutgoingRequests(playerUUID).size() < maxRequests;
    }

    public int getMaxRequests() {
        return 10;
    }

    public void cancelAllRequestsFrom(UUID playerUUID) {
        Map<UUID, FriendRequest> requests = this.activeRequests.remove(playerUUID);
        if (requests != null) {
            for (FriendRequest friendRequest : requests.values()) {
                if (!friendRequest.isPending()) continue;
                PlayerData toData = this.plugin.getDataManager().getPlayerData(friendRequest.getTo());
                toData.removeIncomingRequest(playerUUID);
                this.plugin.getDataManager().savePlayerData(toData);
            }
        }
        for (Map<UUID, FriendRequest> map : this.activeRequests.values()) {
            map.entrySet().removeIf(entry -> {
                FriendRequest request = entry.getValue();
                if (request.getTo().equals(playerUUID) && request.isPending()) {
                    PlayerData fromData = this.plugin.getDataManager().getPlayerData(request.getFrom());
                    fromData.removeOutgoingRequest(playerUUID);
                    this.plugin.getDataManager().savePlayerData(fromData);
                    return true;
                }
                return false;
            });
        }
    }

    public void clearSenderBlocksOnLogin(UUID receiverUUID) {
        this.blockedSenders.remove(receiverUUID);
    }

    public void clearSenderBlock(UUID user1, UUID user2) {
        if (this.blockedSenders.containsKey(user1)) {
            this.blockedSenders.get(user1).remove(user2);
        }
        if (this.blockedSenders.containsKey(user2)) {
            this.blockedSenders.get(user2).remove(user1);
        }
    }

    public void loadAllPendingRequests() {
        for (PlayerData data : this.plugin.getDataManager().getAllPlayerData().values()) {
            UUID to = data.getPlayerUUID();
            for (UUID from : data.getIncomingRequests().keySet()) {
                PlayerData fromData = this.plugin.getDataManager().getPlayerData(from);
                if (fromData == null || !fromData.hasOutgoingRequest(to)) continue;
                FriendRequest request = new FriendRequest(from, to, data.getIncomingRequests().get(from), FriendRequest.RequestStatus.PENDING);
                this.activeRequests.computeIfAbsent(from, k -> new HashMap()).put(to, request);
            }
        }
    }

    public void activateRequestsFor(UUID receiverUUID) {
        for (Map<UUID, FriendRequest> sentRequests : this.activeRequests.values()) {
            for (FriendRequest request : sentRequests.values()) {
                if (!request.getTo().equals(receiverUUID) || !request.isPending() || request.getActivationTimestamp() != 0L) continue;
                request.setActivationTimestamp(System.currentTimeMillis());
            }
        }
    }

    public static enum RequestResult {
        SUCCESS,
        ALREADY_FRIENDS,
        TARGET_BLOCKED_YOU,
        YOU_BLOCKED_TARGET,
        PRIVACY_LEVEL_DENIED,
        REQUEST_ALREADY_SENT,
        SELF_REQUEST,
        MAX_REQUESTS_SENT,
        TARGET_DOES_NOT_EXIST;

    }
}

