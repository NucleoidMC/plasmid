package xyz.nucleoid.plasmid.mixin.bubble;

import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.world.ScheduledTick;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.plasmid.game.world.bubble.BubbleWorldControl;

import java.util.Set;
import java.util.TreeSet;

@Mixin(ServerTickScheduler.class)
public class ServerTickSchedulerMixin<T> implements BubbleWorldControl {
    @Shadow
    @Final
    private Set<ScheduledTick<T>> scheduledTickActions;

    @Shadow
    @Final
    private TreeSet<ScheduledTick<T>> scheduledTickActionsInOrder;

    @Override
    public void disable() {
        this.clearActions();
    }

    @Override
    public void enable() {
        this.clearActions();
    }

    private void clearActions() {
        this.scheduledTickActions.clear();
        this.scheduledTickActionsInOrder.clear();
    }
}
