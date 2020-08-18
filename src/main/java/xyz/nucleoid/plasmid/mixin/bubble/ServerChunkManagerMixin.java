package xyz.nucleoid.plasmid.mixin.bubble;

import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.plasmid.world.bubble.BubbleWorldControl;

@Mixin(ServerChunkManager.class)
public abstract class ServerChunkManagerMixin implements BubbleWorldControl {
    @Shadow
    @Final
    public ThreadedAnvilChunkStorage threadedAnvilChunkStorage;

    @Shadow
    protected abstract void initChunkCaches();

    @Override
    public void enable() {
        this.initChunkCaches();
        ((BubbleWorldControl) this.threadedAnvilChunkStorage).enable();
    }

    @Override
    public void disable() {
        this.initChunkCaches();
        ((BubbleWorldControl) this.threadedAnvilChunkStorage).disable();
    }
}
