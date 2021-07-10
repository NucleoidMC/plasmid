package xyz.nucleoid.plasmid.game.common.widget;

import eu.pb4.sidebars.api.Sidebar;
import net.minecraft.text.Text;


/**
 * An implementation of {@link GameWidget} which provides a sidebar through the use of the vanilla scoreboard which
 * displays at the right side of players' screens.
 *
 * @see xyz.nucleoid.plasmid.game.common.GlobalWidgets
 */
public final class SidebarWidget extends Sidebar implements GameWidget {
    public SidebarWidget() {
        super(Priority.MEDIUM);
        this.show();
    }

    public SidebarWidget(Text title) {
        super(title, Priority.MEDIUM);
        this.show();
    }

    @Override
    public void close() {
        this.hide();
        this.players.clear();
    }
}
