/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.entity.Player
 */
package me.herex.friendsystem.manager;

import me.herex.friendsystem.FriendSystem;
import me.herex.friendsystem.model.PlayerData;
import me.herex.friendsystem.model.PlayerSettings;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class DataManager {
    private final FriendSystem plugin;
    private final Map<UUID, PlayerData> playerDataCache;
    private final File dataFile;
    private FileConfiguration dataConfig;

    public DataManager(FriendSystem plugin) {
        this.plugin = plugin;
        this.playerDataCache = new ConcurrentHashMap<UUID, PlayerData>();
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
    }

    public void loadAllData() {
        if (!this.dataFile.exists()) {
            this.plugin.saveResource("data.yml", false);
        }
        this.dataConfig = YamlConfiguration.loadConfiguration((File)this.dataFile);
        ConfigurationSection playersSection = this.dataConfig.getConfigurationSection("players");
        if (playersSection != null) {
            for (String uuidString : playersSection.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    PlayerData playerData = this.loadPlayerData(uuid);
                    if (playerData == null) continue;
                    this.playerDataCache.put(uuid, playerData);
                }
                catch (IllegalArgumentException e) {
                    this.plugin.getLogger().warning("Invalid UUID in data file: " + uuidString);
                }
            }
        }
        this.plugin.getLogger().info("Loaded data for " + this.playerDataCache.size() + " players");
    }

    public void saveAllData() {
        for (PlayerData playerData : this.playerDataCache.values()) {
            this.savePlayerData(playerData);
        }
        try {
            this.dataConfig.save(this.dataFile);
            this.plugin.getLogger().info("Saved data for " + this.playerDataCache.size() + " players");
        }
        catch (IOException e) {
            this.plugin.getLogger().severe("Failed to save data file: " + e.getMessage());
        }
    }

    public PlayerData getPlayerData(UUID playerUUID) {
        return this.playerDataCache.computeIfAbsent(playerUUID, this::loadPlayerData);
    }

    public PlayerData getPlayerData(Player player) {
        return this.getPlayerData(player.getUniqueId());
    }

    public PlayerData createPlayerData(UUID playerUUID, String playerName) {
        if (this.playerDataCache.containsKey(playerUUID)) {
            return this.playerDataCache.get(playerUUID);
        }
        PlayerData newData = new PlayerData(playerUUID, playerName);
        this.playerDataCache.put(playerUUID, newData);
        this.plugin.getLogger().info("Created new data in memory for offline player " + playerName);
        return newData;
    }

    public void removePlayerData(UUID playerUUID) {
        this.playerDataCache.remove(playerUUID);
        if (this.dataConfig.isConfigurationSection("players." + playerUUID.toString())) {
            this.dataConfig.set("players." + playerUUID.toString(), null);
            this.plugin.getLogger().info("Removed data for player " + playerUUID + " from data.yml.");
        }
    }

    public void clearAllPlayerData() {
        this.playerDataCache.clear();
        this.dataConfig.set("players", null);
        this.plugin.getLogger().warning("Cleared all player data from cache and data.yml.");
    }

    public void savePlayerData(PlayerData playerData) {
        this.playerDataCache.put(playerData.getPlayerUUID(), playerData);
        ConfigurationSection playerSection = this.dataConfig.createSection("players." + playerData.getPlayerUUID().toString());
        playerSection.set("name", (Object)playerData.getPlayerName());
        playerSection.set("friends",
                playerData.getFriends().stream()
                        .map(UUID::toString)
                        .collect(java.util.stream.Collectors.toList()));

        playerSection.set("blocked",
                playerData.getBlockedPlayers().stream()
                        .map(UUID::toString)
                        .collect(java.util.stream.Collectors.toList()));

        playerSection.set("best_friends",
                playerData.getBestFriends().stream()
                        .map(UUID::toString)
                        .collect(java.util.stream.Collectors.toList()));
        HashMap<String, String> nickMap = new HashMap<String, String>();
        for (Map.Entry<UUID, String> entry : playerData.getNicknames().entrySet()) {
            nickMap.put(entry.getKey().toString(), entry.getValue());
        }
        playerSection.createSection("nicknames", nickMap);
        ConfigurationSection incomingSection = playerSection.createSection("incoming_requests");
        for (Map.Entry<UUID, Long> entry : playerData.getIncomingRequests().entrySet()) {
            incomingSection.set(entry.getKey().toString(), (Object)entry.getValue());
        }
        ConfigurationSection configurationSection = playerSection.createSection("outgoing_requests");
        for (Map.Entry<UUID, Long> entry : playerData.getOutgoingRequests().entrySet()) {
            configurationSection.set(entry.getKey().toString(), (Object)entry.getValue());
        }
        PlayerSettings playerSettings = playerData.getSettings();
        ConfigurationSection configurationSection2 = playerSection.createSection("settings");
        configurationSection2.set("allow_requests", (Object)playerSettings.isAllowRequests());
        configurationSection2.set("appear_offline", (Object)playerSettings.isAppearOffline());
        configurationSection2.set("notify_online", (Object)playerSettings.isNotifyOnline());
        configurationSection2.set("allow_join", (Object)playerSettings.isAllowJoin());
        configurationSection2.set("notify_join", (Object)playerSettings.isNotifyJoin());
        configurationSection2.set("messagePrivacy", (Object)playerSettings.getMessagePrivacy().name());
    }

    private PlayerData loadPlayerData(UUID playerUUID) {
        ConfigurationSection playerSection = this.dataConfig.getConfigurationSection("players." + playerUUID.toString());
        if (playerSection == null) {
            return new PlayerData(playerUUID);
        }
        String playerName = playerSection.getString("name", "");
        HashSet<UUID> friends = new HashSet<UUID>();
        if (playerSection.contains("friends")) {
            for (Object friendUUID : playerSection.getStringList("friends")) {
                try {
                    friends.add(UUID.fromString((String)friendUUID));
                }
                catch (IllegalArgumentException e) {
                    this.plugin.getLogger().warning("Invalid friend UUID: " + (String)friendUUID);
                }
            }
        }
        LinkedHashSet<UUID> blocked = new LinkedHashSet<UUID>();
        if (playerSection.contains("blocked")) {
            for (Object blockedUUID : playerSection.getStringList("blocked")) {
                try {
                    blocked.add(UUID.fromString((String)blockedUUID));
                }
                catch (IllegalArgumentException e) {
                    this.plugin.getLogger().warning("Invalid blocked UUID: " + (String)blockedUUID);
                }
            }
        }
        HashSet<UUID> bestFriends = new HashSet<UUID>();
        if (playerSection.contains("best_friends")) {
            for (String bestUUID : playerSection.getStringList("best_friends")) {
                try {
                    bestFriends.add(UUID.fromString(bestUUID));
                }
                catch (IllegalArgumentException e) {
                    this.plugin.getLogger().warning("Invalid best friend UUID: " + bestUUID);
                }
            }
        }
        HashMap<UUID, String> nicknames = new HashMap<UUID, String>();
        ConfigurationSection nickSection = playerSection.getConfigurationSection("nicknames");
        if (nickSection != null) {
            for (String uuidString : nickSection.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    String nick = nickSection.getString(uuidString, "");
                    if (nick.isEmpty()) continue;
                    nicknames.put(uuid, nick);
                }
                catch (IllegalArgumentException e) {
                    this.plugin.getLogger().warning("Invalid nickname UUID: " + uuidString);
                }
            }
        }
        HashMap<UUID, Long> incomingRequests = new HashMap<UUID, Long>();
        ConfigurationSection incomingSection = playerSection.getConfigurationSection("incoming_requests");
        if (incomingSection != null) {
            for (String uuidString : incomingSection.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    long timestamp = incomingSection.getLong(uuidString);
                    incomingRequests.put(uuid, timestamp);
                }
                catch (IllegalArgumentException e) {
                    this.plugin.getLogger().warning("Invalid incoming request UUID: " + uuidString);
                }
            }
        }
        HashMap<UUID, Long> outgoingRequests = new HashMap<UUID, Long>();
        ConfigurationSection outgoingSection = playerSection.getConfigurationSection("outgoing_requests");
        if (outgoingSection != null) {
            for (String uuidString : outgoingSection.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    long timestamp = outgoingSection.getLong(uuidString);
                    outgoingRequests.put(uuid, timestamp);
                }
                catch (IllegalArgumentException e) {
                    this.plugin.getLogger().warning("Invalid outgoing request UUID: " + uuidString);
                }
            }
        }
        PlayerSettings settings = new PlayerSettings();
        ConfigurationSection settingsSection = playerSection.getConfigurationSection("settings");
        if (settingsSection != null) {
            settings.setAllowRequests(settingsSection.getBoolean("allow_requests", true));
            settings.setAppearOffline(settingsSection.getBoolean("appear_offline", false));
            settings.setNotifyOnline(settingsSection.getBoolean("notify_online", true));
            settings.setAllowJoin(settingsSection.getBoolean("allow_join", true));
            settings.setNotifyJoin(settingsSection.getBoolean("notify_join", true));
            String privacy = settingsSection.getString("messagePrivacy", "HIGH");
            try {
                settings.setMessagePrivacy(PlayerSettings.MessagePrivacy.valueOf(privacy));
            }
            catch (IllegalArgumentException e) {
                settings.setMessagePrivacy(PlayerSettings.MessagePrivacy.HIGH);
            }
        }
        return new PlayerData(playerUUID, playerName, friends, incomingRequests, outgoingRequests, settings, blocked, bestFriends, nicknames);
    }

    public boolean hasPlayerData(UUID playerUUID) {
        return this.playerDataCache.containsKey(playerUUID) || this.dataConfig.contains("players." + playerUUID.toString());
    }

    public Map<UUID, PlayerData> getAllPlayerData() {
        return new HashMap<UUID, PlayerData>(this.playerDataCache);
    }

    public void clearExpiredRequests() {
        long timeoutMinutes = 5L;
        for (PlayerData playerData : this.playerDataCache.values()) {
            playerData.clearExpiredRequests(timeoutMinutes);
        }
    }

    public Set<UUID> getAllKnownUUIDs() {
        HashSet<UUID> uuids = new HashSet<UUID>(this.playerDataCache.keySet());
        if (this.dataConfig != null && this.dataConfig.isConfigurationSection("players")) {
            for (String uuidString : this.dataConfig.getConfigurationSection("players").getKeys(false)) {
                try {
                    uuids.add(UUID.fromString(uuidString));
                }
                catch (IllegalArgumentException illegalArgumentException) {}
            }
        }
        return uuids;
    }
}

