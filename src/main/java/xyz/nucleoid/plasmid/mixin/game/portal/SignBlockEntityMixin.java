package xyz.nucleoid.plasmid.mixin.game.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
        var lines = new Component[SignText.LINES];
        for (int i = 0; i < SignText.LINES; i++) {
            lines[i] = this.getDisplayLine(display, i);
        }

        var oldText = this.getText(true);
        this.setText(new SignText(lines, lines, oldText.getColor(), oldText.hasGlowingText()), true);

        this.setWaxed(true);

        if (this.hasLevel()) {
            BlockState cachedState = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, cachedState, cachedState, Block.UPDATE_ALL);
        }
    }

    @NotNull
    private Component getDisplayLine(GamePortalDisplay display, int line) {
        if (line == 1) {
            var name = display.get(GamePortalDisplay.NAME);
            if (name != null) {
                return name;
            }
        } else if (line == 2) {
            var playerCount = display.get(GamePortalDisplay.PLAYER_COUNT);
            if (playerCount != null) {
                return Component.translatable("text.plasmid.game.portal.player_count", playerCount);
            }
        }
        return CommonComponents.EMPTY;
    }

    @Inject(method = "canExecuteClickCommands", at = @At("HEAD"), cancellable = true)
    private void canRunCommandClickEvent(CallbackInfoReturnable<Boolean> ci) {
        if (this.isWaxed() && this.portal != null) {
            ci.setReturnValue(true);
        }
    }

    @Inject(method = "executeClickCommandsIfPresent", at = @At("HEAD"), cancellable = true)
    private void runCommandClickEvent(ServerLevel world, Player player, BlockPos pos, boolean front, CallbackInfoReturnable<Boolean> ci) {
        if (this.portal != null && player instanceof ServerPlayer serverPlayer) {
            this.portal.requestJoin(serverPlayer, false);
            ci.setReturnValue(true);
        }
    }

    @Inject(method = "saveAdditional", at = @At("RETURN"))
    private void writePortalNbt(ValueOutput view, CallbackInfo ci) {
        this.serializePortal(view);
    }

    @Inject(method = "loadAdditional", at = @At("RETURN"))
    private void readPortalData(ValueInput view, CallbackInfo ci) {
        this.loadedPortalId = this.deserializePortalId(view);
    }

    @Override
    public void setLevel(Level world) {
        super.setLevel(world);

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
    public void setRemoved() {
        super.setRemoved();
        this.invalidatePortal();
    }
}
