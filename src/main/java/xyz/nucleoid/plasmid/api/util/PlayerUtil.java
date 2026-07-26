package xyz.nucleoid.plasmid.api.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class PlayerUtil {
    private PlayerUtil() {}

    public static void playSoundToPlayer(Player player, SoundEvent soundEvent, SoundSource category, float volume, float pitch) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSoundEntityPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEvent), category, player, volume, pitch, player.getRandom().nextLong()));
        }
    }
}
