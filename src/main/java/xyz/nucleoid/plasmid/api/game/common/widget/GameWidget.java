package xyz.nucleoid.plasmid.api.game.common.widget;

import net.minecraft.server.level.ServerPlayer;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;

/**
 * A widget which should be displayed on the GUI for all added players.
 *
 * @see SidebarWidget
 * @see BossBarWidget
 * @see GlobalWidgets
 */
public interface GameWidget extends AutoCloseable {
    void addPlayer(ServerPlayer player);

    void removePlayer(ServerPlayer player);

    @Override
    void close();
}
