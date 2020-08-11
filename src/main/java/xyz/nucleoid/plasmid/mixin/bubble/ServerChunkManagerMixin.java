package xyz.nucleoid.plasmid.mixin.bubble;

import xyz.nucleoid.plasmid.game.world.bubble.BubbleChunkControl;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerChunkManager.class)
public abstract class ServerChunkManagerMixin implements BubbleChunkControl {
    @Shadow
    @Final
    public ThreadedAnvilChunkStorage threadedAnvilChunkStorage;

    @Shadow
    protected abstract void initChunkCaches();

    @Override
    public void enable() {
        this.initChunkCaches();
        ((BubbleChunkControl) this.threadedAnvilChunkStorage).enable();
    }

    @Override
    public void disable() {
        this.initChunkCaches();
        ((BubbleChunkControl) this.threadedAnvilChunkStorage).disable();
    }
}
