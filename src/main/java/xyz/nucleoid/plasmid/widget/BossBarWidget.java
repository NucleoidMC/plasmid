package xyz.nucleoid.plasmid.widget;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.player.PlayerSet;

public final class BossBarWidget implements PlayerSet.Listener, AutoCloseable {
    private final PlayerSet players;
    private final ServerBossBar bar;

    private BossBarWidget(PlayerSet players, ServerBossBar bar) {
        this.players = players;
        this.bar = bar;

        this.players.addListener(this);
    }

    public static BossBarWidget open(PlayerSet players, Text title) {
        return BossBarWidget.open(players, title, BossBar.Color.PURPLE, BossBar.Style.PROGRESS);
    }

    public static BossBarWidget open(PlayerSet players, Text title, BossBar.Color color, BossBar.Style style) {
        ServerBossBar bossBar = new ServerBossBar(title, color, style);
        bossBar.setDarkenSky(false);
        bossBar.setThickenFog(false);
        bossBar.setDragonMusic(false);

        BossBarWidget bar = new BossBarWidget(players, bossBar);
        for (ServerPlayerEntity player : players) {
            bar.onAddPlayer(player);
        }

        return bar;
    }

    public void setTitle(Text title) {
        this.bar.setName(title);
    }

    public void setProgress(float progress) {
        this.bar.setPercent(progress);
    }

    public void setStyle(BossBar.Color color, BossBar.Style style) {
        this.bar.setColor(color);
        this.bar.setOverlay(style);
    }

    @Override
    public void onAddPlayer(ServerPlayerEntity player) {
        this.bar.addPlayer(player);
    }

    @Override
    public void onRemovePlayer(ServerPlayerEntity player) {
        this.bar.removePlayer(player);
    }

    @Override
    public void close() {
        this.bar.clearPlayers();
        this.bar.setVisible(false);

        this.players.removeListener(this);
    }
}
