package xyz.nucleoid.plasmid.mixin.game.portal;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.nbt.NbtCompound;
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

    private SignBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private GamePortal portal;
    private Identifier loadedPortalId;

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
            var line = this.getDisplayLine(display, i);
            this.setTextOnRow(i, line);
        }

        if (this.hasWorld()) {
            this.markDirty();
            BlockState cachedState = this.getCachedState();
            this.world.updateListeners(this.pos, cachedState, cachedState, Block.NOTIFY_ALL);
        }
    }

    @NotNull
    private Text getDisplayLine(GamePortalDisplay display, int line) {
        if (line == 0) {
            var name = display.get(GamePortalDisplay.NAME);
            if (name != null) {
                return name;
            }
        } else if (line == 1) {
            var playerCount = display.get(GamePortalDisplay.PLAYER_COUNT);
            if (playerCount != null) {
                return new LiteralText(playerCount + " players");
            }
        }
        return LiteralText.EMPTY;
    }

    @Inject(method = "onActivate", at = @At("HEAD"), cancellable = true)
    private void onActivate(ServerPlayerEntity player, CallbackInfoReturnable<Boolean> ci) {
        if (this.portal != null) {
            this.portal.requestJoin(player);
            ci.setReturnValue(true);
        }
    }

    @Inject(method = "writeNbt", at = @At("RETURN"))
    private void writeNbt(NbtCompound root, CallbackInfo ci) {
        this.serializePortal(root);
    }

    @Inject(method = "readNbt", at = @At("RETURN"))
    private void readNbt(NbtCompound root, CallbackInfo ci) {
        this.loadedPortalId = this.deserializePortalId(root);
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);

        var server = world.getServer();
        if (server != null && this.loadedPortalId != null) {
            this.tryConnectTo(this.loadedPortalId);
            this.loadedPortalId = null;
        }
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        this.invalidatePortal();
    }
}
