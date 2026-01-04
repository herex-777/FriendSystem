/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 */
package me.herex.friendsystem.util;

import org.bukkit.entity.Player;

public class ActionBarUtil {
    public static void sendActionBar(Player player, String message) {
        try {
            Player.class.getMethod("sendActionBar", String.class).invoke(player, message);
        }
        catch (Throwable t) {
            player.sendMessage(message);
        }
    }
}

