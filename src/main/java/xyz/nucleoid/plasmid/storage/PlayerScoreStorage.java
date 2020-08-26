package xyz.nucleoid.plasmid.storage;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A simple class for storing the scores of players.
 * 'Score' can mean any number.
 */
public class PlayerScoreStorage implements ServerStorage {
    public final Map<UUID, Float> scoreMap = new HashMap<>();

    /**
     * Puts a player's UUID and score onto the {@link #scoreMap}.
     * @param player - The player to assign the score for.
     * @param score - The score for the player.
     */
    public void putPlayerScore(ServerPlayerEntity player, float score) {
        this.scoreMap.put(player.getUuid(), score);
    }

    /**
     * Gets the player's score.
     * @param player - The player to get the score for.
     * @return - The player's score or 0 if the player isn't in the {@link #scoreMap}.
     */
    public float getPlayerScore(ServerPlayerEntity player) {
        return this.scoreMap.getOrDefault(player, 0.0F);
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        ListTag listTag = new ListTag();
        this.scoreMap.forEach((uuid, score) -> listTag.add(this.createPlayerScoreTag(uuid, score)));
        tag.put("Players", listTag);
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        ListTag listTag = tag.getList("Players", NbtType.COMPOUND);
        for (Tag compound : listTag) {
            CompoundTag playerTag = (CompoundTag) compound;
            this.scoreMap.put(playerTag.getUuid("UUID"), playerTag.getFloat("Score"));
        }
    }

    private CompoundTag createPlayerScoreTag(UUID uuid, float score) {
        CompoundTag tag = new CompoundTag();
        tag.putUuid("UUID", uuid);
        tag.putFloat("Score", score);
        return tag;
    }
}
