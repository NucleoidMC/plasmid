package xyz.nucleoid.plasmid.widget;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.Game;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.event.GameCloseListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerRemoveListener;

import java.util.ArrayList;
import java.util.List;

public final class GlobalWidgets {
    private final GameWorld gameWorld;
    private final List<GameWidget> widgets = new ArrayList<>();

    public GlobalWidgets(GameWorld gameWorld, Game game) {
        this.gameWorld = gameWorld;

        game.on(PlayerAddListener.EVENT, this::onPlayerAdd);
        game.on(PlayerRemoveListener.EVENT, this::onPlayerRemove);
        game.on(GameCloseListener.EVENT, this::onClose);
    }

    public SidebarWidget addSidebar(Text title) {
        return this.addWidget(new SidebarWidget(this.gameWorld, title));
    }

    public BossBarWidget addBossBar(Text title) {
        return this.addWidget(new BossBarWidget(this.gameWorld, title));
    }

    public BossBarWidget addBossBar(Text title, BossBar.Color color, BossBar.Style style) {
        return this.addWidget(new BossBarWidget(this.gameWorld, title, color, style));
    }

    public <T extends GameWidget> T addWidget(T widget) {
        this.widgets.add(widget);
        return widget;
    }

    private void onPlayerAdd(ServerPlayerEntity player) {
        for (GameWidget widget : this.widgets) {
            widget.addPlayer(player);
        }
    }

    private void onPlayerRemove(ServerPlayerEntity player) {
        for (GameWidget widget : this.widgets) {
            widget.removePlayer(player);
        }
    }

    private void onClose() {
        for (GameWidget widget : this.widgets) {
            widget.close();
        }
    }
}
