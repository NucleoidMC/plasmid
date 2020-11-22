package xyz.nucleoid.plasmid.storage;

import java.util.ArrayList;
import java.util.UUID;
/*
    Please only touch this if you know what you are doing
 */

public class FriendList {
    private int playerId;
    private final ArrayList<UUID> friends = new ArrayList<UUID>(); // holds accepted friends
    private final ArrayList<UUID> requests = new ArrayList<UUID>(); // holds friend requests

    public ArrayList<UUID> returnFlistIds() {
        return this.friends;
    }

    public int returnOwnerId() {
        return this.playerId;
    }

    public ArrayList<UUID> returnRequestList() {
        return this.requests;
    }

    public boolean requestListContains(UUID targetId) {
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

    public boolean addRequests(UUID requesterId) {
        if (this.requests.contains(requesterId)) {
            return false;
        }
        this.requests.add(requesterId);
        return true;
    }

    public void removeFriend(UUID removeId) {
        this.friends.remove(removeId);
    }
}
