package xyz.nucleoid.plasmid.impl.game.common.ui;

import eu.pb4.sgui.api.gui.HotbarGui;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.ui.WaitingLobbyUiLayout;
import xyz.nucleoid.plasmid.api.game.event.GameWaitingLobbyEvents;
import xyz.nucleoid.stimuli.Stimuli;

public class WaitingLobbyUi extends HotbarGui {
    public WaitingLobbyUi(ServerPlayer player, GameSpace gameSpace) {
        super(player);

        var layout = WaitingLobbyUiLayout.of(elements -> {
            int index = 0;

            for (var element : elements) {
                this.setSlot(index, element);
                index += 1;
            }
        });

        try (var invokers = Stimuli.select().forEntity(player)) {
            invokers.get(GameWaitingLobbyEvents.BUILD_UI_LAYOUT).onBuildUiLayout(layout, player);
        }

        layout.refresh();
    }

    @Override
    public boolean onHandSwing() {
        super.onHandSwing();
        return true;
    }

    @Override
    public boolean onClickBlock(BlockHitResult hitResult) {
        return true;
    }

    @Override
    public boolean onEntityAttacked(int entityId) {
        super.onEntityAttacked(entityId);
        return true;
    }

    @Override
    public boolean onEntityInteracted(int entityId, InteractionHand hand, boolean isSneaking, Vec3 interactionPos) {
        super.onEntityInteracted(entityId, hand, isSneaking, interactionPos);
        return true;
    }

    @Override
    public boolean onPickItemFromEntity(int entityId, boolean includeData) {
        super.onPickItemFromEntity(entityId, includeData);
        return true;
    }

    @Override
    public boolean canPlayerClose() {
        return false;
    }
}
