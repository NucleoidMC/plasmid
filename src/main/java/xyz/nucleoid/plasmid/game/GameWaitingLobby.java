package xyz.nucleoid.plasmid.game;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;
import xyz.nucleoid.plasmid.game.event.GameTickListener;
import xyz.nucleoid.plasmid.game.event.OfferPlayerListener;
import xyz.nucleoid.plasmid.game.event.PlayerRemoveListener;
import xyz.nucleoid.plasmid.game.event.RequestStartListener;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.widget.BossBarWidget;
import xyz.nucleoid.plasmid.widget.GlobalWidgets;

import javax.annotation.Nullable;

public final class GameWaitingLobby {
    private static final Text WAITING_TITLE = new TranslatableText("text.plasmid.game.waiting_lobby.bar.waiting");

    private final GameWorld gameWorld;
    private final PlayerConfig playerConfig;

    private final BossBarWidget bar;
    private long countdownStart = -1;
    private long countdownDuration = -1;

    private boolean started;

    private GameWaitingLobby(GameWorld gameWorld, PlayerConfig playerConfig, BossBarWidget bar) {
        this.gameWorld = gameWorld;
        this.playerConfig = playerConfig;
        this.bar = bar;
    }

    public static void addTo(Game game, PlayerConfig playerConfig) {
        GlobalWidgets widgets = new GlobalWidgets(game.getWorld(), game);
        BossBarWidget bar = widgets.addBossBar(WAITING_TITLE);

        GameWaitingLobby lobby = new GameWaitingLobby(game.getWorld(), playerConfig, bar);

        game.setRule(GameRule.CRAFTING, RuleResult.DENY);
        game.setRule(GameRule.PORTALS, RuleResult.DENY);
        game.setRule(GameRule.PVP, RuleResult.DENY);
        game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
        game.setRule(GameRule.HUNGER, RuleResult.DENY);
        game.setRule(GameRule.THROW_ITEMS, RuleResult.DENY);
        game.setRule(GameRule.INTERACTION, RuleResult.DENY);

        game.on(GameTickListener.EVENT, lobby::onTick);
        game.on(RequestStartListener.EVENT, lobby::requestStart);
        game.on(OfferPlayerListener.EVENT, lobby::offerPlayer);
        game.on(PlayerRemoveListener.EVENT, lobby::onRemovePlayer);
    }

    private void onTick() {
        if (this.started) {
            return;
        }

        long time = this.gameWorld.getWorld().getTime();

        if (this.countdownStart != -1 && time >= this.countdownStart + this.countdownDuration) {
            this.started = true;
            this.gameWorld.requestStart().thenAccept(startResult -> {
                if (startResult.isError()) {
                    MutableText message = new TranslatableText("text.plasmid.game.waiting_lobby.bar.cancel").append(startResult.getError());
                    this.gameWorld.getPlayers().sendMessage(message.formatted(Formatting.RED));
                    this.started = false;
                }
            });
        }

        if (time % 20 == 0) {
            this.updateStartTime();
            this.tickCountdownBar();
        }
    }

    @Nullable
    private StartResult requestStart() {
        if (this.gameWorld.getPlayerCount() < this.playerConfig.getMinPlayers()) {
            return StartResult.NOT_ENOUGH_PLAYERS;
        }
        return null;
    }

    private JoinResult offerPlayer(ServerPlayerEntity player) {
        if (this.isFull()) {
            return JoinResult.gameFull();
        }

        this.updateStartTime();
        return JoinResult.ok();
    }

    private void onRemovePlayer(ServerPlayerEntity player) {
        this.updateStartTime();
    }

    private void updateStartTime() {
        long targetCountdown = this.getTargetCountdownDuration();
        if (targetCountdown != -1) {
            if (this.countdownStart == -1) {
                this.countdownStart = this.gameWorld.getWorld().getTime();
            }
            this.countdownDuration = targetCountdown;
        } else {
            this.countdownStart = -1;
            this.countdownDuration = -1;
        }
    }

    private long getTargetCountdownDuration() {
        if (this.isFull()) {
            return this.playerConfig.getCountdown().getFullTicks();
        } else if (this.isReady()) {
            return this.playerConfig.getCountdown().getReadyTicks();
        } else {
            return -1;
        }
    }

    private void tickCountdownBar() {
        if (this.countdownStart != -1) {
            long time = this.gameWorld.getWorld().getTime();
            long remainingTicks = Math.max(this.countdownStart + this.countdownDuration - time, 0);
            long remainingSeconds = remainingTicks / 20;

            this.bar.setTitle(new TranslatableText("text.plasmid.game.waiting_lobby.bar.countdown", remainingSeconds));
            this.bar.setProgress((float) remainingTicks / this.countdownDuration);
        } else {
            this.bar.setTitle(WAITING_TITLE);
            this.bar.setProgress(1.0F);
        }
    }

    private boolean isReady() {
        return this.gameWorld.getPlayerCount() >= this.playerConfig.getThresholdPlayers();
    }

    private boolean isFull() {
        return this.gameWorld.getPlayerCount() >= this.playerConfig.getMaxPlayers();
    }
}
