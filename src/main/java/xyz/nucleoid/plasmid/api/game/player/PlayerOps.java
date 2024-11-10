package xyz.nucleoid.plasmid.api.game.player;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.packet.Packet;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;

/**
 * A utility interface that allows various operations to be applied to a group of players such as sending a message,
 * packet, or sound.
 *
 * @see PlayerSet
 */
public interface PlayerOps {
    /**
     * Sends a packet to all players associated with this {@link PlayerOps}.
     *
     * @param packet the packet to send
     */
    void sendPacket(Packet<?> packet);

    /**
     * Sends a chat message to all players associated with this {@link PlayerOps}.
     *
     * @param message the chat message to send
     */
    void sendMessage(Text message);

    /**
     * Displays a title to all players associated with this {@link PlayerOps}.
     *
     * @param title the title message to display
     * @param lengthTicks the amount of ticks this title should stay on screen for
     */
    default void showTitle(Text title, int lengthTicks) {
        this.showTitle(title, 10, lengthTicks, 10);
    }

    /**
     * Displays a title to all players associated with this {@link PlayerOps}.
     *
     * @param title the title message to display
     * @param fadeInTicks the amount of ticks it should take for this title to fade in
     * @param stayTicks the amount of ticks this title should remain on screen for
     * @param fadeOutTicks the amount of ticks it should take for this title to fade out
     */
    void showTitle(Text title, int fadeInTicks, int stayTicks, int fadeOutTicks);

    /**
     * Displays a title and subtitle to all players associated with this {@link PlayerOps}.
     *
     * @param title the title message to display
     * @param subtitle the subtitle message to display
     * @param fadeInTicks the amount of ticks it should take for this title to fade in
     * @param stayTicks the amount of ticks this title should remain on screen for
     * @param fadeOutTicks the amount of ticks it should take for this title to fade out
     */
    void showTitle(Text title, Text subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks);

    /**
     * Sends a message to the action bar of all players associated with this {@link PlayerOps}.
     *
     * @param message the action bar message to send
     */
    void sendActionBar(Text message);

    /**
     * Sends a message to the action bar of all players associated with this {@link PlayerOps}.
     *
     * @param message the action bar message to send
     * @param fadeInTicks the amount of ticks it should take for this message to fade in
     * @param stayTicks the amount of ticks this message should remain on screen for
     * @param fadeOutTicks the amount of ticks it should take for this message to fade out
     */
    void sendActionBar(Text message, int fadeInTicks, int stayTicks, int fadeOutTicks);

    /**
     * Plays a sound to all players associated with this {@link PlayerOps}.
     *
     * @param sound the sound to play
     */
    void playSound(SoundEvent sound);

    /**
     * Plays a sound to all players associated with this {@link PlayerOps}.
     *
     * @param sound the sound to play
     * @param category the sound category to associate this sound with
     * @param volume the volume of the sound to play (corresponds to how quickly the volume falls off with distance)
     * @param pitch the pitch of the sound being played
     */
    void playSound(SoundEvent sound, SoundCategory category, float volume, float pitch);

    /**
     * Adds a {@link StatusEffectInstance} to all players associated with this {@link PlayerOps}.
     *
     * @param effect the status effect to add
     */
    void addStatusEffect(StatusEffectInstance effect);
}
