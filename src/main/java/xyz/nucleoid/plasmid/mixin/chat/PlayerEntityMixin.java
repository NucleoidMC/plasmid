package xyz.nucleoid.plasmid.mixin.chat;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.stimuli.Stimuli;

@Mixin(value = PlayerEntity.class, priority = 600)
public class PlayerEntityMixin {

    @Inject(method = "getDisplayName", at = @At("TAIL"), cancellable = true)
    private void callDisplayNameEvent(CallbackInfoReturnable<Text> cir) {
        if (((Object) this) instanceof ServerPlayerEntity player) {
            try (var invokers = Stimuli.select().forEntityAt(player, player.getBlockPos())) {
                Text vanilla = cir.getReturnValue();
                Text output = invokers.get(GamePlayerEvents.DISPLAY_NAME).onDisplayNameCreation(player, vanilla, vanilla);
                if (!vanilla.equals(output)) {
                    cir.setReturnValue(output);
                }
            }
        }
    }
}
