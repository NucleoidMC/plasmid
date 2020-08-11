package xyz.nucleoid.plasmid.mixin.custom;

import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.event.HandSwingListener;
import xyz.nucleoid.plasmid.item.CustomItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onHandSwing", at = @At("HEAD"))
    private void onHandSwing(HandSwingC2SPacket packet, CallbackInfo ci) {
        Hand hand = packet.getHand();

        ItemStack stack = this.player.getStackInHand(hand);
        CustomItem custom = CustomItem.match(stack);
        if (custom != null) {
            custom.onSwingHand(this.player, hand);
        }

        GameWorld gameWorld = GameWorld.forWorld(this.player.world);
        if (gameWorld != null && gameWorld.containsPlayer(this.player)) {
            gameWorld.invoker(HandSwingListener.EVENT).onSwingHand(this.player, hand);
        }
    }
}
