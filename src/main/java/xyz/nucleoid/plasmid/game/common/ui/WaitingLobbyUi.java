package xyz.nucleoid.plasmid.game.common.ui;

import eu.pb4.sgui.api.gui.HotbarGui;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.GameWaitingLobbyEvents;
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
    public boolean canPlayerClose() {
        return false;
    }
}
