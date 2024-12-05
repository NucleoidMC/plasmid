package xyz.nucleoid.plasmid.api.game.event;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.ApiStatus;
import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.api.game.common.team.TeamAllocator;
import xyz.nucleoid.plasmid.api.game.common.team.TeamSelectionLobby;
import xyz.nucleoid.plasmid.api.game.player.PlayerIterable;
import xyz.nucleoid.stimuli.event.StimulusEvent;

/**
 * Events relating to a {@link TeamSelectionLobby} applied to a {@link GameActivity} within a {@link GameSpace}.
 */
@ApiStatus.Experimental
public final class TeamSelectionLobbyEvents {
    /**
     * Called when a {@link TeamAllocator} has been populated to allocate selected teams for a game.
     * <p>
     * This event can be used to replace team preferences or add groups of
	 * players that should be on the same team.
     */
    public static final StimulusEvent<Finalize> FINALIZE = StimulusEvent.create(Finalize.class, ctx -> (allocator, players) -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onFinalizeTeamSelection(allocator, players);
            }
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
        }
    });

    public interface Finalize {
        void onFinalizeTeamSelection(TeamAllocator<GameTeamKey, ServerPlayerEntity> allocator, PlayerIterable players);
    }
}
