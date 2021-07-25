package xyz.nucleoid.plasmid.game.common.widget;

import eu.pb4.sidebars.api.Sidebar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.function.Function;

/**
 * An implementation of {@link GameWidget} which provides a sidebar through the use of the vanilla scoreboard which
 * displays at the right side of players' screens.
 *
 * @see xyz.nucleoid.plasmid.game.common.GlobalWidgets
 */
public final class SidebarWidget extends Sidebar implements GameWidget {
    private final Function<ServerPlayerEntity, Boolean> playerChecker;

    public SidebarWidget() {
        super(Priority.MEDIUM);
        this.playerChecker = SidebarWidget::alwaysTrue;
        this.show();
    }

    public SidebarWidget(Function<ServerPlayerEntity, Boolean> playerChecker) {
        super(Priority.MEDIUM);
        this.playerChecker = playerChecker;
        this.show();
    }

    public SidebarWidget(Text title) {
        super(title, Priority.MEDIUM);
        this.playerChecker = SidebarWidget::alwaysTrue;
        this.show();
    }

    public SidebarWidget(Text title, Function<ServerPlayerEntity, Boolean> playerChecker) {
        super(title, Priority.MEDIUM);
        this.playerChecker = playerChecker;

        this.show();
    }

    @Override
    public void addPlayer(ServerPlayerEntity player) {
        if (this.playerChecker.apply(player)) {
            super.addPlayer(player);
        }
    }

    @Override
    public void close() {
        this.hide();
        this.players.clear();
    }

    private static boolean alwaysTrue(ServerPlayerEntity player) {
        return true;
    }
}
