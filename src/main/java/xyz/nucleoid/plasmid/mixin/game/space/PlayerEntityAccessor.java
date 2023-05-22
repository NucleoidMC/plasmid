package xyz.nucleoid.plasmid.mixin.game.space;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerEntity.class)
public interface PlayerEntityAccessor
{
    @Accessor("PLAYER_MODEL_PARTS")
    TrackedData<Byte> playerModelParts();
}
