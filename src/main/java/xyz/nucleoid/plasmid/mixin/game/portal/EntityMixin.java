package xyz.nucleoid.plasmid.mixin.game.portal;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
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
import xyz.nucleoid.plasmid.game.portal.GamePortal;
import xyz.nucleoid.plasmid.game.portal.GamePortalDisplay;
import xyz.nucleoid.plasmid.game.portal.GamePortalInterface;
import xyz.nucleoid.plasmid.util.compatibility.DisguiseLibCompatibility;

@Mixin(Entity.class)
public abstract class EntityMixin implements GamePortalInterface {
    @Shadow
    public World world;

    @Shadow
    public abstract Vec3d getPos();

    private FloatingText display;
    private GamePortal portal;
    private Identifier loadedPortalId;

    @Override
    public boolean interactWithPortal(ServerPlayerEntity player) {
        if (this.portal != null) {
            this.portal.requestJoin(player);
            return true;
        }
        return false;
    }

    @Override
    public void setPortal(GamePortal portal) {
        this.portal = portal;
        if (portal == null && this.display != null) {
            this.display.remove();
            this.display = null;
        }
    }

    @Nullable
    @Override
    public GamePortal getPortal() {
        return this.portal;
    }

    @Inject(method = "setPos", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;entityChangeListener:Lnet/minecraft/world/entity/EntityChangeListener;"))
    private void setPos(double x, double y, double z, CallbackInfo ci) {
        if (this.display != null) {
            this.display.setPos(this.getDisplayAnchor());
        }
    }

    @Override
    public void setDisplay(GamePortalDisplay display) {
        this.removeDisplay();

        Text name = display.get(GamePortalDisplay.NAME);
        Integer playerCount = display.get(GamePortalDisplay.PLAYER_COUNT);
        if (name != null && playerCount != null) {
            FloatingText floatingText = this.createDisplay();
            floatingText.setText(new Text[] { name, new LiteralText(playerCount + " players") });
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
        return this.getPos().add(0.0, DisguiseLibCompatibility.getEntityHeight((Entity) (Object) this), 0.0);
    }

    private void removeDisplay() {
        if (this.display != null) {
            this.display.remove();
            this.display = null;
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (this.loadedPortalId != null) {
            this.tryConnectTo(this.loadedPortalId);
            this.loadedPortalId = null;
        }
    }

    @Inject(method = "writeNbt", at = @At("RETURN"))
    private void writeNbt(NbtCompound root, CallbackInfoReturnable<NbtCompound> ci) {
        this.serializePortal(root);
    }

    @Inject(method = "readNbt", at = @At("RETURN"))
    private void readNbt(NbtCompound root, CallbackInfo ci) {
        this.loadedPortalId = this.deserializePortalId(root);
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void remove(CallbackInfo ci) {
        this.invalidatePortal();
        this.removeDisplay();
    }
}
