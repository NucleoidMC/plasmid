package xyz.nucleoid.plasmid.game.common.widget;

import eu.pb4.sidebars.api.Sidebar;
import net.minecraft.scoreboard.number.BlankNumberFormat;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.function.Predicate;

/**
 * An implementation of {@link GameWidget} which provides a sidebar through the use of the vanilla scoreboard which
 * displays at the right side of players' screens.
 *
 * @see xyz.nucleoid.plasmid.game.common.GlobalWidgets
 */
public class SidebarWidget extends Sidebar implements GameWidget {
    private final Predicate<ServerPlayerEntity> playerChecker;

    public SidebarWidget() {
        super(Priority.MEDIUM);
        this.setDefaultNumberFormat(BlankNumberFormat.INSTANCE);
        this.playerChecker = SidebarWidget::alwaysTrue;
        this.show();
    }

    public SidebarWidget(Predicate<ServerPlayerEntity> playerChecker) {
        super(Priority.MEDIUM);
        this.setDefaultNumberFormat(BlankNumberFormat.INSTANCE);
        this.playerChecker = playerChecker;
        this.show();
    }

    public SidebarWidget(Text title) {
        super(title, Priority.MEDIUM);
        this.setDefaultNumberFormat(BlankNumberFormat.INSTANCE);
        this.playerChecker = SidebarWidget::alwaysTrue;
        this.show();
    }

    public SidebarWidget(Text title, Predicate<ServerPlayerEntity> playerChecker) {
        super(title, Priority.MEDIUM);
        this.setDefaultNumberFormat(BlankNumberFormat.INSTANCE);
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

    private static boolean alwaysTrue(ServerPlayerEntity player) {
        return true;
    }
}
