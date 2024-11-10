package xyz.nucleoid.plasmid.api.game.player;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Represents an {@link Iterable} of {@link ServerPlayerEntity} on a server.
 *
 * @see PlayerSet
 * @see PlayerOps
 */
public interface PlayerIterable extends PlayerOps, Iterable<ServerPlayerEntity> {
    PlayerIterable EMPTY = Collections::emptyIterator;

    /**
     * @return an iterator over the online {@link ServerPlayerEntity} within this {@link PlayerIterable}
     */
    @Override
    Iterator<ServerPlayerEntity> iterator();

    /**
     * @return a stream of online {@link ServerPlayerEntity} within this {@link PlayerIterable}
     */
    default Stream<ServerPlayerEntity> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

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
            player.playSoundToPlayer(sound, category, volume, pitch);
        }
    }

    @Override
    default void addStatusEffect(StatusEffectInstance effect) {
        for (var player : this) {
            player.addStatusEffect(effect);
        }
    }
}
