package xyz.nucleoid.plasmid.util;

import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.player.PlayerSet;

/**
 * @deprecated in favour of {@link PlayerSet}, which can be accessed through the {@link GameWorld}
 */
@Deprecated
public final class BroadcastUtils {
    public static void broadcastTitle(Text message, GameWorld world) {
        world.getPlayerSet().sendTitle(message);
    }

    public static void broadcastMessage(Text message, GameWorld world) {
        world.getPlayerSet().sendMessage(message);
    }

    public static void broadcastSound(SoundEvent sound, float pitch, GameWorld world) {
        world.getPlayerSet().sendSound(sound, SoundCategory.PLAYERS, 1.0F, pitch);
    }

    public static void broadcastSound(SoundEvent sound, GameWorld world) {
        broadcastSound(sound, 1.0f, world);
    }
}
