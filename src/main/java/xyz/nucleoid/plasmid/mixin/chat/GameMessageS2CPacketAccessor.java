package xyz.nucleoid.plasmid.mixin.chat;

import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(GameMessageS2CPacket.class)
public interface GameMessageS2CPacketAccessor {

    @Accessor("message")
    Text getText();

    @Accessor("senderUuid")
    UUID getUUID();
}
