/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 */
package me.herex.friendsystem;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class VersionHandler {
    private static String serverVersion;
    private static boolean isLegacy;
    private static boolean isModern;
    private static int majorVersion;

    public static void init() {
        String pkg;
        String[] parts;
        String bukkitVersion = Bukkit.getBukkitVersion();
        serverVersion = bukkitVersion != null && !bukkitVersion.isEmpty() ? "v" + bukkitVersion.split("-")[0].replace('.', '_') : ((parts = (pkg = Bukkit.getServer().getClass().getPackage().getName()).split("\\.")).length > 3 ? parts[3] : "v1_8_R1");
        isLegacy = serverVersion.startsWith("v1_8") || serverVersion.startsWith("v1_9") || serverVersion.startsWith("v1_10") || serverVersion.startsWith("v1_11") || serverVersion.startsWith("v1_12");
        isModern = !isLegacy;
        majorVersion = VersionHandler.getMajorVersion();
        Bukkit.getLogger().info("[FriendSystem] Detected server version: " + serverVersion);
        Bukkit.getLogger().info("[FriendSystem] Using " + (isLegacy ? "legacy" : "modern") + " API for sounds and materials.");
    }

    public static String getServerVersion() {
        return serverVersion;
    }

    public static boolean isLegacy() {
        return isLegacy;
    }

    public static boolean isModern() {
        return isModern;
    }

    public static UUID getPlayerUUID(Player player) {
        return player.getUniqueId();
    }

    public static String getPlayerName(Player player) {
        return player.getName();
    }

    public static boolean isPlayerOnline(UUID uuid) {
        Player player = Bukkit.getPlayer((UUID)uuid);
        return player != null && player.isOnline();
    }

    public static Player getOnlinePlayer(UUID uuid) {
        return Bukkit.getPlayer((UUID)uuid);
    }

    public static Player getOnlinePlayer(String name) {
        return Bukkit.getPlayer((String)name);
    }

    public static String getPlayerDisplayName(Player player) {
        if (isLegacy) {
            return player.getDisplayName();
        }
        return player.getDisplayName();
    }

    public static int getPlayerPing(Player player) {
        try {
            Object entityPlayer = player.getClass().getMethod("getHandle", new Class[0]).invoke(player, new Object[0]);
            return (Integer)entityPlayer.getClass().getField("ping").get(entityPlayer);
        }
        catch (Exception e) {
            return -1;
        }
    }

    public static String getPlayerWorldName(Player player) {
        return player.getWorld().getName();
    }

    public static String getServerName() {
        return Bukkit.getServerName();
    }

    public static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        }
        catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean methodExists(Class<?> clazz, String methodName, Class<?> ... parameterTypes) {
        try {
            clazz.getMethod(methodName, parameterTypes);
            return true;
        }
        catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static int getMajorVersion() {
        if (majorVersion != -1) {
            return majorVersion;
        }
        try {
            String version = serverVersion.substring(1);
            String[] parts = version.split("_");
            return Integer.parseInt(parts[1]);
        }
        catch (Exception e) {
            return 8;
        }
    }

    public static int getMinorVersion() {
        try {
            String version = serverVersion.substring(1);
            String[] parts = version.split("_");
            return Integer.parseInt(parts[2]);
        }
        catch (Exception e) {
            return 8;
        }
    }

    public static boolean isAtLeast(int major, int minor) {
        int currentMajor = VersionHandler.getMajorVersion();
        int currentMinor = VersionHandler.getMinorVersion();
        if (currentMajor > major) {
            return true;
        }
        if (currentMajor < major) {
            return false;
        }
        return currentMinor >= minor;
    }

    public static Material getMaterial(String modernName, String legacyName) {
        try {
            if (legacyName.equalsIgnoreCase("SIGN") || modernName.equalsIgnoreCase("SIGN")) {
                if (isModern) {
                    return Material.valueOf((String)"OAK_SIGN");
                }
                if (Material.getMaterial((String)"SIGN") != null) {
                    return Material.getMaterial((String)"SIGN");
                }
                if (Material.getMaterial((String)"SIGN_ITEM") != null) {
                    return Material.getMaterial((String)"SIGN_ITEM");
                }
            }
            return Material.valueOf((String)(isModern ? modernName : legacyName));
        }
        catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("[FriendSystem] Could not find material: " + (isModern ? modernName : legacyName));
            return Material.AIR;
        }
    }

    public static Sound getSound(String modernSound, String legacySound) {
        try {
            return Sound.valueOf((String)(isModern ? modernSound : legacySound));
        }
        catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("[FriendSystem] Could not find sound: " + (isModern ? modernSound : legacySound));
            return Sound.valueOf((String)(isModern ? "UI_BUTTON_CLICK" : "CLICK"));
        }
    }

    static {
        majorVersion = -1;
    }
}

