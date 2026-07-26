package xyz.nucleoid.plasmid.api.game.common.widget;

import eu.pb4.sidebars.api.ScrollableSidebar;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;

import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * An implementation of {@link GameWidget} which provides a sidebar through the use of the vanilla scoreboard which
 * displays at the right side of players' screens.
 *
 * This sidebar will scroll if it contains more than 14 elements
 *
 * @see GlobalWidgets
 */
public class ScrollableSidebarWidget extends ScrollableSidebar implements GameWidget {
    private final Predicate<ServerPlayer> playerChecker;

    public ScrollableSidebarWidget(int ticksPerLine) {
        super(Priority.MEDIUM, ticksPerLine);
        this.playerChecker = ScrollableSidebarWidget::alwaysTrue;
        this.show();
    }

    public ScrollableSidebarWidget(int ticksPerLine, Predicate<ServerPlayer> playerChecker) {
        super(Priority.MEDIUM, ticksPerLine);
        this.playerChecker = playerChecker;
        this.show();
    }

    public ScrollableSidebarWidget(Component title, int ticksPerLine) {
        super(title, Priority.MEDIUM, ticksPerLine);
        this.playerChecker = ScrollableSidebarWidget::alwaysTrue;
        this.show();
    }

    public ScrollableSidebarWidget(Component title, int ticksPerLine, Predicate<ServerPlayer> playerChecker) {
        super(title, Priority.MEDIUM, ticksPerLine);
        this.playerChecker = playerChecker;

        this.show();
    }

    @Override
    public void addPlayer(ServerPlayer player) {
        if (this.playerChecker.test(player)) {
            super.addPlayer(player);
        }
    }

    @Override
    public void close() {
        this.hide();
        this.players.clear();
    }

    private static Boolean alwaysTrue(ServerPlayer player) {
        return true;
    }
}
