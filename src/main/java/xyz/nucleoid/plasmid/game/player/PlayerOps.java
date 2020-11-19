package xyz.nucleoid.plasmid.game.player;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.Packet;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;

public interface PlayerOps {
    void sendPacket(Packet<?> packet);

    void sendMessage(Text message);

    void sendTitle(Text message);

    void sendTitle(Text message, int fadeInTicks, int stayTicks, int fadeOutTicks);

    void sendSound(SoundEvent sound);

    void sendSound(SoundEvent sound, SoundCategory category, float volume, float pitch);

    void addStatusEffect(StatusEffectInstance effect);
}
