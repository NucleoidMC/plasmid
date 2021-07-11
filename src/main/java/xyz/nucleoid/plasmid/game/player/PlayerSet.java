package xyz.nucleoid.plasmid.game.player;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.util.PlayerRef;

import java.util.Collections;
import java.util.Iterator;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Represents a set of {@link ServerPlayerEntity} on a server. These players are not guaranteed to be currently online,
 * but all functionality will operate only on currently online players.
 * <p>
 * Can be iterated, and additionally implements {@link PlayerOps} which allows for quickly applying various operations
 * to all players within the set such as sending a message.
 *
 * @see MutablePlayerSet
 */
public interface PlayerSet extends PlayerOps, Iterable<ServerPlayerEntity> {
    PlayerSet EMPTY = new PlayerSet() {
        @Override
        public boolean contains(UUID id) {
            return false;
        }

        @Override
        @Nullable
        public ServerPlayerEntity getEntity(UUID id) {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public MutablePlayerSet copy(MinecraftServer server) {
            return new MutablePlayerSet(server);
        }

        @NotNull
        @Override
        public Iterator<ServerPlayerEntity> iterator() {
            return Collections.emptyIterator();
        }

        @Override
        public Iterable<UUID> uuids() {
            return Collections.emptyList();
        }

        @Override
        public Iterable<PlayerRef> playerRefs() {
            return Collections.emptyList();
        }
    };

    /**
     * Queries whether this {@link PlayerSet} contains the given player {@link UUID}.
     * This will return {@code true} for players that are included in the {@link PlayerSet} even if they are not online.
     *
     * @param id the player uuid to query
     * @return {@code true} if this player {@link UUID} is contained within this {@link PlayerSet}
     */
    boolean contains(UUID id);

    /**
     * Queries whether this {@link PlayerSet} contains the given {@link PlayerRef}.
     * This will return {@code true} for players that are included in the {@link PlayerSet} even if they are not online.
     *
     * @param ref the {@link PlayerRef} to query
     * @return {@code true} if this {@link PlayerRef} is contained within this {@link PlayerSet}
     */
    default boolean contains(PlayerRef ref) {
        return this.contains(ref.id());
    }

    /**
     * Queries whether this {@link PlayerSet} contains the given {@link ServerPlayerEntity}.
     *
     * @param player the {@link ServerPlayerEntity} to query
     * @return {@code true} if this {@link ServerPlayerEntity} is contained within this {@link PlayerSet}
     */
    default boolean contains(ServerPlayerEntity player) {
        return this.contains(player.getUuid());
    }

    /**
     * Looks up a corresponding online {@link ServerPlayerEntity} that is contained within this {@link PlayerSet}
     * given a player {@link UUID}.
     *
     * @param id the id to look up in this set
     * @return the corresponding online {@link ServerPlayerEntity}, or {@code null} if not contained or offline
     */
    @Nullable
    ServerPlayerEntity getEntity(UUID id);

    /**
     * Returns the number of players contained within this {@link PlayerSet}, including offline players.
     *
     * @return the number of players in this {@link PlayerSet}
     */
    int size();

    /**
     * Returns whether this {@link PlayerSet} is empty (including offline players).
     *
     * @return {@code true} if this {@link PlayerSet} is empty
     */
    default boolean isEmpty() {
        return this.size() <= 0;
    }

    /**
     * Creates a mutable copy of this {@link PlayerSet}.
     *
     * @param server the {@link MinecraftServer} instance that these players exist within
     * @return a mutable copy of this {@link PlayerSet}
     */
    MutablePlayerSet copy(MinecraftServer server);

    /**
     * @return an iterator over the online {@link ServerPlayerEntity} within this {@link PlayerSet}
     */
    @Override
    Iterator<ServerPlayerEntity> iterator();

    /**
     * @return a stream of online {@link ServerPlayerEntity} within this {@link PlayerSet}
     */
    default Stream<ServerPlayerEntity> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    Iterable<UUID> uuids();

    Iterable<PlayerRef> playerRefs();

    @Override
    default void sendPacket(Packet<?> packet) {
        for (var player : this) {
            player.networkHandler.sendPacket(packet);
        }
    }

    @Override
    default void sendMessage(Text message) {
        for (var player : this) {
            player.sendMessage(message, false);
        }
    }

    @Override
    default void showTitle(Text title, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        this.sendPacket(new TitleFadeS2CPacket(fadeInTicks, stayTicks, fadeOutTicks));
        this.sendPacket(new TitleS2CPacket(title));
    }

    @Override
    default void showTitle(Text title, Text subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        this.sendPacket(new TitleFadeS2CPacket(fadeInTicks, stayTicks, fadeOutTicks));
        this.sendPacket(new TitleS2CPacket(title));
        this.sendPacket(new SubtitleS2CPacket(subtitle));
    }

    @Override
    default void sendActionBar(Text message) {
        for (var player : this) {
            player.sendMessage(message, true);
        }
    }

    @Override
    default void sendActionBar(Text message, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        this.sendPacket(new TitleFadeS2CPacket(fadeInTicks, stayTicks, fadeOutTicks));
        this.sendPacket(new OverlayMessageS2CPacket(message));
    }

    @Override
    default void playSound(SoundEvent sound) {
        this.playSound(sound, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    default void playSound(SoundEvent sound, SoundCategory category, float volume, float pitch) {
        for (var player : this) {
            player.playSound(sound, category, volume, pitch);
        }
    }

    @Override
    default void addStatusEffect(StatusEffectInstance effect) {
        for (var player : this) {
            player.addStatusEffect(effect);
        }
    }
}
