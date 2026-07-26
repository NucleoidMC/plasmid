package xyz.nucleoid.plasmid.mixin.chat;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientboundPlayerInfoUpdatePacket.Entry.class)
public interface PlayerListS2CPacketEntryAccessor {
    @Accessor("displayName")
    @Mutable
    void setDisplayName(Component name);
}
