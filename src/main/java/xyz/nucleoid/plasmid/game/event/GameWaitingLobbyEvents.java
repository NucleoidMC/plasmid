package xyz.nucleoid.plasmid.game.event;

import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.common.ui.WaitingLobbyUiLayout;
import xyz.nucleoid.stimuli.event.StimulusEvent;

/**
 * Events relating to a {@link GameWaitingLobby} applied to a {@link GameActivity} within a {@link GameSpace}.
 */
public final class GameWaitingLobbyEvents {
    /**
     * Called when a {@link WaitingLobbyUiLayout} is created for a specific player's waiting lobby UI.
     * <p>
     * This event should be used to add custom UI elements to the hotbar UI
     * used by players before the game begins.
     */
    public static final StimulusEvent<BuildUiLayout> BUILD_UI_LAYOUT = StimulusEvent.create(BuildUiLayout.class, ctx -> (layout, player) -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onBuildUiLayout(layout, player);
            }
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
        }
    });

    public interface BuildUiLayout {
        void onBuildUiLayout(WaitingLobbyUiLayout layout, ServerPlayerEntity player);
    }
}
