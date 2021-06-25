package xyz.nucleoid.plasmid.game.player;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.Packet;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;

public interface PlayerOps {
    void sendPacket(Packet<?> packet);

    void sendMessage(Text message);

    default void sendTitle(Text message) {
        this.sendTitle(message, 10, 40, 10);
    }

    void sendTitle(Text message, int fadeInTicks, int stayTicks, int fadeOutTicks);

    void sendTitle(Text title, Text subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks);

    void sendActionBar(Text message);

    void sendActionBar(Text message, int fadeInTicks, int stayTicks, int fadeOutTicks);

    void playSound(SoundEvent sound);

    void playSound(SoundEvent sound, SoundCategory category, float volume, float pitch);

    void addStatusEffect(StatusEffectInstance effect);
}
