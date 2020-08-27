package xyz.nucleoid.plasmid.mixin.chat;

import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TitleS2CPacket.class)
public interface TitleS2CPacketAccessor {

    @Accessor("text")
    Text getMessage();

    @Accessor("action")
    TitleS2CPacket.Action getTitleAction();

    @Accessor("fadeInTicks")
    int getFadeIn();

    @Accessor("stayTicks")
    int getStay();

    @Accessor("fadeOutTicks")
    int getFadeOut();
}
