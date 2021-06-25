package xyz.nucleoid.plasmid.game.portal;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.game.GameResult;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.GameTexts;
import xyz.nucleoid.plasmid.game.config.CustomValuesConfig;
import xyz.nucleoid.plasmid.party.PartyManager;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class GamePortal {
    private final MinecraftServer server;
    private final Identifier id;
    private final GamePortalBackend backend;
    private CustomValuesConfig custom = CustomValuesConfig.empty();

    private final Set<GamePortalInterface> interfaces = new ObjectOpenHashSet<>();

    private GamePortalDisplay lastDisplay = new GamePortalDisplay();
    private GamePortalDisplay currentDisplay = new GamePortalDisplay();

    public GamePortal(MinecraftServer server, Identifier id, GamePortalBackend.Factory backendFactory) {
        this.server = server;
        this.id = id;
        this.backend = backendFactory.create(server, id);
    }

    void setCustom(CustomValuesConfig custom) {
        this.custom = custom;
    }

    public Identifier getId() {
        return this.id;
    }

    public CustomValuesConfig getCustom() {
        return this.custom;
    }

    public void requestJoin(ServerPlayerEntity player) {
        PartyManager partyManager = PartyManager.get(player.server);
        Collection<ServerPlayerEntity> players = partyManager.getPartyMembers(player);

        this.requestJoinAll(player, players)
                .thenAcceptAsync(results -> {
                    this.handleJoinResults(player, results);
                }, player.server);
    }

    private CompletableFuture<JoinResults> requestJoinAll(ServerPlayerEntity primaryPlayer, Collection<ServerPlayerEntity> players) {
        return CompletableFuture.supplyAsync(() -> this.backend.requestJoin(primaryPlayer))
                .thenCompose(Function.identity())
                .thenApplyAsync(gameSpace -> this.tryJoinAll(players, gameSpace), this.server)
                .exceptionally(this::handleJoinException);
    }

    private JoinResults tryJoinAll(Collection<ServerPlayerEntity> players, GameSpace gameSpace) {
        JoinResults results = new JoinResults();

        GameResult screenResult = gameSpace.screenPlayerJoins(players);
        if (screenResult.isError()) {
            results.globalError = screenResult.getError();
            return results;
        }

        for (ServerPlayerEntity player : players) {
            GameResult result = gameSpace.offerPlayer(player);
            if (result.isError()) {
                results.playerErrors.put(player, result.getError());
            }
        }

        return results;
    }

    private JoinResults handleJoinException(Throwable throwable) {
        JoinResults results = new JoinResults();
        results.globalError = this.getFeedbackForException(throwable);
        return results;
    }

    private void handleJoinResults(ServerPlayerEntity player, JoinResults results) {
        if (results.globalError != null) {
            player.sendMessage(results.globalError.shallowCopy().formatted(Formatting.RED), false);
        } else if (!results.playerErrors.isEmpty()) {
            player.sendMessage(
                    GameTexts.Join.partyJoinError(results.playerErrors.size()).formatted(Formatting.RED),
                    false
            );

            for (Map.Entry<ServerPlayerEntity, Text> entry : results.playerErrors.entrySet()) {
                Text error = entry.getValue().shallowCopy().formatted(Formatting.RED);
                entry.getKey().sendMessage(error, false);
            }
        }
    }

    private Text getFeedbackForException(Throwable throwable) {
        GameOpenException gameOpenException = GameOpenException.unwrap(throwable);
        if (gameOpenException != null) {
            return gameOpenException.getReason().shallowCopy();
        } else {
            return GameTexts.Join.unexpectedError();
        }
    }

    public boolean addInterface(GamePortalInterface itf) {
        if (itf.getPortal() == null && this.interfaces.add(itf)) {
            itf.setPortal(this);
            itf.setDisplay(this.currentDisplay);
            return true;
        }
        return false;
    }

    public boolean removeInterface(GamePortalInterface itf) {
        if (this.interfaces.remove(itf)) {
            itf.invalidatePortal();
            return true;
        }
        return false;
    }

    public void invalidate() {
        for (GamePortalInterface itf : this.interfaces) {
            itf.invalidatePortal();
        }
        this.interfaces.clear();
    }

    void updateDisplay() {
        this.flipDisplay();

        GamePortalDisplay display = this.currentDisplay;
        this.backend.populateDisplay(display);

        if (!display.equals(this.lastDisplay)) {
            for (GamePortalInterface itf : this.interfaces) {
                itf.setDisplay(display);
            }
        }
    }

    void flipDisplay() {
        GamePortalDisplay swap = this.currentDisplay;
        this.currentDisplay = this.lastDisplay;
        this.lastDisplay = swap;

        this.currentDisplay.clear();
    }

    static class JoinResults {
        Text globalError;
        final Map<ServerPlayerEntity, Text> playerErrors = new Object2ObjectOpenHashMap<>();
    }
}
