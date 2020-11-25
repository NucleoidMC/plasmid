package xyz.nucleoid.plasmid.game;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
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

public final class GameWaitingLobby {
    private static final Text WAITING_TITLE = new TranslatableText("text.plasmid.game.waiting_lobby.bar.waiting");

    private final GameSpace gameSpace;
    private final PlayerConfig playerConfig;

    private final BossBarWidget bar;
    private long countdownStart = -1;
    private long countdownDuration = -1;

    private boolean started;

    private GameWaitingLobby(GameSpace gameSpace, PlayerConfig playerConfig, BossBarWidget bar) {
        this.gameSpace = gameSpace;
        this.playerConfig = playerConfig;
        this.bar = bar;
    }

    public static void applyTo(GameLogic logic, PlayerConfig playerConfig) {
        GlobalWidgets widgets = new GlobalWidgets(logic);
        BossBarWidget bar = widgets.addBossBar(WAITING_TITLE);

        GameWaitingLobby lobby = new GameWaitingLobby(logic.getSpace(), playerConfig, bar);

        logic.setRule(GameRule.CRAFTING, RuleResult.DENY);
        logic.setRule(GameRule.PORTALS, RuleResult.DENY);
        logic.setRule(GameRule.PVP, RuleResult.DENY);
        logic.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
        logic.setRule(GameRule.HUNGER, RuleResult.DENY);
        logic.setRule(GameRule.THROW_ITEMS, RuleResult.DENY);
        logic.setRule(GameRule.INTERACTION, RuleResult.DENY);

        logic.on(GameTickListener.EVENT, lobby::onTick);
        logic.on(RequestStartListener.EVENT, lobby::requestStart);
        logic.on(OfferPlayerListener.EVENT, lobby::offerPlayer);
        logic.on(PlayerRemoveListener.EVENT, lobby::onRemovePlayer);
    }

    private void onTick() {
        if (this.started) {
            return;
        }

        long time = this.gameSpace.getWorld().getTime();

        if (this.countdownStart != -1 && time >= this.countdownStart + this.countdownDuration) {
            this.started = true;
            this.gameSpace.requestStart().thenAccept(startResult -> {
                if (startResult.isError()) {
                    MutableText message = new TranslatableText("text.plasmid.game.waiting_lobby.bar.cancel").append(startResult.getError());
                    this.gameSpace.getPlayers().sendMessage(message.formatted(Formatting.RED));
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
        if (this.gameSpace.getPlayerCount() < this.playerConfig.getMinPlayers()) {
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
                this.countdownStart = this.gameSpace.getWorld().getTime();
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
            long time = this.gameSpace.getWorld().getTime();
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
        return this.gameSpace.getPlayerCount() >= this.playerConfig.getThresholdPlayers();
    }

    private boolean isFull() {
        return this.gameSpace.getPlayerCount() >= this.playerConfig.getMaxPlayers();
    }
}
