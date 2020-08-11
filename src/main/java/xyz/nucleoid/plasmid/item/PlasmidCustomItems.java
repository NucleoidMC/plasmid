package xyz.nucleoid.plasmid.item;

import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.map.template.trace.RegionTraceMode;
import xyz.nucleoid.plasmid.game.map.template.trace.RegionTracer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class PlasmidCustomItems {
    public static final CustomItem ADD_REGION = CustomItem.builder()
            .id(new Identifier(Plasmid.ID, "add_region"))
            .name(new LiteralText("Add Region"))
            .onUse(PlasmidCustomItems::addRegion)
            .onSwingHand(PlasmidCustomItems::changeRegionMode)
            .register();

    private static TypedActionResult<ItemStack> addRegion(PlayerEntity player, World world, Hand hand) {
        if (player instanceof RegionTracer) {
            RegionTracer constructor = (RegionTracer) player;

            RegionTraceMode traceMode = constructor.getMode();

            BlockPos pos = traceMode.tryTrace(player);
            if (pos != null) {
                if (constructor.isTracing()) {
                    constructor.finishTracing(pos);
                    player.sendMessage(new LiteralText("Use /map region commit <name> to add this region"), true);
                } else {
                    constructor.startTracing(pos);
                }
            }
        }

        return TypedActionResult.pass(ItemStack.EMPTY);
    }

    private static void changeRegionMode(PlayerEntity player, Hand hand) {
        if (player instanceof RegionTracer) {
            RegionTracer constructor = (RegionTracer) player;

            RegionTraceMode nextMode = constructor.getMode().next();
            constructor.setMode(nextMode);

            player.sendMessage(new LiteralText("Changed trace mode to: ").append(nextMode.getName()), true);
        }
    }
}
