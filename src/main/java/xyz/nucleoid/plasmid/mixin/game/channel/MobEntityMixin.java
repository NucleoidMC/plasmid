package xyz.nucleoid.plasmid.mixin.game.channel;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.entity.FloatingText;
import xyz.nucleoid.plasmid.game.channel.GameChannelInterface;
import xyz.nucleoid.plasmid.game.channel.GameChannel;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity implements GameChannelInterface {
    private FloatingText display;
    private GameChannel channel;
    private Identifier loadedChannel;

    private MobEntityMixin(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
    }

    @Override
    public void setChannel(GameChannel channel) {
        this.channel = channel;
        if (channel == null && this.display != null) {
            this.display.remove();
            this.display = null;
        }
    }

    @Nullable
    @Override
    public GameChannel getChannel() {
        return this.channel;
    }

    @Override
    public void setPos(double x, double y, double z) {
        Vec3d pos = this.getPos();
        if (pos.x == x && pos.y == y && pos.z == z) {
            return;
        }

        super.setPos(x, y, z);

        if (this.display != null) {
            this.display.setPos(this.getDisplayAnchor());
        }
    }

    @Override
    public void setDisplay(Text[] display) {
        if (display.length > 0) {
            FloatingText floatingText = this.createDisplay();
            floatingText.setText(display);
        } else {
            this.removeDisplay();
        }
    }

    private FloatingText createDisplay() {
        if (this.display == null) {
            Vec3d anchor = this.getDisplayAnchor();
            this.display = FloatingText.create((ServerWorld) this.world, anchor, FloatingText.VerticalAlign.BOTTOM);
        }
        return this.display;
    }

    private Vec3d getDisplayAnchor() {
        return this.getPos().add(0.0, this.getHeight(), 0.0);
    }

    private void removeDisplay() {
        if (this.display != null) {
            this.display.remove();
            this.display = null;
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (this.loadedChannel != null) {
            this.tryConnectTo(this.world.getServer(), this.loadedChannel);
            this.loadedChannel = null;
        }
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void onInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> ci) {
        if (this.channel != null && player instanceof ServerPlayerEntity && hand == Hand.MAIN_HAND) {
            this.channel.requestJoin((ServerPlayerEntity) player);
            ci.setReturnValue(ActionResult.SUCCESS);
        }
    }

    @Inject(method = "writeCustomDataToTag", at = @At("RETURN"))
    private void toTag(CompoundTag root, CallbackInfo ci) {
        this.serializeChannel(root);
    }

    @Inject(method = "readCustomDataFromTag", at = @At("RETURN"))
    private void fromTag(CompoundTag root, CallbackInfo ci) {
        this.loadedChannel = this.deserializeChannelId(root);
    }

    @Override
    public void remove() {
        this.invalidateChannel();
        this.removeDisplay();

        super.remove();
    }
}
