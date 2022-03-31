package xyz.nucleoid.plasmid.game.common.rust;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.rust.network.connection.RustGameConnection;
import xyz.nucleoid.plasmid.game.common.rust.network.connection.RustSocketConnection;
import xyz.nucleoid.plasmid.game.common.rust.network.message.*;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class RustGame {
    private GameSpace gameSpace;
    private GameActivity activity;
    private ServerWorld world;

    private RustGameConnection connection;

    private final Set<UUID> deadPlayers = new ObjectOpenHashSet<>();

    private RustGame() {
    }

    public static CompletableFuture<RustGame> connect() {
        final RustGame game = new RustGame();

        return RustSocketConnection.connect(new InetSocketAddress(RustSocketConnection.DEFAULT_PORT), new RustGameConnection.Handler() {
            @Override
            public void acceptConnection() {
            }

            @Override
            public void acceptMessage(RustGameMessage message) {
                game.accept(message);
            }

            @Override
            public void acceptError(Throwable cause) {
            }

            @Override
            public void acceptClosed() {
            }
        }).thenApply(connection -> {
            game.connection = connection;
            return game;
        });
    }

    public void start(GameActivity activity, ServerWorld world) {
        this.gameSpace = activity.getGameSpace();
        this.activity = activity;
        this.world = world;

        this.onPlayersChange();

        activity.listen(GamePlayerEvents.ADD, player -> this.onPlayersChange());
        activity.listen(GamePlayerEvents.REMOVE, player -> this.onPlayersChange());

        activity.listen(PlayerDeathEvent.EVENT, (player, source) -> {
            this.deadPlayers.add(player.getUuid());
            this.send(new PlayerDie(player.getUuid()));
            player.changeGameMode(GameMode.SPECTATOR);
            return ActionResult.FAIL;
        });

        activity.listen(GameActivityEvents.DISABLE, () -> {
            this.connection.close();
        });
    }

    private void onPlayersChange() {
        final List<UUID> participants = this.gameSpace.getPlayers().stream()
                .map(Entity::getUuid)
                .filter(uuid -> !this.deadPlayers.contains(uuid))
                .toList();
        this.send(new SetParticipants(participants));
    }

    public void send(RustGameMessage message) {
        this.connection.send(message);
    }

    private void accept(RustGameMessage message) {
        final GameSpace gameSpace = this.gameSpace;
        if (gameSpace == null) {
            return;
        }

        if (message instanceof TeleportPlayer teleportPlayer) {
            final ServerPlayerEntity player = gameSpace.getPlayers().getEntity(teleportPlayer.player());
            if (player != null) {
                final Vec3f position = teleportPlayer.dest();
                player.teleport(this.world, position.getX(), position.getY(), position.getZ(), 0.0f, 0.0f);
            }
        } else if (message instanceof SetBlock setBlock) {
            final Block block = Registry.BLOCK.get(setBlock.block());
            this.world.setBlockState(setBlock.pos(), block.getDefaultState());
        } else if (message instanceof GiveItem giveItem) {
            final Item item = Registry.ITEM.get(giveItem.item());
            final ServerPlayerEntity player = gameSpace.getPlayers().getEntity(giveItem.player());
            if (player != null) {
                player.dropItem(new ItemStack(item, giveItem.quantity()), true);
            }
        }
    }
}
