package xyz.nucleoid.plasmid.widget;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.player.MutablePlayerSet;

public final class BossBarWidget implements GameWidget {
    private final MutablePlayerSet players;
    private final ServerBossBar bar;

    public BossBarWidget(GameSpace gameSpace, Text title, BossBar.Color color, BossBar.Style style) {
        this(gameSpace.getServer(), title, color, style);
    }

    public BossBarWidget(GameSpace gameSpace, Text title) {
        this(gameSpace.getServer(), title);
    }

    public BossBarWidget(MinecraftServer server, Text title, BossBar.Color color, BossBar.Style style) {
        this.players = new MutablePlayerSet(server);
        this.bar = new ServerBossBar(title, color, style);
        this.bar.setDarkenSky(false);
        this.bar.setThickenFog(false);
        this.bar.setDragonMusic(false);
    }

    public BossBarWidget(MinecraftServer server, Text title) {
        this(server, title, BossBar.Color.PURPLE, BossBar.Style.PROGRESS);
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
    public void addPlayer(ServerPlayerEntity player) {
        this.bar.addPlayer(player);
    }

    @Override
    public void removePlayer(ServerPlayerEntity player) {
        this.bar.removePlayer(player);
    }

    @Override
    public void close() {
        this.players.clear();

        this.bar.clearPlayers();
        this.bar.setVisible(false);
    }
}
