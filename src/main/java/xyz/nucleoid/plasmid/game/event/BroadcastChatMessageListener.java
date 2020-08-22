package xyz.nucleoid.plasmid.game.event;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

/**
 * Called when any {@link net.minecraft.server.network.ServerPlayerEntity} sends a message in chat in a {@link xyz.nucleoid.plasmid.game.GameWorld}.
 *
 * <p>Upon return:
 * <ul>
 * <li>{@link ActionResult#SUCCESS} cancels further processing and allows the message to be sent.
 * <li>{@link ActionResult#FAIL} cancels further processing and the message being sent.
 * <li>{@link ActionResult#PASS} moves on to the next listener.
 * </ul>
 *
 */
public interface BroadcastChatMessageListener {
    EventType<BroadcastChatMessageListener> EVENT = EventType.create(BroadcastChatMessageListener.class, listeners -> (message, sender) -> {
        for (BroadcastChatMessageListener listener : listeners) {
            ActionResult result = listener.onBroadcastChatMessage(message, sender);

            if (result != ActionResult.PASS) {
                return result;
            }
        }

        return ActionResult.PASS;
    });

    ActionResult onBroadcastChatMessage(Text message, ServerPlayerEntity sender);
}