package xyz.nucleoid.plasmid.mixin.bubble;

import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.plasmid.world.bubble.CloseBubbleWorld;

@Mixin(ServerChunkManager.class)
public abstract class ServerChunkManagerMixin implements CloseBubbleWorld {
    @Shadow
    @Final
    public ThreadedAnvilChunkStorage threadedAnvilChunkStorage;

    @Shadow
    protected abstract void initChunkCaches();

    @Override
    public void closeBubble() {
        this.initChunkCaches();
        CloseBubbleWorld.closeBubble(this.threadedAnvilChunkStorage);
    }
}
