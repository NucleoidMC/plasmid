package xyz.nucleoid.plasmid.storage;

import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FriendList {
    private int playerId;
    private ArrayList<UUID> friends = new ArrayList<UUID>();
    public ArrayList<UUID> returnFlistIds() {
        return this.friends;
    }
    public int returnOwnerId() {
        return this.playerId;
    }

    public void addFreind(UUID addedFriend) {
        if (this.friends.contains(addedFriend)) {
            System.out.println("no adding");
            return;
        }else{
            this.friends.add(addedFriend);
            System.out.println("added");
        }
        for (UUID ids: this.friends) {
            System.out.println(ids);
        }
    }
    public void removeFriend(UUID removeId) {
        this.friends.remove(removeId);
    }
}
