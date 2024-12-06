package xyz.nucleoid.plasmid.mixin.game.portal;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.Brightness;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.impl.portal.GamePortal;
import xyz.nucleoid.plasmid.impl.portal.GamePortalDisplay;
import xyz.nucleoid.plasmid.impl.portal.GamePortalInterface;
import xyz.nucleoid.plasmid.impl.compatibility.DisguiseLibCompatibility;

@Mixin(Entity.class)
public abstract class EntityMixin implements GamePortalInterface {
    @Shadow
    public World world;

    @Shadow
    public abstract Vec3d getPos();

    @Unique
    private TextDisplayElement textDisplayElement;
    @Unique
    private ElementHolder hologram;
    @Unique
    private GamePortal portal;
    @Unique
    private Identifier loadedPortalId;

    @Override
    public boolean interactWithPortal(ServerPlayerEntity player) {
        if (this.portal != null) {
            this.portal.requestJoin(player, false);
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
        var hologram = this.getOrCreateTextElement();

        var text = Text.empty();

        var name = display.get(GamePortalDisplay.NAME);
        var playerCount = display.get(GamePortalDisplay.PLAYER_COUNT);
        if (name != null && playerCount != null) {
            text.append(name);
            if (playerCount > -1) {
                text.append("\n").append(Text.translatable("text.plasmid.game.portal.player_count", playerCount));
            }
        }
        hologram.setText(text);

        if (hologram.isDirty()) {
            hologram.tick();
        }
    }

    @Unique
    private TextDisplayElement getOrCreateTextElement() {
        if (this.hologram != null) {
            return textDisplayElement;
        }

        var entity = (Entity) (Object) this;
        var offset = new Vec3d(0.0, DisguiseLibCompatibility.getEntityHeight(entity) + 0.2, 0.0);

        this.hologram = new ElementHolder();
        this.textDisplayElement = new TextDisplayElement();
        this.textDisplayElement.setOffset(offset);
        this.textDisplayElement.setBrightness(new Brightness(15, 15));
        this.textDisplayElement.setBillboardMode(DisplayEntity.BillboardMode.CENTER);
        this.textDisplayElement.setDisplayWidth(5);
        this.textDisplayElement.setDisplayHeight(1);
        this.textDisplayElement.setViewRange(0.5f);
        this.hologram.addElement(this.textDisplayElement);

        EntityAttachment.of(this.hologram, (Entity) (Object) this);

        return this.textDisplayElement;
    }

    private void removeHologram() {
        var hologram = this.hologram;
        this.hologram = null;

        if (hologram != null) {
            hologram.destroy();
            this.textDisplayElement = null;
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (this.loadedPortalId != null) {
            if (this.tryConnectTo(this.loadedPortalId)) {
                this.loadedPortalId = null;
            }
        }
    }

    @Inject(method = "writeNbt", at = @At("RETURN"))
    private void writeNbt(NbtCompound root, CallbackInfoReturnable<NbtCompound> ci) {
        if (this.loadedPortalId == null) {
            this.serializePortal(root);
        } else {
            root.putString(GamePortalInterface.NBT_KEY, this.loadedPortalId.toString());
        }
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
