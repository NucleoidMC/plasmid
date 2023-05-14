package xyz.nucleoid.plasmid.game.manager;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameResult;
import xyz.nucleoid.plasmid.game.GameSpacePlayers;
import xyz.nucleoid.plasmid.game.GameTexts;
import xyz.nucleoid.plasmid.game.player.MutablePlayerSet;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.isolation.PlayerManagerAccess;

import java.util.*;
import java.util.function.Consumer;

public final class ManagedGameSpacePlayers implements GameSpacePlayers {
    private final ManagedGameSpace space;
    final MutablePlayerSet set;
    private final Map<ServerPlayerEntity, Consumer<ServerPlayerEntity>> leaveHandlers = new HashMap<>();

    ManagedGameSpacePlayers(ManagedGameSpace space) {
        this.space = space;
        this.set = new MutablePlayerSet(space.getServer());
    }

    @Override
    public GameResult screenJoins(Collection<ServerPlayerEntity> players) {
        return this.space.screenJoins(players);
    }

    @Override
    public GameResult offer(OfferContext context) {
        var result = this.attemptOffer(context);

        if (result.isError()) {
            this.attemptGarbageCollection();
        }

        return result;
    }

    private GameResult attemptOffer(OfferContext context) {

        var player = context.player();
        if (this.set.contains(player)) {
            return GameResult.error(GameTexts.Join.alreadyJoined());
        }

        var offer = new PlayerOffer(player);
        var result = this.space.offerPlayer(offer);

        var reject = result.asReject();
        if (reject != null) {
            return GameResult.error(reject.reason());
        }

        var accept = result.asAccept();
        if (accept != null) {
            try {
                accept.applyJoin(player); //this must set all the player's properties, including world and position
                var playerManager = (PlayerManagerAccess)this.space.getServer().getPlayerManager();
                if(!this.space.getWorlds().contains(player.getWorld().getRegistryKey()))
                    return GameResult.error(GameTexts.Join.worldNotSet()); //ensure the player is in the correct world
                if(playerManager.plasmid$playerInstanceAlreadyExists(player))
                    return GameResult.error(GameTexts.Join.playerAlreadyExist()); //ensure the player instance we are using is not already in the player manager

                context.onApply().run(); //in the default implementation, it removes the player from the world where the player was before joining
                playerManager.plasmid$AddPlayerAndSendDefaultJoinPacket(player, context.sendFirstJoinPacket()); //add the player to the player manager and send the default join packet
                this.leaveHandlers.put(player, context.leaveHandler());
                this.set.add(player);
                this.space.onAddPlayer(player);

                return GameResult.ok();
            } catch (Throwable throwable) {
                return GameResult.error(GameTexts.Join.unexpectedError());
            }
        } else {
            return GameResult.error(GameTexts.Join.genericError());
        }
    }

    void attemptGarbageCollection() {
        if (this.set.isEmpty()) {
            this.space.close(GameCloseReason.GARBAGE_COLLECTED);
        }
    }

    @Override
    public boolean kick(ServerPlayerEntity player) {
        if (this.set.contains(player)) {
            this.leaveHandlers.remove(player).accept(player);
            this.space.onPlayerRemove(player);
            this.set.remove(player);
            this.attemptGarbageCollection();
            return true;
        } else {
            return false;
        }
    }


    public Consumer<ServerPlayerEntity> remove(ServerPlayerEntity player) {
        if (!this.set.contains(player)) {
            return null;
        }
        var leaveHandler = this.leaveHandlers.remove(player);

        this.space.onPlayerRemove(player);

        this.set.remove(player);

        this.attemptGarbageCollection();

        return leaveHandler;
    }

    void clear() {
        this.set.clear();
        this.leaveHandlers.clear();
    }

    @Override
    public boolean contains(UUID id) {
        return this.set.contains(id);
    }

    @Override
    @Nullable
    public ServerPlayerEntity getEntity(UUID id) {
        return this.set.getEntity(id);
    }

    @Override
    public int size() {
        return this.set.size();
    }

    @Override
    public @NotNull Iterator<ServerPlayerEntity> iterator() {
        return this.set.iterator();
    }
}
