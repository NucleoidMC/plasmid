package xyz.nucleoid.plasmid.mixin.chat;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.stimuli.Stimuli;

@Mixin(value = Player.class, priority = 600)
public class PlayerEntityMixin {

    @Inject(method = "getDisplayName", at = @At("TAIL"), cancellable = true)
    private void callDisplayNameEvent(CallbackInfoReturnable<Component> cir) {
        if (((Object) this) instanceof ServerPlayer player) {
            try (var invokers = Stimuli.select().forEntityAt(player, player.blockPosition())) {
                Component vanilla = cir.getReturnValue();
                Component output = invokers.get(GamePlayerEvents.DISPLAY_NAME).onDisplayNameCreation(player, vanilla, vanilla);
                if (!vanilla.equals(output)) {
                    cir.setReturnValue(output);
                }
            }
        }
    }
}
