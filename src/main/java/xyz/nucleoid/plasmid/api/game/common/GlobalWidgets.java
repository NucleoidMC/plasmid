package xyz.nucleoid.plasmid.api.game.common;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.widget.BossBarWidget;
import xyz.nucleoid.plasmid.api.game.common.widget.GameWidget;
import xyz.nucleoid.plasmid.api.game.common.widget.ScrollableSidebarWidget;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.game.common.widget.SidebarWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Utilities for applying various {@link GameWidget} implementations for all players within a {@link GameSpace}.
 *
 * @see GlobalWidgets#addTo(GameActivity)
 * @see SidebarWidget
 * @see BossBarWidget
 */
public final class GlobalWidgets implements AutoCloseable {
    private final GameSpace gameSpace;
    private final List<GameWidget> widgets = new ArrayList<>();

    private GlobalWidgets(GameSpace gameSpace) {
        this.gameSpace = gameSpace;
    }

    /**
     * Creates a {@link GlobalWidgets} instance and registers it to the given {@link GameActivity}.
     * All players within this activity will have the added widgets displayed to them.
     *
     * @param activity the activity to add this instance to
     * @return a {@link GlobalWidgets} instance which can be used to add various widgets for all players
     */
    public static GlobalWidgets addTo(GameActivity activity) {
        var widgets = new GlobalWidgets(activity.getGameSpace());

        activity.listen(GamePlayerEvents.ADD, widgets::onAddPlayer);
        activity.listen(GamePlayerEvents.REMOVE, widgets::onRemovePlayer);
        activity.listen(GameActivityEvents.DISABLE, widgets::close);

        return widgets;
    }

    /**
     * Adds a sidebar for all players associated with this {@link GlobalWidgets} instance.
     *
     * @param title the title for the sidebar
     * @return the created {@link SidebarWidget}
     */
    public SidebarWidget addSidebar(Text title) {
        return this.addWidget(new SidebarWidget(title));
    }

    /**
     * Adds a sidebar for all players associated with this {@link GlobalWidgets} instance.
     *
     * @return the created {@link SidebarWidget}
     */
    public SidebarWidget addSidebar() {
        return this.addWidget(new SidebarWidget());
    }

    /**
     * Adds a sidebar for selected players associated with this {@link GlobalWidgets} instance.
     *
     * @param playerChecker function returning true for players that can see this sidebar
     * @return the created {@link SidebarWidget}
     */
    public SidebarWidget addSidebar(Text title, Predicate<ServerPlayerEntity> playerChecker) {
        return this.addWidget(new SidebarWidget(title, playerChecker));
    }

    /**
     * Adds a sidebar for selected players associated with this {@link GlobalWidgets} instance.
     *
     * @param playerChecker function returning true for players that can see this sidebar
     * @return the created {@link SidebarWidget}
     */
    public SidebarWidget addSidebar(Predicate<ServerPlayerEntity> playerChecker) {
        return this.addWidget(new SidebarWidget(playerChecker));
    }

    /**
     * Adds a scrollable sidebar for all players associated with this {@link GlobalWidgets} instance.
     *
     * @param title the title for the sidebar
     * @return the created {@link SidebarWidget}
     */
    public ScrollableSidebarWidget addScrollableSidebar(Text title, int ticksPerLine) {
        return this.addWidget(new ScrollableSidebarWidget(title, ticksPerLine));
    }

    /**
     * Adds a scrollable sidebar for all players associated with this {@link GlobalWidgets} instance.
     *
     * @return the created {@link SidebarWidget}
     */
    public ScrollableSidebarWidget addScrollableSidebar(int ticksPerLine) {
        return this.addWidget(new ScrollableSidebarWidget(ticksPerLine));
    }

    /**
     * Adds a scrollable sidebar for selected players associated with this {@link GlobalWidgets} instance.
     *
     * @param playerChecker function returning true for players that can see this sidebar
     * @return the created {@link SidebarWidget}
     */
    public ScrollableSidebarWidget addScrollableSidebar(Text title, int ticksPerLine, Predicate<ServerPlayerEntity> playerChecker) {
        return this.addWidget(new ScrollableSidebarWidget(title, ticksPerLine, playerChecker));
    }

    /**
     * Adds a scrollable sidebar for selected players associated with this {@link GlobalWidgets} instance.
     *
     * @param playerChecker function returning true for players that can see this sidebar
     * @return the created {@link SidebarWidget}
     */
    public ScrollableSidebarWidget addScrollableSidebar(int ticksPerLine, Predicate<ServerPlayerEntity> playerChecker) {
        return this.addWidget(new ScrollableSidebarWidget(ticksPerLine, playerChecker));
    }

    /**
     * Adds a boss bar for all players associated with this {@link GlobalWidgets} instance.
     *
     * @param title the title for the boss bar
     * @return the created {@link BossBarWidget}
     */
    public BossBarWidget addBossBar(Text title) {
        return this.addWidget(new BossBarWidget(title));
    }

    /**
     * Adds a boss bar for all players associated with this {@link GlobalWidgets} instance.
     *
     * @param title the title for the boss bar
     * @param color the color for the boss bar
     * @param style the style for the bossbar
     * @return the created {@link BossBarWidget}
     */
    public BossBarWidget addBossBar(Text title, BossBar.Color color, BossBar.Style style) {
        return this.addWidget(new BossBarWidget(title, color, style));
    }

    /**
     * Adds a {@link GameWidget} for all players associated with this {@link GlobalWidgets} instance.
     *
     * @param widget the widget to add
     * @param <T> the type of widget being added
     * @return the added widget
     */
    public <T extends GameWidget> T addWidget(T widget) {
        for (var player : this.gameSpace.getPlayers()) {
            widget.addPlayer(player);
        }
        this.widgets.add(widget);
        return widget;
    }

    /**
     * Removes a {@link GameWidget} for all players associated with this {@link GlobalWidgets} instance.
     *
     * @param widget the widget to remove
     */
    public void removeWidget(GameWidget widget) {
        if (this.widgets.remove(widget)) {
            widget.close();
        }
    }

    private void onAddPlayer(ServerPlayerEntity player) {
        for (var widget : this.widgets) {
            widget.addPlayer(player);
        }
    }

    private void onRemovePlayer(ServerPlayerEntity player) {
        for (var widget : this.widgets) {
            widget.removePlayer(player);
        }
    }

    @Override
    public void close() {
        for (var widget : this.widgets) {
            widget.close();
        }
        this.widgets.clear();
    }
}
