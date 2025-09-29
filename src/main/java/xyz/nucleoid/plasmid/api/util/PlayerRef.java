package xyz.nucleoid.plasmid.api.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.GameSpace;

import java.util.UUID;
import java.util.function.Consumer;

public record PlayerRef(UUID id) {
    public static PlayerRef of(PlayerEntity player) {
        return new PlayerRef(player.getUuid());
    }

    public static PlayerRef of(GameProfile profile) {
        return new PlayerRef(profile.id());
    }

    public static PlayerRef ofUnchecked(UUID id) {
        return new PlayerRef(id);
    }

    @Nullable
    public ServerPlayerEntity getEntity(GameSpace gameSpace) {
        return gameSpace.getPlayers().getEntity(this.id);
    }

    @Nullable
    public ServerPlayerEntity getEntity(ServerWorld world) {
        return this.getEntity(world.getServer());
    }

    @Nullable
    public ServerPlayerEntity getEntity(MinecraftServer server) {
        return server.getPlayerManager().getPlayer(this.id);
    }

    public boolean isOnline(GameSpace gameSpace) {
        return this.getEntity(gameSpace) != null;
    }

    public boolean isOnline(ServerWorld world) {
        return this.getEntity(world) != null;
    }

    public boolean isOnline(MinecraftServer server) {
        return this.getEntity(server) != null;
    }

    public void ifOnline(GameSpace gameSpace, Consumer<ServerPlayerEntity> consumer) {
        ServerPlayerEntity player = this.getEntity(gameSpace);
        if (player != null) {
            consumer.accept(player);
        }
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
}
