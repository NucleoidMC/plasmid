package xyz.nucleoid.plasmid.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

public final class PlayerRef {
    private final UUID id;

    private PlayerRef(UUID id) {
        this.id = id;
    }

    public static PlayerRef of(PlayerEntity player) {
        return new PlayerRef(player.getUuid());
    }

    public static PlayerRef of(GameProfile profile) {
        return new PlayerRef(profile.getId());
    }

    @Nullable
    public ServerPlayerEntity getEntity(ServerWorld world) {
        return this.getEntity(world.getServer());
    }

    @Nullable
    public ServerPlayerEntity getEntity(MinecraftServer server) {
        return server.getPlayerManager().getPlayer(this.id);
    }

    public boolean isOnline(ServerWorld world) {
        return this.getEntity(world) != null;
    }

    public boolean isOnline(MinecraftServer server) {
        return this.getEntity(server) != null;
    }

    public UUID id() {
        return this.id;
    }

    public void ifOnline(ServerWorld world, Consumer<ServerPlayerEntity> consumer) {
        ServerPlayerEntity player = this.getEntity(world);
        if (player != null) {
            consumer.accept(player);
        }
    }

    public void ifOnline(MinecraftServer server, Consumer<ServerPlayerEntity> consumer) {
        ServerPlayerEntity player = this.getEntity(server);
        if (player != null) {
            consumer.accept(player);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj instanceof PlayerRef) {
            return ((PlayerRef) obj).id.equals(this.id);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}
