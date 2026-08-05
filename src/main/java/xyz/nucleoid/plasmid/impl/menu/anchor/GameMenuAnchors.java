package xyz.nucleoid.plasmid.impl.menu.anchor;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import java.util.Set;

/**
 * The anchors currently loaded, refreshed on a timer so signs and holograms follow player counts.
 */
public final class GameMenuAnchors {
    private static final long UPDATE_INTERVAL = 20L;

    private static final Set<GameMenuAnchor> LIVE = new ReferenceOpenHashSet<>();

    private static long lastUpdate;

    private GameMenuAnchors() {
    }

    static void bind(GameMenuAnchor anchor) {
        LIVE.add(anchor);
    }

    public static void unbind(GameMenuAnchor anchor) {
        LIVE.remove(anchor);
    }

    public static void clear() {
        LIVE.clear();
    }

    public static void tick(MinecraftServer server) {
        long time = server.overworld().getGameTime();
        if (time - lastUpdate <= UPDATE_INTERVAL) {
            return;
        }

        lastUpdate = time;

        for (var anchor : LIVE) {
            var entry = anchor.resolveEntry();
            if (entry == null) {
                continue;
            }

            var display = new GameMenuDisplay();
            display.set(GameMenuDisplay.NAME, Component.empty().append(entry.name()).withStyle(ChatFormatting.AQUA));
            display.set(GameMenuDisplay.PLAYER_COUNT, entry.getPlayerCount());

            if (!display.equals(anchor.getLastDisplay())) {
                anchor.setLastDisplay(display);
                anchor.setDisplay(display);
            }
        }
    }
}
