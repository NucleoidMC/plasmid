package xyz.nucleoid.plasmid.game.common;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.activity.GameActivity;
import xyz.nucleoid.plasmid.game.common.widget.BossBarWidget;
import xyz.nucleoid.plasmid.game.common.widget.GameWidget;
import xyz.nucleoid.plasmid.game.common.widget.SidebarWidget;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;

import java.util.ArrayList;
import java.util.List;

public final class GlobalWidgets implements AutoCloseable {
    private final GameSpace gameSpace;
    private final List<GameWidget> widgets = new ArrayList<>();

    private GlobalWidgets(GameSpace gameSpace) {
        this.gameSpace = gameSpace;
    }

    public static GlobalWidgets addTo(GameActivity activity) {
        GlobalWidgets widgets = new GlobalWidgets(activity.getGameSpace());

        activity.listen(GamePlayerEvents.ADD, widgets::onAddPlayer);
        activity.listen(GamePlayerEvents.REMOVE, widgets::onRemovePlayer);
        activity.listen(GameActivityEvents.DISABLE, widgets::close);

        return widgets;
    }

    public SidebarWidget addSidebar(Text title) {
        return this.addWidget(new SidebarWidget(this.gameSpace, title));
    }

    public BossBarWidget addBossBar(Text title) {
        return this.addWidget(new BossBarWidget(title));
    }

    public BossBarWidget addBossBar(Text title, BossBar.Color color, BossBar.Style style) {
        return this.addWidget(new BossBarWidget(title, color, style));
    }

    public <T extends GameWidget> T addWidget(T widget) {
        for (ServerPlayerEntity player : this.gameSpace.getPlayers()) {
            widget.addPlayer(player);
        }
        this.widgets.add(widget);
        return widget;
    }

    public void removeWidget(GameWidget widget) {
        if (this.widgets.remove(widget)) {
            widget.close();
        }
    }

    private void onAddPlayer(ServerPlayerEntity player) {
        for (GameWidget widget : this.widgets) {
            widget.addPlayer(player);
        }
    }

    private void onRemovePlayer(ServerPlayerEntity player) {
        for (GameWidget widget : this.widgets) {
            widget.removePlayer(player);
        }
    }

    @Override
    public void close() {
        for (GameWidget widget : this.widgets) {
            widget.close();
        }
        this.widgets.clear();
    }
}
