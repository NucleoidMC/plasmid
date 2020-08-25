package xyz.nucleoid.plasmid.mixin.bubble;

import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.world.ScheduledTick;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.plasmid.world.bubble.CloseBubbleWorld;

import java.util.Set;
import java.util.TreeSet;

@Mixin(ServerTickScheduler.class)
public class ServerTickSchedulerMixin<T> implements CloseBubbleWorld {
    @Shadow
    @Final
    private Set<ScheduledTick<T>> scheduledTickActions;

    @Shadow
    @Final
    private TreeSet<ScheduledTick<T>> scheduledTickActionsInOrder;

    @Override
    public void closeBubble() {
        this.scheduledTickActions.clear();
        this.scheduledTickActionsInOrder.clear();
    }
}
