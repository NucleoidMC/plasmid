package xyz.nucleoid.plasmid.mixin.game.rule;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.event.DropItemListener;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    private PlayerEntityMixin(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
    }

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void isInvulnerableTo(DamageSource source, CallbackInfoReturnable<Boolean> ci) {
        if (this.world.isClient || source != DamageSource.FALL) {
            return;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        GameWorld gameWorld = GameWorld.forWorld(this.world);
        if (gameWorld != null && gameWorld.containsPlayer(player)) {
            RuleResult result = gameWorld.testRule(GameRule.FALL_DAMAGE);
            if (result == RuleResult.ALLOW) {
                ci.setReturnValue(false);
            } else if (result == RuleResult.DENY) {
                ci.setReturnValue(true);
            }
        }
    }

    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    private void dropSelectedItem(boolean dropEntireStack, CallbackInfoReturnable<Boolean> ci) {
        if (this.world.isClient) {
            return;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        GameWorld gameWorld = GameWorld.forWorld(this.world);
        if (gameWorld != null && gameWorld.containsPlayer(player)) {

            int slot = player.inventory.selectedSlot;
            ItemStack stack = player.inventory.getStack(slot);

            // Check the gamerule first
            RuleResult ruleResult = gameWorld.testRule(GameRule.THROW_ITEMS);
            boolean shouldCancel = ruleResult == RuleResult.DENY;

            // If the gamerule doesn't cancel it, check the event
            if (!shouldCancel) {
                ActionResult dropResult = gameWorld.invoker(DropItemListener.EVENT).onDrop((PlayerEntity) (Object) this, slot, stack);
                shouldCancel = dropResult == ActionResult.FAIL;
            }

            if (shouldCancel) {
                player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, slot, stack));

                ci.setReturnValue(false);
            }
        }
    }
}
