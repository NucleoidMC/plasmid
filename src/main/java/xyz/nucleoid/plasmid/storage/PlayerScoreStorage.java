package xyz.nucleoid.plasmid.storage;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerScoreStorage implements ServerStorage {
    public final Map<UUID, Float> scoreMap = new HashMap<>();

    public void putPlayerScore(ServerPlayerEntity player, float score) {
        this.scoreMap.put(player.getUuid(), score);
    }

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