package xyz.nucleoid.plasmid.mixin.game.channel;

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
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.game.channel.GameChannelInterface;
import xyz.nucleoid.plasmid.game.channel.GameChannel;

@Mixin(SignBlockEntity.class)
public abstract class SignBlockEntityMixin extends BlockEntity implements GameChannelInterface {
    @Shadow
    public abstract void setTextOnRow(int row, Text text);

    private GameChannel channel;
    private Identifier loadedChannel;

    private SignBlockEntityMixin(BlockEntityType<?> type) {
        super(type);
    }

    @Override
    public void setChannel(GameChannel channel) {
        this.channel = channel;
    }

    @Nullable
    @Override
    public GameChannel getChannel() {
        return this.channel;
    }

    @Override
    public void setDisplay(Text[] display) {
        for (int i = 0; i < 4; i++) {
            Text line = i < display.length ? display[i] : LiteralText.EMPTY;
            this.setTextOnRow(i, line);
        }

        if (this.hasWorld()) {
            this.markDirty();
            BlockState cachedState = this.getCachedState();
            this.world.updateListeners(this.pos, cachedState, cachedState, 0b11);
        }
    }

    @Inject(method = "onActivate", at = @At("HEAD"), cancellable = true)
    private void onActivate(PlayerEntity player, CallbackInfoReturnable<Boolean> ci) {
        if (this.channel != null && player instanceof ServerPlayerEntity) {
            this.channel.requestJoin((ServerPlayerEntity) player);
            ci.setReturnValue(true);
        }
    }

    @Inject(method = "toTag", at = @At("RETURN"))
    private void toTag(CompoundTag root, CallbackInfoReturnable<CompoundTag> ci) {
        this.serializeChannel(root);
    }

    @Inject(method = "fromTag", at = @At("RETURN"))
    private void fromTag(BlockState state, CompoundTag root, CallbackInfo ci) {
        this.loadedChannel = this.deserializeChannelId(root);
    }

    @Override
    public void setLocation(World world, BlockPos pos) {
        MinecraftServer server = world.getServer();
        if (server != null && this.loadedChannel != null) {
            this.tryConnectTo(server, this.loadedChannel);
            this.loadedChannel = null;
        }

        super.setLocation(world, pos);
    }

    @Override
    public void markInvalid() {
        super.markInvalid();
        this.invalidateChannel();
    }
}
