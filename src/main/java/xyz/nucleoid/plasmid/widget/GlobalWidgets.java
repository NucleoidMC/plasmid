package xyz.nucleoid.plasmid.widget;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.GameLogic;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.GameCloseListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerRemoveListener;

import java.util.ArrayList;
import java.util.List;

public final class GlobalWidgets {
    private final GameSpace gameSpace;
    private final List<GameWidget> widgets = new ArrayList<>();

    public GlobalWidgets(GameSpace gameSpace, GameLogic logic) {
        this.gameSpace = gameSpace;

        logic.on(PlayerAddListener.EVENT, this::onPlayerAdd);
        logic.on(PlayerRemoveListener.EVENT, this::onPlayerRemove);
        logic.on(GameCloseListener.EVENT, this::onClose);
    }

    public SidebarWidget addSidebar(Text title) {
        return this.addWidget(new SidebarWidget(this.gameSpace, title));
    }

    public BossBarWidget addBossBar(Text title) {
        return this.addWidget(new BossBarWidget(this.gameSpace, title));
    }

    public BossBarWidget addBossBar(Text title, BossBar.Color color, BossBar.Style style) {
        return this.addWidget(new BossBarWidget(this.gameSpace, title, color, style));
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
