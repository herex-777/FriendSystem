/*
 * Decompiled with CFR 0.152.
 */
package me.herex.friendsystem.model;

import java.util.UUID;

public class FriendRequest {
    private final UUID from;
    private final UUID to;
    private final long timestamp;
    private RequestStatus status;
    private long activationTimestamp = 0L;

    public FriendRequest(UUID from, UUID to) {
        this.from = from;
        this.to = to;
        this.timestamp = System.currentTimeMillis();
        this.status = RequestStatus.PENDING;
        this.activationTimestamp = 0L;
    }

    public FriendRequest(UUID from, UUID to, long timestamp, RequestStatus status) {
        this.from = from;
        this.to = to;
        this.timestamp = timestamp;
        this.status = status;
        this.activationTimestamp = 0L;
    }

    public UUID getFrom() {
        return this.from;
    }

    public UUID getTo() {
        return this.to;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public RequestStatus getStatus() {
        return this.status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public long getActivationTimestamp() {
        return this.activationTimestamp;
    }

    public void setActivationTimestamp(long activationTimestamp) {
        this.activationTimestamp = activationTimestamp;
    }

    public boolean isExpired(long timeoutMinutes) {
        if (this.activationTimestamp == 0L) {
            return false;
        }
        long timeoutMillis = timeoutMinutes * 60L * 1000L;
        return System.currentTimeMillis() - this.activationTimestamp > timeoutMillis;
    }

    public boolean isPending() {
        return this.status == RequestStatus.PENDING;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        FriendRequest that = (FriendRequest)obj;
        return this.from.equals(that.from) && this.to.equals(that.to);
    }

    public int hashCode() {
        return this.from.hashCode() * 31 + this.to.hashCode();
    }

    public String toString() {
        return "FriendRequest{from=" + this.from + ", to=" + this.to + ", timestamp=" + this.timestamp + ", status=" + (Object)((Object)this.status) + ", activationTimestamp=" + this.activationTimestamp + '}';
    }

    public static enum RequestStatus {
        PENDING,
        ACCEPTED,
        DENIED,
        EXPIRED;

    }
}

