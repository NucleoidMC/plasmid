package xyz.nucleoid.plasmid.mixin.game.portal;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
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

@Mixin(SignBlockEntity.class)
public abstract class SignBlockEntityMixin extends BlockEntity implements GamePortalInterface {
    @Shadow
    public abstract void setTextOnRow(int row, Text text);

    private GamePortal portal;
    private Identifier loadedPortalId;

    private SignBlockEntityMixin(BlockEntityType<?> type) {
        super(type);
    }

    @Override
    public void setPortal(GamePortal portal) {
        this.portal = portal;
    }

    @Nullable
    @Override
    public GamePortal getPortal() {
        return this.portal;
    }

    @Override
    public void setDisplay(GamePortalDisplay display) {
        for (int i = 0; i < 4; i++) {
            Text line = this.getDisplayLine(display, i);
            this.setTextOnRow(i, line);
        }

        if (this.hasWorld()) {
            this.markDirty();
            BlockState cachedState = this.getCachedState();
            this.world.updateListeners(this.pos, cachedState, cachedState, 0b11);
        }
    }

    @NotNull
    private Text getDisplayLine(GamePortalDisplay display, int line) {
        if (line == 0) {
            Text name = display.get(GamePortalDisplay.NAME);
            if (name != null) {
                return name;
            }
        } else if (line == 1) {
            Integer playerCount = display.get(GamePortalDisplay.PLAYER_COUNT);
            if (playerCount != null) {
                return new LiteralText(playerCount + " players");
            }
        }
        return LiteralText.EMPTY;
    }

    @Inject(method = "onActivate", at = @At("HEAD"), cancellable = true)
    private void onActivate(PlayerEntity player, CallbackInfoReturnable<Boolean> ci) {
        if (this.portal != null && player instanceof ServerPlayerEntity) {
            this.portal.requestJoin((ServerPlayerEntity) player);
            ci.setReturnValue(true);
        }
    }

    @Inject(method = "toTag", at = @At("RETURN"))
    private void toTag(CompoundTag root, CallbackInfoReturnable<CompoundTag> ci) {
        this.serializePortal(root);
    }

    @Inject(method = "fromTag", at = @At("RETURN"))
    private void fromTag(BlockState state, CompoundTag root, CallbackInfo ci) {
        this.loadedPortalId = this.deserializePortalId(root);
    }

    @Override
    public void setLocation(World world, BlockPos pos) {
        MinecraftServer server = world.getServer();
        if (server != null && this.loadedPortalId != null) {
            this.tryConnectTo(this.loadedPortalId);
            this.loadedPortalId = null;
        }

        super.setLocation(world, pos);
    }

    @Override
    public void markInvalid() {
        super.markInvalid();
        this.invalidatePortal();
    }
}
