package xyz.nucleoid.plasmid.mixin.game.space;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.plasmid.impl.player.isolation.ScreenHandlerAccess;

@Mixin(AbstractContainerMenu.class)
public class ScreenHandlerMixin implements ScreenHandlerAccess {
    @Shadow
    @Final
    public NonNullList<Slot> slots;
    @Shadow
    @Final
    private NonNullList<ItemStack> lastSlots;

    @Override
    public void plasmid$resetTrackedState() {
        for (int i = 0; i < this.slots.size(); i++) {
            var stack = this.slots.get(i).getItem();
            this.lastSlots.set(i, stack.copy());
        }
    }
}
