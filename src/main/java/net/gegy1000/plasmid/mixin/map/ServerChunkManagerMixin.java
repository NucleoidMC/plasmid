package net.gegy1000.plasmid.mixin.map;

import net.gegy1000.plasmid.game.world.ChunkLoadControl;
import net.gegy1000.plasmid.game.world.ClearChunkTickets;
import net.gegy1000.plasmid.game.world.ClearChunks;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerChunkManager.class)
public abstract class ServerChunkManagerMixin implements ClearChunks, ChunkLoadControl {
    @Shadow
    @Final
    private ChunkTicketManager ticketManager;

    @Shadow
    @Final
    public ThreadedAnvilChunkStorage threadedAnvilChunkStorage;

    @Shadow
    protected abstract void initChunkCaches();

    @Override
    public void clearChunks() {
        this.initChunkCaches();

        ((ClearChunks) this.threadedAnvilChunkStorage).clearChunks();
        ((ClearChunkTickets) this.ticketManager).clearChunkTickets(this.threadedAnvilChunkStorage);
    }

    @Override
    public void enable() {
        ((ChunkLoadControl) this.threadedAnvilChunkStorage).enable();
    }

    @Override
    public void disable() {
        ((ChunkLoadControl) this.threadedAnvilChunkStorage).disable();
    }
}
