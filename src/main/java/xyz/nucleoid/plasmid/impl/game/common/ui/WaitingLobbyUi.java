package xyz.nucleoid.plasmid.impl.game.common.ui;

import eu.pb4.sgui.api.gui.HotbarGui;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.ui.WaitingLobbyUiLayout;
import xyz.nucleoid.plasmid.api.game.event.GameWaitingLobbyEvents;
import xyz.nucleoid.stimuli.Stimuli;

public class WaitingLobbyUi extends HotbarGui {
    public WaitingLobbyUi(ServerPlayerEntity player, GameSpace gameSpace) {
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
    public boolean onClickEntity(int entityId, EntityInteraction type, boolean isSneaking, Vec3d interactionPos) {
        super.onClickEntity(entityId, type, isSneaking, interactionPos);
        return true;
    }

    @Override
    public boolean canPlayerClose() {
        return false;
    }
}
