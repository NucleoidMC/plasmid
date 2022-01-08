package xyz.nucleoid.plasmid.storage;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
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
    public NbtCompound writeNbt() {
        var nbt = new NbtCompound();
        var list = new NbtList();
        this.scoreMap.forEach((uuid, score) -> list.add(this.createPlayerScoreNbt(uuid, score)));
        nbt.put("Players", list);
        return nbt;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        var list = nbt.getList("Players", NbtType.COMPOUND);
        for (NbtElement element : list) {
            NbtCompound compound = (NbtCompound) element;
            this.scoreMap.put(compound.getUuid("UUID"), compound.getInt("Score"));
        }
    }

    private NbtCompound createPlayerScoreNbt(UUID uuid, int score) {
        var nbt = new NbtCompound();
        nbt.putUuid("UUID", uuid);
        nbt.putInt("Score", score);
        return nbt;
    }
}
