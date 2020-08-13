package xyz.nucleoid.plasmid.mixin.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.event.EntityDeathListener;
import xyz.nucleoid.plasmid.game.event.EntityDropLootListener;

import java.util.List;
import java.util.function.Consumer;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    private LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    private void callDeathListener(DamageSource source, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.world.isClient) {
            return;
        }

        GameWorld gameWorld = GameWorld.forWorld(entity.world);

        // validate world & only trigger if this entity is inside it
        if (gameWorld != null && gameWorld.containsEntity(entity)) {
            ActionResult result = gameWorld.invoker(EntityDeathListener.EVENT).onDeath(entity, source);

            // cancel death if FAIL was returned from any listener
            if (result == ActionResult.FAIL) {
                ci.cancel();
            }
        }
    }

    @Redirect(method = "dropLoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/LootTable;generateLoot(Lnet/minecraft/loot/context/LootContext;Ljava/util/function/Consumer;)V"))
    private void modifyDroppedLoot(LootTable lootTable, LootContext context, Consumer<ItemStack> lootConsumer) {
        List<ItemStack> droppedStacks = lootTable.generateLoot(context);

        // default stack dropping for client
        if (world.isClient) {
            droppedStacks.forEach(this::dropStack);
            return;
        }

        GameWorld gameWorld = GameWorld.forWorld(world);

        if(gameWorld != null && gameWorld.containsEntity((LivingEntity) (Object) this)) {
            TypedActionResult<List<ItemStack>> result = gameWorld.invoker(EntityDropLootListener.EVENT).onDropLoot((LivingEntity) (Object) this, droppedStacks);

            // drop potentially modified stacks from listeners
            if(result.getResult() != ActionResult.FAIL) {
                result.getValue().forEach(this::dropStack);
            }

            return;
        }

        // default stack dropping for non-gameworld on server
        droppedStacks.forEach(this::dropStack);
    }
}
