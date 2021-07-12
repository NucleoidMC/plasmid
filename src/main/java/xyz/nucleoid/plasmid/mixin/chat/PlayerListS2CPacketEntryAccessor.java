package xyz.nucleoid.plasmid.mixin.chat;

import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerListS2CPacket.Entry.class)
public interface PlayerListS2CPacketEntryAccessor {
    @Accessor("displayName")
    @Mutable
    void setDisplayName(Text name);
}
