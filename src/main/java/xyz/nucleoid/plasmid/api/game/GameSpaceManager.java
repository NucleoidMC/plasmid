package xyz.nucleoid.plasmid.api.game;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.impl.manager.GameSpaceManagerImpl;
import xyz.nucleoid.plasmid.impl.manager.ManagedGameSpace;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@ApiStatus.NonExtendable
public interface GameSpaceManager {
    static GameSpaceManagerImpl get() {
        return GameSpaceManagerImpl.get();
    }

    CompletableFuture<GameSpace> open(RegistryEntry<GameConfig<?>> config);

    Collection<ManagedGameSpace> getOpenGameSpaces();

    @Nullable
    GameSpace byId(UUID id);

    @Nullable
    GameSpace byUserId(Identifier userId);

    @Nullable
    GameSpace byWorld(World world);

    @Nullable
    GameSpace byPlayer(PlayerEntity player);

    boolean hasGame(World world);

    boolean inGame(PlayerEntity player);
}
