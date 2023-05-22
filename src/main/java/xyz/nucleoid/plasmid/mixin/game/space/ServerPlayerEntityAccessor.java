package xyz.nucleoid.plasmid.mixin.game.space;

import net.minecraft.network.encryption.PublicPlayerSession;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayerEntity.class)
public interface ServerPlayerEntityAccessor {

    @Accessor
    PublicPlayerSession getSession();

    @Accessor
    void setSession(PublicPlayerSession session);
}
