package xyz.nucleoid.plasmid.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/*
    Unless you know what you are doing please do not touch this class TYSM || this is to (hopefully) prevent anything from breaking :P

    if you have a legit reason for using this and want help ask I ran out of name ideas on the discord server

 */
public class FriendListManager {
    public static Map<UUID, FriendList> fArrays = new HashMap<UUID, FriendList>();

    public static void appendNewFriendList(UUID playerUUID, FriendList fList) {
        System.out.println("added");
        if (fArrays.containsValue(playerUUID)) {
            fArrays.computeIfPresent(playerUUID, (k, v) -> v = fList);
        } else {
            fArrays.put(playerUUID, fList);
        }
        System.out.println("added");
    }

    public static FriendList returnFriendlist(UUID playerUUID) {
        System.out.println("returning");
        return fArrays.get(playerUUID);
    }

    public static void removeFriendList(UUID OwnerUUID) {
        System.out.println("returning");
        if (fArrays.containsValue(OwnerUUID)) {
            fArrays.remove(OwnerUUID);
        }
        System.out.println("returning");
    }
}
