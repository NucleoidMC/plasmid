package xyz.nucleoid.plasmid.api.game;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@ApiStatus.NonExtendable
public interface GameSpaceManager {
    static GameSpaceManager get() {
        return GameSpaceManagerImpl.get();
    }

    CompletableFuture<GameSpace> open(Holder<GameConfig<?>> config);

    Collection<GameSpace> getOpenGameSpaces();

    @Nullable
    GameSpace byId(UUID id);

    @Nullable
    GameSpace byUserId(Identifier userId);

    @Nullable
    GameSpace byLevel(Level level);

    @Nullable
    GameSpace byPlayer(Player player);

    boolean hasGame(Level level);

    boolean inGame(Player player);
}
