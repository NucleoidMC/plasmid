package xyz.nucleoid.plasmid.mixin.game.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
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
import xyz.nucleoid.plasmid.api.menu.GameMenuEntryConfig;
import xyz.nucleoid.plasmid.api.menu.GameMenuTheme;
import xyz.nucleoid.plasmid.impl.menu.anchor.GameMenuAnchor;
import xyz.nucleoid.plasmid.impl.menu.anchor.GameMenuAnchors;
import xyz.nucleoid.plasmid.impl.menu.anchor.GameMenuDisplay;

@Mixin(SignBlockEntity.class)
public abstract class SignBlockEntityMixin extends BlockEntity implements GameMenuAnchor {
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
    private Holder<GameMenuEntryConfig> anchoredEntry;
    @Unique
    private Holder<GameMenuTheme> anchoredTheme;
    @Unique
    private GameMenuDisplay lastDisplay;
    @Unique
    private boolean anchorLoaded;

    @Override
    public void setAnchoredEntry(@Nullable Holder<GameMenuEntryConfig> entry) {
        this.anchoredEntry = entry;
    }

    @Nullable
    @Override
    public Holder<GameMenuEntryConfig> getAnchoredEntry() {
        return this.anchoredEntry;
    }

    @Override
    public void setAnchoredTheme(@Nullable Holder<GameMenuTheme> theme) {
        this.anchoredTheme = theme;
    }

    @Nullable
    @Override
    public Holder<GameMenuTheme> getAnchoredTheme() {
        return this.anchoredTheme;
    }

    @Nullable
    @Override
    public GameMenuDisplay getLastDisplay() {
        return this.lastDisplay;
    }

    @Override
    public void setLastDisplay(@Nullable GameMenuDisplay display) {
        this.lastDisplay = display;
    }

    @Override
    public void setDisplay(GameMenuDisplay display) {
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
    private Component getDisplayLine(GameMenuDisplay display, int line) {
        if (line == 1) {
            var name = display.get(GameMenuDisplay.NAME);
            if (name != null) {
                return name;
            }
        } else if (line == 2) {
            var playerCount = display.get(GameMenuDisplay.PLAYER_COUNT);
            if (playerCount != null) {
                return Component.translatable("text.plasmid.game.menu.player_count", playerCount);
            }
        }
        return CommonComponents.EMPTY;
    }

    @Inject(method = "canExecuteClickCommands", at = @At("HEAD"), cancellable = true)
    private void canRunCommandClickEvent(CallbackInfoReturnable<Boolean> ci) {
        if (this.isWaxed() && this.anchoredEntry != null) {
            ci.setReturnValue(true);
        }
    }

    @Inject(method = "executeClickCommandsIfPresent", at = @At("HEAD"), cancellable = true)
    private void runCommandClickEvent(ServerLevel world, Player player, BlockPos pos, boolean front, CallbackInfoReturnable<Boolean> ci) {
        if (this.anchoredEntry != null && player instanceof ServerPlayer serverPlayer) {
            this.onInteract(serverPlayer, false);
            ci.setReturnValue(true);
        }
    }

    @Inject(method = "saveAdditional", at = @At("RETURN"))
    private void writeAnchor(ValueOutput view, CallbackInfo ci) {
        this.serializeAnchor(view);
    }

    @Inject(method = "loadAdditional", at = @At("RETURN"))
    private void readAnchor(ValueInput view, CallbackInfo ci) {
        this.deserializeAnchor(view);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        GameMenuAnchors.unbind(this);
    }
}
