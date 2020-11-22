package xyz.nucleoid.plasmid.storage.FriendSystem;

import java.util.ArrayList;
import java.util.UUID;
/*
    Please only touch this if you know what you are doing
 */

public class FriendList {
    private int playerId;
    private final ArrayList<UUID> friends = new ArrayList<UUID>(); // holds accepted friends
    private final ArrayList<UUID> requests = new ArrayList<UUID>(); // holds friend requests

    public ArrayList<UUID> getFriends() {
        return this.friends;
    }

    public int getOwnerId() {
        return this.playerId;
    }

    public ArrayList<UUID> getRequests() {
        return this.requests;
    }

    public boolean hasSentRequest(UUID targetId) {
        return this.requests.contains(targetId);
    }

    public boolean hasFriend(UUID target) {
        return this.friends.contains(target);
    }

    public void addFriend(UUID addedFriend) {
        if (this.friends.contains(addedFriend)) {
            return;
        } else {
            this.friends.add(addedFriend);
        }
        for (UUID ids : this.friends) {
            System.out.println(ids);
        }
    }

    public void removeRequest(UUID request) {
        this.requests.remove(request);
    }

    public boolean addRequest(UUID requesterId) {
        if (this.requests.contains(requesterId)) {
            return false;
        }
        this.requests.add(requesterId);
        return true;
    }

    public void removeFriend(UUID removeId) { // only call this method if you are using the /friend remove command or you 100% know what you are doing
        this.friends.remove(removeId);
    }
}
