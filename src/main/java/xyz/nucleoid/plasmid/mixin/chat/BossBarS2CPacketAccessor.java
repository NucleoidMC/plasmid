package xyz.nucleoid.plasmid.mixin.chat;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(BossBarS2CPacket.class)
public interface BossBarS2CPacketAccessor {

    @Accessor("uuid")
    UUID getUUID();

    @Accessor("type")
    BossBarS2CPacket.Type getBarType();

    @Accessor("name")
    Text getMessage();

    @Accessor("percent")
    float getBarPercent();

    @Accessor("color")
    BossBar.Color getBarColor();

    @Accessor("overlay")
    BossBar.Style getBarOverlay();

    @Accessor("darkenSky")
    boolean barShouldDarkenSky();

    @Accessor("dragonMusic")
    boolean barHasDragonMusic();

    @Accessor("thickenFog")
    boolean barShouldThickenFog();
}
