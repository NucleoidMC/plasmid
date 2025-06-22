package xyz.nucleoid.plasmid.mixin.game.portal;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
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

@Mixin(SignBlockEntity.class)
public abstract class SignBlockEntityMixin extends BlockEntity implements GamePortalInterface {
    @Shadow
    public abstract SignText getText(boolean front);

    @Shadow
    public abstract boolean setText(SignText text, boolean front);

    @Shadow
    public abstract boolean isWaxed();

    @Shadow
    public abstract boolean setWaxed(boolean waxed);

    private SignBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Unique
    private GamePortal portal;
    @Unique
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
        var lines = new Text[SignText.field_43299];
        for (int i = 0; i < SignText.field_43299; i++) {
            lines[i] = this.getDisplayLine(display, i);
        }

        var oldText = this.getText(true);
        this.setText(new SignText(lines, lines, oldText.getColor(), oldText.isGlowing()), true);

        this.setWaxed(true);

        if (this.hasWorld()) {
            BlockState cachedState = this.getCachedState();
            this.world.updateListeners(this.pos, cachedState, cachedState, Block.NOTIFY_ALL);
        }
    }

    @NotNull
    private Text getDisplayLine(GamePortalDisplay display, int line) {
        if (line == 1) {
            var name = display.get(GamePortalDisplay.NAME);
            if (name != null) {
                return name;
            }
        } else if (line == 2) {
            var playerCount = display.get(GamePortalDisplay.PLAYER_COUNT);
            if (playerCount != null) {
                return Text.translatable("text.plasmid.game.portal.player_count", playerCount);
            }
        }
        return ScreenTexts.EMPTY;
    }

    @Inject(method = "canRunCommandClickEvent", at = @At("HEAD"), cancellable = true)
    private void canRunCommandClickEvent(CallbackInfoReturnable<Boolean> ci) {
        if (this.isWaxed() && this.portal != null) {
            ci.setReturnValue(true);
        }
    }

    @Inject(method = "runCommandClickEvent", at = @At("HEAD"), cancellable = true)
    private void runCommandClickEvent(ServerWorld world, PlayerEntity player, BlockPos pos, boolean front, CallbackInfoReturnable<Boolean> ci) {
        if (this.portal != null && player instanceof ServerPlayerEntity serverPlayer) {
            this.portal.requestJoin(serverPlayer, false);
            ci.setReturnValue(true);
        }
    }

    @Inject(method = "writeData", at = @At("RETURN"))
    private void writePortalNbt(WriteView view, CallbackInfo ci) {
        this.serializePortal(view);
    }

    @Inject(method = "readData", at = @At("RETURN"))
    private void readPortalData(ReadView view, CallbackInfo ci) {
        this.loadedPortalId = this.deserializePortalId(view);
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
    public boolean updatePortalImmediately() {
        return this.loadedPortalId == null;
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        this.invalidatePortal();
    }
}
