package xyz.nucleoid.plasmid.game.common;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameResult;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.GameTexts;
import xyz.nucleoid.plasmid.game.common.config.PlayerConfig;
import xyz.nucleoid.plasmid.game.common.widget.BossBarWidget;
import xyz.nucleoid.plasmid.game.common.widget.SidebarWidget;
import xyz.nucleoid.plasmid.game.config.GameConfig;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.util.compatibility.AfkDisplayCompatibility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A very simple waiting lobby implementation that games can easily apply to their {@link GameActivity}.
 * <p>
 * This implements both control for minimum/maximum players as well as a countdown for game start, and additionally
 * sets some basic rules which prevent players from damaging the map or each other.
 *
 * @see GameWaitingLobby#addTo(GameActivity, PlayerConfig)
 * @see PlayerConfig
 * @see xyz.nucleoid.plasmid.game.common.team.TeamSelectionLobby
 */
public final class GameWaitingLobby {
    private static final Text WAITING_TITLE = Text.translatable("text.plasmid.game.waiting_lobby.bar.waiting");
    private static final int START_REQUESTED_COUNTDOWN = 20 * 3;

    private static final BossBar.Color WAITING_COLOR = BossBar.Color.BLUE;
    private static final BossBar.Color COUNTING_COLOR = BossBar.Color.GREEN;
    private static final BossBar.Style BOSS_BAR_STYLE = BossBar.Style.NOTCHED_10;

    private static final Text PADDING_LINE = Text.literal(" ".repeat(36));

    private final GameSpace gameSpace;
    private final PlayerConfig playerConfig;

    private final BossBarWidget bar;
    private final SidebarWidget sidebar;
    private long countdownStart = -1;
    private long countdownDuration = -1;

    private boolean startRequested;
    private boolean started;
    private List<Text> sidebarText;

    private GameWaitingLobby(GameSpace gameSpace, PlayerConfig playerConfig, BossBarWidget bar, SidebarWidget sidebar) {
        this.gameSpace = gameSpace;
        this.playerConfig = playerConfig;
        this.bar = bar;
        this.sidebar = sidebar;
    }

    /**
     * Applies this waiting lobby implementation to the given {@link GameActivity}.
     *
     * @param activity the activity to apply to
     * @param playerConfig the config that this waiting lobby should respect regarding player counts and countdowns
     */
    public static GameWaitingLobby addTo(GameActivity activity, PlayerConfig playerConfig) {
        var sourceConfig = activity.getGameSpace().getMetadata().sourceConfig();

        var widgets = GlobalWidgets.addTo(activity);
        var bar = widgets.addBossBar(WAITING_TITLE, WAITING_COLOR, BOSS_BAR_STYLE);
        var sidebar = widgets.addSidebar();
        var lobby = new GameWaitingLobby(activity.getGameSpace(), playerConfig, bar, sidebar);
        activity.deny(GameRuleType.PVP).deny(GameRuleType.FALL_DAMAGE).deny(GameRuleType.HUNGER)
                .deny(GameRuleType.CRAFTING).deny(GameRuleType.PORTALS).deny(GameRuleType.THROW_ITEMS)
                .deny(GameRuleType.INTERACTION).deny(GameRuleType.PLACE_BLOCKS).deny(GameRuleType.BREAK_BLOCKS);

        activity.listen(GameActivityEvents.TICK, lobby::onTick);
        activity.listen(GameActivityEvents.REQUEST_START, lobby::requestStart);
        activity.listen(GamePlayerEvents.SCREEN_JOINS, lobby::screenJoins);
        activity.listen(GamePlayerEvents.OFFER, lobby::offerPlayer);
        activity.listen(GamePlayerEvents.REMOVE, lobby::onRemovePlayer);


        lobby.setSidebarLines(sourceConfig.value().description());
        var title = GameConfig.shortName(sourceConfig).copy();

        if (title.getStyle().getColor() == null) {
            title.setStyle(title.getStyle().withColor(Formatting.GOLD));
        }

        if (title.getStyle().toString().contains("bold=null")) {
            title.setStyle(title.getStyle());
        }

        lobby.sidebar.setTitle(title);
        lobby.sidebar.show();

        return lobby;
    }
    public void setSidebarTitle(Text text) {
        this.sidebar.setTitle(text);
    }

    public void setSidebarLines(List<Text> texts) {
        this.sidebarText = new ArrayList<>(texts.size());

        for (var line : texts) {
            var text = line.copy();
            if (line.getStyle().getColor() == null) {
                text.setStyle(line.getStyle().withColor(Formatting.YELLOW));
            }
            this.sidebarText.add(text);
        }
    }

    private void onTick() {
        if (this.started) {
            return;
        }

        long time = this.gameSpace.getTime();

        if (time % 20 == 0) {
            this.updateCountdown();
            this.tickCountdownBar();
            this.tickSidebar();
            this.tickCountdownSound();
        }

        if (this.countdownStart != -1 && time >= this.countdownStart + this.countdownDuration) {
            this.started = true;

            var startResult = this.gameSpace.requestStart();
            if (startResult.isError()) {
                MutableText message = Text.translatable("text.plasmid.game.waiting_lobby.bar.cancel").append(startResult.error());
                this.gameSpace.getPlayers().sendMessage(message.formatted(Formatting.RED));
                this.started = false;
                this.startRequested = false;
                this.countdownStart = -1;
            }
        }
    }

    @Nullable
    private GameResult requestStart() {
        if (this.gameSpace.getPlayers().size() < this.playerConfig.minPlayers()) {
            return GameResult.error(GameTexts.Start.notEnoughPlayers());
        }

        if (!this.started) {
            // consume the start request but initiate countdown
            this.startRequested = true;
            return GameResult.ok();
        } else {
            // we allow the actual start logic to pass through now
            return null;
        }
    }

    private GameResult screenJoins(Collection<ServerPlayerEntity> players) {
        int newPlayerCount = this.gameSpace.getPlayers().size() + players.size();
        if (newPlayerCount > this.playerConfig.maxPlayers()) {
            return GameResult.error(GameTexts.Join.gameFull());
        }

        return GameResult.ok();
    }

    private PlayerOfferResult offerPlayer(PlayerOffer offer) {
        if (this.isFull()) {
            return offer.reject(GameTexts.Join.gameFull());
        }

        this.updateCountdown();
        return offer.pass();
    }

    private void onRemovePlayer(ServerPlayerEntity player) {
        this.updateCountdown();
    }

    private void updateCountdown() {
        long targetDuration = this.getTargetCountdownDuration();
        if (targetDuration != this.countdownDuration) {
            this.updateCountdown(targetDuration);
        }
    }

    private void updateCountdown(long targetDuration) {
        if (targetDuration != -1) {
            long time = this.gameSpace.getTime();
            long startTime = time;

            if (this.countdownStart != -1) {
                long countdownEnd = this.countdownStart + this.countdownDuration;
                long timeRemaining = countdownEnd - time;

                long remainingDuration = Math.min(timeRemaining, targetDuration);
                startTime = Math.min(time, time + remainingDuration - targetDuration);
            }

            this.countdownStart = startTime;
            this.countdownDuration = targetDuration;
        } else {
            this.countdownStart = -1;
            this.countdownDuration = -1;
        }
    }

    private long getTargetCountdownDuration() {
        var countdown = this.playerConfig.countdown();
        if (this.startRequested) {
            return START_REQUESTED_COUNTDOWN;
        }

        if (this.gameSpace.getPlayers().size() >= this.playerConfig.minPlayers()) {
            if (this.isActiveFull()) {
                return countdown.fullSeconds() * 20L;
            } else if (this.isReady()) {
                return countdown.readySeconds() * 20L;
            }
        }

        return -1;
    }

    private void tickCountdownBar() {
        if (this.countdownStart != -1) {
            long time = this.gameSpace.getTime();
            long remainingTicks = this.getRemainingTicks(time);
            long remainingSeconds = remainingTicks / 20;

            this.bar.setTitle(Text.translatable("text.plasmid.game.waiting_lobby.bar.countdown", remainingSeconds));
            this.bar.setStyle(COUNTING_COLOR, BOSS_BAR_STYLE);
            this.bar.setProgress((float) remainingTicks / this.countdownDuration);
        } else {
            this.bar.setTitle(WAITING_TITLE);
            this.bar.setStyle(WAITING_COLOR, BOSS_BAR_STYLE);
            this.bar.setProgress(1.0F);
        }
    }

    private void tickSidebar() {
        this.sidebar.set((b) -> {
            b.add(PADDING_LINE);
            if (this.countdownStart != -1) {
                long time = this.gameSpace.getTime();
                long remainingTicks = this.getRemainingTicks(time);
                long remainingSeconds = remainingTicks / 20;

                b.add(Text.translatable("text.plasmid.game.waiting_lobby.bar.countdown", remainingSeconds));
            } else {
                b.add(WAITING_TITLE);
            }
            b.add(ScreenTexts.EMPTY);
            b.add(Text.translatable("text.plasmid.game.waiting_lobby.sidebar.players",
                    Text.literal("" + this.gameSpace.getPlayers().size()).formatted(Formatting.AQUA),
                    Text.literal("/").formatted(Formatting.GRAY),
                    Text.literal("" + this.playerConfig.maxPlayers()).formatted(Formatting.AQUA)));

            if (this.sidebarText != null && !this.sidebarText.isEmpty()) {
                b.add(ScreenTexts.EMPTY);
                for (Text text : this.sidebarText) {
                    b.add(text);
                }
            }
            b.add(ScreenTexts.EMPTY);
        });
    }

    private void tickCountdownSound() {
        if (this.countdownStart != -1) {
            long time = this.gameSpace.getTime();
            long remainingSeconds = this.getRemainingTicks(time) / 20;

            if (remainingSeconds <= 3) {
                var players = this.gameSpace.getPlayers();

                float pitch = remainingSeconds == 0 ? 1.5F : 1.0F;
                players.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0F, pitch);
            }
        }
    }

    private long getRemainingTicks(long time) {
        return Math.max(this.countdownStart + this.countdownDuration - time, 0);
    }

    private boolean isReady() {
        return this.gameSpace.getPlayers().size() >= this.playerConfig.thresholdPlayers();
    }

    private boolean isFull() {
        return this.gameSpace.getPlayers().size() >= this.playerConfig.maxPlayers();
    }

    private boolean isActiveFull() {
        if (this.isFull()) {
            return true;
        }

        // if all players on the server are in this lobby
        var server = this.gameSpace.getServer();
        if (this.gameSpace.getPlayers().size() >= server.getCurrentPlayerCount()) {
            return true;
        }

        // if there are no players outside of a game on the server
        for (var world : server.getWorlds()) {
            if (!hasActivePlayer(world) && !GameSpaceManager.get().hasGame(world)) {
                return false;
            }
        }

        return true;
    }

    private static boolean hasActivePlayer(ServerWorld world) {
        for (var player : world.getPlayers()) {
            if (AfkDisplayCompatibility.isActive(player)) {
                return true;
            }
        }

        return false;
    }
}
