package xyz.nucleoid.plasmid.api.game.common.widget;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;

import java.util.UUID;

/**
 * An implementation of {@link GameWidget} which displays a boss bar at the top of players' screens.
 *
 * @see ServerBossEvent
 * @see GlobalWidgets
 */
public final class BossBarWidget implements GameWidget {
    private final ServerBossEvent bar;

    public BossBarWidget(UUID uuid, Component title, BossEvent.BossBarColor color, BossEvent.BossBarOverlay style) {
        this.bar = new ServerBossEvent(uuid, title, color, style);
        this.bar.setDarkenScreen(false);
        this.bar.setCreateWorldFog(false);
        this.bar.setPlayBossMusic(false);
    }

    public BossBarWidget(Component title) {
        this(UUID.randomUUID(), title, BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
    }

    public void setTitle(Component title) {
        this.bar.setName(title);
    }

    public void setProgress(float progress) {
        this.bar.setProgress(progress);
    }

    public void setStyle(BossEvent.BossBarColor color, BossEvent.BossBarOverlay style) {
        this.bar.setColor(color);
        this.bar.setOverlay(style);
    }

    public void setVisible(boolean visible) {
        this.bar.setVisible(visible);
    }

    @Override
    public void addPlayer(ServerPlayer player) {
        this.bar.addPlayer(player);
    }

    @Override
    public void removePlayer(ServerPlayer player) {
        this.bar.removePlayer(player);
    }

    @Override
    public void close() {
        this.bar.removeAllPlayers();
        this.bar.setVisible(false);
    }
}
