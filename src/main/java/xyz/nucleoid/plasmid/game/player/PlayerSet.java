package xyz.nucleoid.plasmid.game.player;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
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
        public PlayerSet copy() {
            return EMPTY;
        }

        @NotNull
        @Override
        public Iterator<ServerPlayerEntity> iterator() {
            return Collections.emptyIterator();
        }
    };

    boolean contains(UUID id);

    @Nullable
    ServerPlayerEntity getEntity(UUID id);

    default boolean contains(PlayerRef ref) {
        return this.contains(ref.getId());
    }

    default boolean contains(ServerPlayerEntity player) {
        return this.contains(player.getUuid());
    }

    int size();

    default boolean isEmpty() {
        return this.size() <= 0;
    }

    // TODO: 0.5- return mutable
    PlayerSet copy();

    default Stream<ServerPlayerEntity> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    @Override
    default void sendPacket(Packet<?> packet) {
        for (ServerPlayerEntity player : this) {
            player.networkHandler.sendPacket(packet);
        }
    }

    @Override
    default void sendMessage(Text message) {
        for (ServerPlayerEntity player : this) {
            player.sendMessage(message, false);
        }
    }

    @Override
    default void sendTitle(Text message, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        this.sendPacket(new TitleS2CPacket(fadeInTicks, stayTicks, fadeOutTicks));
        this.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.TITLE, message));
    }

    @Override
    default void sendTitle(Text title, Text subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        this.sendPacket(new TitleS2CPacket(fadeInTicks, stayTicks, fadeOutTicks));
        this.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.TITLE, title));
        this.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.SUBTITLE, subtitle));
    }
    @Override
    default void sendActionbar(Text text, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        this.sendPacket(new TitleS2CPacket(fadeInTicks, stayTicks, fadeOutTicks));
        this.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.ACTIONBAR, text));
    }

    @Override
    default void sendSound(SoundEvent sound) {
        this.sendSound(sound, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    default void sendSound(SoundEvent sound, SoundCategory category, float volume, float pitch) {
        for (ServerPlayerEntity player : this) {
            player.playSound(sound, category, volume, pitch);
        }
    }

    @Override
    default void addStatusEffect(StatusEffectInstance effect) {
        for (ServerPlayerEntity player : this) {
            player.addStatusEffect(effect);
        }
    }
}
