package xyz.nucleoid.plasmid.game.player;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.util.PlayerRef;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface PlayerSet extends PlayerOps, Iterable<ServerPlayerEntity> {
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
    default void sendTitle(Text message) {
        this.sendTitle(message, 1, 5, 3);
    }

    @Override
    default void sendTitle(Text message, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        this.sendPacket(new TitleS2CPacket(fadeInTicks, stayTicks, fadeOutTicks));
        this.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.TITLE, message));
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
