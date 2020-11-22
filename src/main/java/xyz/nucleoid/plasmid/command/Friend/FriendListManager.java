package xyz.nucleoid.plasmid.command.Friend;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/*
    Unless you know what you are doing please do not touch this class TYSM || this is to (hopefully) prevent anything from breaking :P

    if you have a legit reason for using this and want help ask I ran out of name ideas on the discord server

 */
public class FriendListManager {
    public static Map<UUID, FriendList> fArrays = new HashMap<UUID, FriendList>(); // manages all payers friend lists

    public static void appendNewFriendList(UUID playerUUID, FriendList fList) { // add new friend list
        if (fArrays.containsValue(playerUUID)) {
            fArrays.computeIfPresent(playerUUID, (k, v) -> v = fList);
        } else {
            fArrays.put(playerUUID, fList);
        }
    }

    public static FriendList returnFriendlist(UUID playerUUID) { // return friend list of a player
        return fArrays.get(playerUUID);
    }

    public static void removeFriendList(UUID OwnerUUID) { // remove friend list CAUTION ONY CALL THIS METHOD IF THE PLAYER IS DISCONNECTING OR THEY ARE PERM BANNED
        if (fArrays.containsValue(OwnerUUID)) {
            fArrays.remove(OwnerUUID);
        }
    }
}
