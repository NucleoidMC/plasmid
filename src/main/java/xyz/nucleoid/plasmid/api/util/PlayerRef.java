package xyz.nucleoid.plasmid.api.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.GameSpace;

import java.util.UUID;
import java.util.function.Consumer;

public record PlayerRef(UUID id) {
    public static PlayerRef of(Player player) {
        return new PlayerRef(player.getUUID());
    }

    public static PlayerRef of(GameProfile profile) {
        return new PlayerRef(profile.id());
    }

    public static PlayerRef of(NameAndId nameAndId) {
        return new PlayerRef(nameAndId.id());
    }

    public static PlayerRef ofUnchecked(UUID id) {
        return new PlayerRef(id);
    }

    @Nullable
    public ServerPlayer getEntity(GameSpace gameSpace) {
        return gameSpace.getPlayers().getEntity(this.id);
    }

    @Nullable
    public ServerPlayer getEntity(ServerLevel world) {
        return this.getEntity(world.getServer());
    }

    @Nullable
    public ServerPlayer getEntity(MinecraftServer server) {
        return server.getPlayerList().getPlayer(this.id);
    }

    public boolean isOnline(GameSpace gameSpace) {
        return this.getEntity(gameSpace) != null;
    }

    public boolean isOnline(ServerLevel world) {
        return this.getEntity(world) != null;
    }

    public boolean isOnline(MinecraftServer server) {
        return this.getEntity(server) != null;
    }

    public void ifOnline(GameSpace gameSpace, Consumer<ServerPlayer> consumer) {
        ServerPlayer player = this.getEntity(gameSpace);
        if (player != null) {
            consumer.accept(player);
        }
    }

    public void ifOnline(ServerLevel world, Consumer<ServerPlayer> consumer) {
        ServerPlayer player = this.getEntity(world);
        if (player != null) {
            consumer.accept(player);
        }
    }

    public void ifOnline(MinecraftServer server, Consumer<ServerPlayer> consumer) {
        ServerPlayer player = this.getEntity(server);
        if (player != null) {
            consumer.accept(player);
        }
    }
}
