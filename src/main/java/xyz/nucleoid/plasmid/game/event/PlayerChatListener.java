package xyz.nucleoid.plasmid.game.event;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import xyz.nucleoid.plasmid.game.GameSpace;

/**
 * Called when any {@link net.minecraft.server.network.ServerPlayerEntity} sends a message in chat in a {@link GameSpace}.
 *
 * <p>Upon return:
 * <ul>
 * <li>{@link ActionResult#SUCCESS} cancels further processing and allows the message to be sent.
 * <li>{@link ActionResult#FAIL} cancels further processing and the message being sent.
 * <li>{@link ActionResult#PASS} moves on to the next listener.
 * </ul>
 *
 */
public interface PlayerChatListener {
    EventType<PlayerChatListener> EVENT = EventType.create(PlayerChatListener.class, listeners -> (message, sender) -> {
        for (PlayerChatListener listener : listeners) {
            ActionResult result = listener.onSendChatMessage(message, sender);

            if (result != ActionResult.PASS) {
                return result;
            }
        }

        return ActionResult.PASS;
    });

    ActionResult onSendChatMessage(Text message, ServerPlayerEntity sender);
}
