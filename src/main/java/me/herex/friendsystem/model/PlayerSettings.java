/*
 * Decompiled with CFR 0.152.
 */
package me.herex.friendsystem.model;

public class PlayerSettings {
    private RequestPrivacy requestPrivacy;
    private boolean notifyOnline;
    private boolean allowJoin;
    private boolean notifyJoin;
    private OnlineStatus onlineStatus;
    private MessagePrivacy messagePrivacy;
    private int notificationLevel = 0;

    public PlayerSettings() {
        this.requestPrivacy = RequestPrivacy.NONE;
        this.notifyOnline = true;
        this.allowJoin = true;
        this.notifyJoin = true;
        this.onlineStatus = OnlineStatus.ONLINE;
        this.messagePrivacy = MessagePrivacy.NONE;
    }

    public PlayerSettings(boolean allowRequests, boolean appearOffline, boolean notifyOnline, boolean allowJoin, boolean notifyJoin, MessagePrivacy messagePrivacy) {
        this.requestPrivacy = allowRequests ? RequestPrivacy.NONE : RequestPrivacy.MAX;
        this.notifyOnline = notifyOnline;
        this.allowJoin = allowJoin;
        this.notifyJoin = notifyJoin;
        this.onlineStatus = appearOffline ? OnlineStatus.APPEAR_OFFLINE : OnlineStatus.ONLINE;
        this.messagePrivacy = messagePrivacy != null ? messagePrivacy : MessagePrivacy.NONE;
    }

    public boolean isAllowRequests() {
        return this.requestPrivacy != RequestPrivacy.MAX;
    }

    public void setAllowRequests(boolean allowRequests) {
        this.requestPrivacy = allowRequests ? RequestPrivacy.NONE : RequestPrivacy.MAX;
    }

    public RequestPrivacy getRequestPrivacy() {
        return this.requestPrivacy;
    }

    public void setRequestPrivacy(RequestPrivacy privacy) {
        this.requestPrivacy = privacy;
    }

    public boolean isAppearOffline() {
        return this.onlineStatus == OnlineStatus.APPEAR_OFFLINE;
    }

    public void setAppearOffline(boolean appearOffline) {
        this.onlineStatus = appearOffline ? OnlineStatus.APPEAR_OFFLINE : OnlineStatus.ONLINE;
    }

    public boolean isNotifyOnline() {
        return this.notifyOnline;
    }

    public void setNotifyOnline(boolean notifyOnline) {
        this.notifyOnline = notifyOnline;
    }

    public boolean isAllowJoin() {
        return this.allowJoin;
    }

    public void setAllowJoin(boolean allowJoin) {
        this.allowJoin = allowJoin;
    }

    public boolean isNotifyJoin() {
        return this.notifyJoin;
    }

    public void setNotifyJoin(boolean notifyJoin) {
        this.notifyJoin = notifyJoin;
    }

    public OnlineStatus getOnlineStatus() {
        return this.onlineStatus;
    }

    public void setOnlineStatus(OnlineStatus status) {
        this.onlineStatus = status;
    }

    public MessagePrivacy getMessagePrivacy() {
        return this.messagePrivacy;
    }

    public void setMessagePrivacy(MessagePrivacy privacy) {
        this.messagePrivacy = privacy;
    }

    public int getNotificationLevel() {
        return this.notificationLevel;
    }

    public void setNotificationLevel(int level) {
        this.notificationLevel = level;
    }

    public String toString() {
        return "PlayerSettings{requestPrivacy=" + (Object)((Object)this.requestPrivacy) + ", notifyOnline=" + this.notifyOnline + ", allowJoin=" + this.allowJoin + ", notifyJoin=" + this.notifyJoin + ", onlineStatus=" + (Object)((Object)this.onlineStatus) + ", messagePrivacy=" + (Object)((Object)this.messagePrivacy) + '}';
    }

    public static enum RequestPrivacy {
        MAX,
        HIGH,
        NONE;

    }

    public static enum OnlineStatus {
        ONLINE,
        AWAY,
        BUSY,
        APPEAR_OFFLINE;

    }

    public static enum MessagePrivacy {
        MAX,
        HIGH,
        MEDIUM,
        LOW,
        NONE;

    }
}

