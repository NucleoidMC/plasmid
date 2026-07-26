package xyz.nucleoid.plasmid.api.game.common.widget;

import eu.pb4.sidebars.api.Sidebar;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;

import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.server.level.ServerPlayer;

/**
 * An implementation of {@link GameWidget} which provides a sidebar through the use of the vanilla scoreboard which
 * displays at the right side of players' screens.
 *
 * @see GlobalWidgets
 */
public class SidebarWidget extends Sidebar implements GameWidget {
    private final Predicate<ServerPlayer> playerChecker;

    public SidebarWidget() {
        super(Priority.MEDIUM);
        this.setDefaultNumberFormat(BlankFormat.INSTANCE);
        this.playerChecker = SidebarWidget::alwaysTrue;
        this.show();
    }

    public SidebarWidget(Predicate<ServerPlayer> playerChecker) {
        super(Priority.MEDIUM);
        this.setDefaultNumberFormat(BlankFormat.INSTANCE);
        this.playerChecker = playerChecker;
        this.show();
    }

    public SidebarWidget(Component title) {
        super(title, Priority.MEDIUM);
        this.setDefaultNumberFormat(BlankFormat.INSTANCE);
        this.playerChecker = SidebarWidget::alwaysTrue;
        this.show();
    }

    public SidebarWidget(Component title, Predicate<ServerPlayer> playerChecker) {
        super(title, Priority.MEDIUM);
        this.setDefaultNumberFormat(BlankFormat.INSTANCE);
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

    private static boolean alwaysTrue(ServerPlayer player) {
        return true;
    }
}
