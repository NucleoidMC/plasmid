package xyz.nucleoid.plasmid.game.common.widget;

import eu.pb4.sidebars.api.ScrollableSidebar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.function.Predicate;

/**
 * An implementation of {@link GameWidget} which provides a sidebar through the use of the vanilla scoreboard which
 * displays on the right side of players' screens.
 *
 * This sidebar will scroll if it contains more than 14 elements
 *
 * @see xyz.nucleoid.plasmid.game.common.GlobalWidgets
 */
public final class ScrollableSidebarWidget extends ScrollableSidebar implements GameWidget {
    private final Predicate<ServerPlayerEntity> playerChecker;

    public ScrollableSidebarWidget(int ticksPerLine) {
        super(Priority.MEDIUM, ticksPerLine);
        this.playerChecker = ScrollableSidebarWidget::alwaysTrue;
        this.show();
    }

    public ScrollableSidebarWidget(int ticksPerLine, Predicate<ServerPlayerEntity> playerChecker) {
        super(Priority.MEDIUM, ticksPerLine);
        this.playerChecker = playerChecker;
        this.show();
    }

    public ScrollableSidebarWidget(Text title, int ticksPerLine) {
        super(title, Priority.MEDIUM, ticksPerLine);
        this.playerChecker = ScrollableSidebarWidget::alwaysTrue;
        this.show();
    }

    public ScrollableSidebarWidget(Text title, int ticksPerLine, Predicate<ServerPlayerEntity> playerChecker) {
        super(title, Priority.MEDIUM, ticksPerLine);
        this.playerChecker = playerChecker;

        this.show();
    }

    @Override
    public void addPlayer(ServerPlayerEntity player) {
        if (this.playerChecker.test(player)) {
            super.addPlayer(player);
        }
    }

    @Override
    public void close() {
        this.hide();
        this.players.clear();
    }

    private static Boolean alwaysTrue(ServerPlayerEntity player) {
        return true;
    }
}
