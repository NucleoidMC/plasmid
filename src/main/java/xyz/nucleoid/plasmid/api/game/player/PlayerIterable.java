package xyz.nucleoid.plasmid.api.game.player;

import xyz.nucleoid.plasmid.api.util.PlayerUtil;

import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * Represents an {@link Iterable} of {@link ServerPlayer} on a server.
 *
 * @see PlayerSet
 * @see PlayerOps
 */
public interface PlayerIterable extends PlayerOps, Iterable<ServerPlayer> {
    PlayerIterable EMPTY = Collections::emptyIterator;

    /**
     * @return an iterator over the online {@link ServerPlayer} within this {@link PlayerIterable}
     */
    @Override
    Iterator<ServerPlayer> iterator();

    /**
     * @return a stream of online {@link ServerPlayer} within this {@link PlayerIterable}
     */
    default Stream<ServerPlayer> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    @Override
    default void sendPacket(Packet<?> packet) {
        for (var player : this) {
            player.connection.send(packet);
        }
    }

    @Override
    default void sendMessage(Component message) {
        for (var player : this) {
            player.sendSystemMessage(message, false);
        }
    }

    @Override
    default void showTitle(Component title, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        this.sendPacket(new ClientboundSetTitlesAnimationPacket(fadeInTicks, stayTicks, fadeOutTicks));
        this.sendPacket(new ClientboundSetTitleTextPacket(title));
    }

    @Override
    default void showTitle(Component title, Component subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        this.sendPacket(new ClientboundSetTitlesAnimationPacket(fadeInTicks, stayTicks, fadeOutTicks));
        this.sendPacket(new ClientboundSetTitleTextPacket(title));
        this.sendPacket(new ClientboundSetSubtitleTextPacket(subtitle));
    }

    @Override
    default void sendActionBar(Component message) {
        for (var player : this) {
            player.sendSystemMessage(message, true);
        }
    }

    @Override
    default void sendActionBar(Component message, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        this.sendPacket(new ClientboundSetTitlesAnimationPacket(fadeInTicks, stayTicks, fadeOutTicks));
        this.sendPacket(new ClientboundSetActionBarTextPacket(message));
    }

    @Override
    default void playSound(SoundEvent sound) {
        this.playSound(sound, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    default void playSound(SoundEvent sound, SoundSource category, float volume, float pitch) {
        for (var player : this) {
            PlayerUtil.playSoundToPlayer(player, sound, category, volume, pitch);
        }
    }

    @Override
    default void addStatusEffect(MobEffectInstance effect) {
        for (var player : this) {
            player.addEffect(effect);
        }
    }
}
