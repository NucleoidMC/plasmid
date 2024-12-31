package xyz.nucleoid.plasmid.impl.portal.backend;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.api.game.GameResult;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.api.game.player.GamePlayerJoiner;
import xyz.nucleoid.plasmid.api.game.player.JoinIntent;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public interface PortalUserContext {
    static PortalUserContext of(ServerPlayerEntity serverPlayer) {
        return new Player(serverPlayer);
    }

    void openUi(Consumer<ServerPlayerEntity> guiOpener);

    boolean canJoinExisting();

    GameResult tryJoin(GameSpace gameSpace, JoinIntent joinIntent);

    void tryOpening(RegistryEntry<GameConfig<?>> game);

    record Player(ServerPlayerEntity player) implements PortalUserContext {
        @Override
        public void openUi(Consumer<ServerPlayerEntity> guiOpener) {
            guiOpener.accept(this.player);
        }

        @Override
        public boolean canJoinExisting() {
            return true;
        }

        @Override
        public GameResult tryJoin(GameSpace gameSpace, JoinIntent joinIntent) {
            return GamePlayerJoiner.tryJoin(this.player, gameSpace, joinIntent);
        }

        @Override
        public void tryOpening(RegistryEntry<GameConfig<?>> game) {
            CompletableFuture.supplyAsync(() -> GameSpaceManagerImpl.get().open(game))
                    .thenCompose(Function.identity())
                    .handleAsync((gameSpace, throwable) -> {
                        GameResult result;
                        if (gameSpace != null) {
                            result = GamePlayerJoiner.tryJoin(this.player, gameSpace, JoinIntent.PLAY);
                        } else {
                            result = GamePlayerJoiner.handleJoinException(throwable);
                        }

                        if (result.isError()) {
                            this.player.sendMessage(result.errorCopy().formatted(Formatting.RED), false);
                        }

                        return null;
                    }, this.player.server);
        }
    }
}
