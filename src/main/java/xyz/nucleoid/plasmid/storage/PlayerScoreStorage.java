package xyz.nucleoid.plasmid.storage;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

/**
 * A simple class for storing the scores of players.
 */
public class PlayerScoreStorage implements ServerStorage {
    public final Object2IntMap<UUID> scoreMap = new Object2IntArrayMap<>();

    /**
     * Puts a player's UUID and score onto the {@link #scoreMap}.
     * @param player - The player to assign the score for.
     * @param score - The score for the player.
     */
    public void putPlayerScore(ServerPlayerEntity player, int score) {
        this.scoreMap.put(player.getUuid(), score);
    }

    /**
     * Gets the player's score.
     * @param player - The player to get the score for.
     * @return - The player's score or 0 if the player isn't in the {@link #scoreMap}.
     */
    public int getPlayerScore(ServerPlayerEntity player) {
        return this.scoreMap.getOrDefault(player, 0);
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
            this.scoreMap.put(playerTag.getUuid("UUID"), playerTag.getInt("Score"));
        }
    }

    private CompoundTag createPlayerScoreTag(UUID uuid, int score) {
        CompoundTag tag = new CompoundTag();
        tag.putUuid("UUID", uuid);
        tag.putInt("Score", score);
        return tag;
    }
}
