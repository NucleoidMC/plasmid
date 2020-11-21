package xyz.nucleoid.plasmid.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FriendListManager {
    public static Map<UUID, FriendList> fArrays = new HashMap<UUID, FriendList>();

    public static void appendNewFreindList(UUID playerUUID, FriendList fList) {
        System.out.println("added");
        if (fArrays.containsValue(playerUUID)) {
            fArrays.computeIfPresent(playerUUID, (k, v) -> v = fList);
        } else {
            fArrays.put(playerUUID, fList);
        }
        System.out.println("added");
    }

    public static FriendList returnFlist(UUID playerUUID) {
        System.out.println("returning");
        return fArrays.get(playerUUID);
    }

    public static void removeFreindList(UUID OwnerUUID) {
        System.out.println("returning");
        if (fArrays.containsValue(OwnerUUID)) {
            fArrays.remove(OwnerUUID);
        }
        System.out.println("returning");
    }
}
