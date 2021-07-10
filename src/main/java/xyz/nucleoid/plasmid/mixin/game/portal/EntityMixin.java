package xyz.nucleoid.plasmid.mixin.game.portal;

import eu.pb4.holograms.api.Holograms;
import eu.pb4.holograms.api.elements.text.StaticTextHologramElement;
import eu.pb4.holograms.api.holograms.AbstractHologram;
import eu.pb4.holograms.api.holograms.EntityHologram;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
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

    private EntityHologram hologram;
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
        if (portal == null) {
            this.removeHologram();
        }
    }

    @Nullable
    @Override
    public GamePortal getPortal() {
        return this.portal;
    }

    @Override
    public void setDisplay(GamePortalDisplay display) {
        var hologram = this.getOrCreateHologram();

        hologram.clearElements();

        var name = display.get(GamePortalDisplay.NAME);
        var playerCount = display.get(GamePortalDisplay.PLAYER_COUNT);
        if (name != null && playerCount != null) {
            hologram.addElement(new StaticTextHologramElement(name));
            hologram.addElement(new StaticTextHologramElement(new LiteralText(playerCount + " players")));
        }
    }

    private EntityHologram getOrCreateHologram() {
        var hologram = this.hologram;
        if (hologram != null) {
            return hologram;
        }

        var entity = (Entity) (Object) this;
        var offset = new Vec3d(0.0, DisguiseLibCompatibility.getEntityHeight(entity), 0.0);

        this.hologram = hologram = Holograms.create(entity, offset, new Text[0]);
        hologram.setAlignment(AbstractHologram.VerticalAlign.BOTTOM);

        hologram.show();

        return hologram;
    }

    private void removeHologram() {
        var hologram = this.hologram;
        this.hologram = null;

        if (hologram != null) {
            hologram.hide();
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
        this.removeHologram();
    }
}
