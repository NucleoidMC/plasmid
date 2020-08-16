package xyz.nucleoid.plasmid.util;

import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.GameWorld;

public class BroadcastUtils {

    public static void broadcastTitle(Text message, GameWorld world) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            player.networkHandler.sendPacket(new TitleS2CPacket(1, 5,  3));
            player.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.TITLE, message));
        }
    }

    public static void broadcastMessage(Text message, GameWorld world) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            player.sendMessage(message, false);
        };
    }

    public static void broadcastSound(SoundEvent sound, float pitch, GameWorld world) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            player.playSound(sound, SoundCategory.PLAYERS, 1.0F, pitch);
        };
    }

    public static void broadcastSound(SoundEvent sound,  GameWorld world) {
        broadcastSound(sound, 1.0f, world);
    }

}
