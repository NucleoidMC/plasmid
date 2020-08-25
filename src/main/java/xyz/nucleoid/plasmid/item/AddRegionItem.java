package xyz.nucleoid.plasmid.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xyz.nucleoid.plasmid.fake.FakeItem;
import xyz.nucleoid.plasmid.game.map.template.trace.RegionTraceMode;
import xyz.nucleoid.plasmid.game.map.template.trace.RegionTracer;

public final class AddRegionItem extends Item implements FakeItem {
    public AddRegionItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (world.isClient) {
            return super.use(world, player, hand);
        }

        ItemStack stack = player.getStackInHand(hand);

        if (player instanceof RegionTracer) {
            RegionTracer tracer = (RegionTracer) player;

            if (!player.isSneaking()) {
                this.updateTrace(player, tracer);
            } else {
                this.changeTraceMode(player, tracer);
            }

            return TypedActionResult.success(stack);
        }

        return TypedActionResult.pass(stack);
    }

    private void updateTrace(PlayerEntity player, RegionTracer tracer) {
        RegionTraceMode traceMode = tracer.getMode();

        BlockPos pos = traceMode.tryTrace(player);
        if (pos != null) {
            if (tracer.isTracing()) {
                tracer.finishTracing(pos);
                player.sendMessage(new LiteralText("Use /map region commit <name> to add this region"), true);
            } else {
                tracer.startTracing(pos);
            }
        }
    }

    private void changeTraceMode(PlayerEntity player, RegionTracer tracer) {
        RegionTraceMode nextMode = tracer.getMode().next();
        tracer.setMode(nextMode);

        player.sendMessage(new LiteralText("Changed trace mode to: ").append(nextMode.getName()), true);
    }

    @Override
    public Item asProxy() {
        return Items.STICK;
    }
}
