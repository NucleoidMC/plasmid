package xyz.nucleoid.plasmid.mixin.game.channel;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.entity.FloatingText;
import xyz.nucleoid.plasmid.game.channel.GameChannel;
import xyz.nucleoid.plasmid.game.channel.GameChannelInterface;

@Mixin(Entity.class)
public abstract class EntityMixin implements GameChannelInterface {
    @Shadow
    public World world;

    @Shadow
    public abstract Vec3d getPos();

    @Shadow
    public abstract float getHeight();

    private FloatingText display;
    private GameChannel channel;
    private Identifier loadedChannel;

    @Override
    public boolean interactWithChannel(ServerPlayerEntity player) {
        if (this.channel != null) {
            this.channel.requestJoin(player);
            return true;
        }
        return false;
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

    @Inject(method = "setPos", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;chunkPosUpdateRequested:Z"))
    private void setPos(double x, double y, double z, CallbackInfo ci) {
        if (this.display != null) {
            this.display.setPos(this.getDisplayAnchor());
        }
    }

    @Override
    public void setDisplay(Text[] display) {
        this.removeDisplay();
        if (display.length > 0) {
            FloatingText floatingText = this.createDisplay();
            floatingText.setText(display);
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

    @Inject(method = "toTag", at = @At("RETURN"))
    private void toTag(CompoundTag root, CallbackInfoReturnable<CompoundTag> ci) {
        this.serializeChannel(root);
    }

    @Inject(method = "fromTag", at = @At("RETURN"))
    private void fromTag(CompoundTag root, CallbackInfo ci) {
        this.loadedChannel = this.deserializeChannelId(root);
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void remove(CallbackInfo ci) {
        this.invalidateChannel();
        this.removeDisplay();
    }
}
