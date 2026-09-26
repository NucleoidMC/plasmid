package xyz.nucleoid.plasmid.api.game.common;

import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.sgui.api.SguiUtils;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.*;
import xyz.nucleoid.plasmid.api.game.common.config.WaitingLobbyConfig;
import xyz.nucleoid.plasmid.api.game.common.team.TeamSelectionLobby;
import xyz.nucleoid.plasmid.api.game.common.ui.WaitingLobbyUiLayout;
import xyz.nucleoid.plasmid.api.game.common.widget.BossBarWidget;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.event.GameWaitingLobbyEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.plasmid.api.game.player.JoinOfferResult;
import xyz.nucleoid.plasmid.api.game.player.PlayerSet;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.api.game.common.widget.SidebarWidget;
import xyz.nucleoid.plasmid.api.util.PlayerRef;
import xyz.nucleoid.plasmid.impl.game.common.ui.WaitingLobbyUi;
import xyz.nucleoid.plasmid.impl.game.common.ui.element.LeaveGameWaitingLobbyUiElement;
import xyz.nucleoid.plasmid.impl.game.common.ui.element.ReadyGameWaitingLobbyUiElement;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;
import xyz.nucleoid.plasmid.impl.compatibility.AfkDisplayCompatibility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;

/**
 * A very simple waiting lobby implementation that games can easily apply to their {@link GameActivity}.
 * <p>
 * This implements both control for minimum/maximum players and a countdown for game start, and additionally
 * sets some basic rules which prevent players from damaging the map or each other.
 *
 * @see GameWaitingLobby#addTo(GameActivity, WaitingLobbyConfig)
 * @see WaitingLobbyConfig
 * @see TeamSelectionLobby
 */
public final class GameWaitingLobby {
    private static final Component WAITING_TITLE = Component.translatable("text.plasmid.game.waiting_lobby.bar.waiting");
    private static final int START_REQUESTED_COUNTDOWN = 20 * 3;

    private static final BossEvent.BossBarColor WAITING_COLOR = BossEvent.BossBarColor.BLUE;
    private static final BossEvent.BossBarColor COUNTING_COLOR = BossEvent.BossBarColor.GREEN;
    private static final BossEvent.BossBarOverlay BOSS_BAR_STYLE = BossEvent.BossBarOverlay.NOTCHED_10;

    private static final Component PADDING_LINE = Component.literal(" ".repeat(36));

    private final GameSpace gameSpace;
    private final WaitingLobbyConfig playerConfig;

    private final PlayerLimiter limiter;
    private final BossBarWidget bar;
    private final SidebarWidget sidebar;
    private long countdownStart = -1;
    private long countdownDuration = -1;

    private final ArrayList<ServerPlayer> playerVotes = new ArrayList<>();
    private final HashMap<ServerPlayer, WaitingLobbyUiLayout> playerToLayout = new HashMap<>();
    private boolean startRequested;
    private boolean started;
    private List<Component> sidebarText;

    private GameWaitingLobby(GameSpace gameSpace, WaitingLobbyConfig playerConfig, BossBarWidget bar, SidebarWidget sidebar, PlayerLimiter limiter) {
        this.gameSpace = gameSpace;
        this.playerConfig = playerConfig;
        this.bar = bar;
        this.sidebar = sidebar;
        this.limiter = limiter;
    }

    /**
     * Applies this waiting lobby implementation to the given {@link GameActivity}.
     *
     * @param activity the activity to apply to
     * @param playerConfig the config that this waiting lobby should respect regarding player counts and countdowns
     */
    public static GameWaitingLobby addTo(GameActivity activity, WaitingLobbyConfig playerConfig) {
        var sourceConfig = activity.getGameSpace().getMetadata().sourceConfig();

        var widgets = GlobalWidgets.addTo(activity);
        var bar = widgets.addBossBar(WAITING_TITLE, WAITING_COLOR, BOSS_BAR_STYLE);
        var sidebar = widgets.addSidebar();
        var limiter = PlayerLimiter.addTo(activity, playerConfig.playerConfig());
        var lobby = new GameWaitingLobby(activity.getGameSpace(), playerConfig, bar, sidebar, limiter);
        activity.deny(GameRuleType.PVP).deny(GameRuleType.FALL_DAMAGE).deny(GameRuleType.HUNGER)
                .deny(GameRuleType.CRAFTING).deny(GameRuleType.PORTALS).deny(GameRuleType.THROW_ITEMS)
                .deny(GameRuleType.INTERACTION).deny(GameRuleType.PLACE_BLOCKS).deny(GameRuleType.BREAK_BLOCKS);

        activity.listen(GameActivityEvents.TICK, lobby::onTick);
        activity.listen(GameActivityEvents.REQUEST_START, lobby::requestStart);
        activity.listen(GamePlayerEvents.OFFER, lobby::offerPlayer);
        activity.listen(GamePlayerEvents.ADD, lobby::onAddPlayer);
        activity.listen(GamePlayerEvents.REMOVE, lobby::onRemovePlayer);
        activity.listen(GamePlayerEvents.JOIN_MESSAGE, lobby::onJoinMessage);
        activity.listen(GamePlayerEvents.LEAVE_MESSAGE, lobby::onLeaveMessage);
        activity.listen(GameWaitingLobbyEvents.BUILD_UI_LAYOUT, lobby::onBuildUiLayout);

        activity.listen(GameActivityEvents.STATE_UPDATE, lobby::updateState);

        lobby.setSidebarLines(sourceConfig.value().description());
        var title = GameConfig.shortName(sourceConfig).copy();

        if (title.getStyle().getColor() == null) {
            title.setStyle(title.getStyle().withColor(ChatFormatting.GOLD));
        }

        if (title.getStyle().toString().contains("bold=null")) {
            title.setStyle(title.getStyle());
        }

        lobby.sidebar.setTitle(title);
        lobby.updateSidebar();
        lobby.sidebar.show();

        return lobby;
    }

    @Nullable
    private Component onJoinMessage(ServerPlayer player, Component currentText, Component defaultText) {
        if (currentText == null || (this.playerConfig.thresholdPlayers() == 1 && this.playerConfig.minPlayers() == 1) || this.playerConfig.playerConfig().maxPlayers().isEmpty() || this.gameSpace.getPlayers().spectators().contains(player)) {
            return currentText;
        }
        var count = this.gameSpace.getPlayers().participants().size();
        var canStart = count >= this.playerConfig.minPlayers() && (this.isActiveFull(this.gameSpace.getPlayers().size()) || this.isReady(count));

        if (canStart) {
            return currentText;
        }

        var required = Math.max(Math.min(this.playerConfig.thresholdPlayers(),
                this.gameSpace.getServer().getPlayerCount()
        ), this.playerConfig.minPlayers()) - count;

        if (count >= this.playerConfig.minPlayers()) {
            this.showVoteReminder();
            for (ServerPlayer plr : this.gameSpace.getPlayers().participants()) {
                WaitingLobbyUiLayout layout = this.playerToLayout.get(plr);
                if (layout != null) {
                    layout.refresh();
                }
            }
        }

        return Component.empty()
                .append(currentText)
                .append(" ")
                .append(Component.translatable("text.plasmid.game.waiting_lobby.players_needed_to_start", required).withStyle(ChatFormatting.YELLOW));
    }

    @Nullable
    private Component onLeaveMessage(ServerPlayer player, Component currentText, Component defaultText) {
        if (currentText == null || (this.playerConfig.thresholdPlayers() == 1 && this.playerConfig.minPlayers() == 1) || this.playerConfig.playerConfig().maxPlayers().isEmpty() || this.gameSpace.getPlayers().spectators().contains(player)) {
            return currentText;
        }

        var count = this.gameSpace.getPlayers().participants().size() - 1;
        var canStart = count >= this.playerConfig.minPlayers() && (this.isActiveFull(this.gameSpace.getPlayers().size() - 1) || this.isReady(count));


        if (canStart) {
            return currentText;
        }

        var required = Math.max(Math.min(this.playerConfig.thresholdPlayers(),
                this.gameSpace.getServer().getPlayerCount()
        ), this.playerConfig.minPlayers()) - count;

        return Component.empty()
                .append(currentText)
                .append(" ")
                .append(Component.translatable("text.plasmid.game.waiting_lobby.players_needed_to_start", required).withStyle(ChatFormatting.YELLOW));
    }

    private GameSpaceState.Builder updateState(GameSpaceState.Builder builder) {
        return builder.state(this.getTargetCountdownDuration() != -1 ? GameSpaceState.State.STARTING : GameSpaceState.State.WAITING);
    }

    public void setSidebarTitle(Component text) {
        this.sidebar.setTitle(text);
    }

    public void setSidebarLines(List<Component> texts) {
        this.sidebarText = new ArrayList<>(texts.size());

        for (var line : texts) {
            var text = line.copy();
            if (line.getStyle().getColor() == null) {
                text.setStyle(line.getStyle().withColor(ChatFormatting.YELLOW));
            }
            this.sidebarText.add(text);
        }
    }
    private void showVoteReminder() {
        if (this.started) {
            return;
        }
        PlayerSet participants = this.gameSpace.getPlayers().participants();
        MutableComponent styledText = Component.translatable("text.plasmid.game.waiting_lobby.player_vote_click_here", this.playerVotes.size(), String.format("%.0f", Math.ceil((double) participants.size() / 2)))
                .withStyle((style -> style
                        .withColor(ChatFormatting.BLUE)));
        participants.sendActionBar(Component.translatable("text.plasmid.game.waiting_lobby.player_vote", this.playerConfig.minPlayers(), styledText));
    }
    private void onTick() {
        if (this.started) {
            return;
        }

        long time = this.gameSpace.getTime();

        if (time % 20 == 0) {
            this.updateCountdown();
            this.tickCountdownBar();
            this.updateSidebar();
            this.tickCountdownSound();
        }

        if (this.countdownStart != -1 && time >= this.countdownStart + this.countdownDuration) {
            this.started = true;

            var startResult = this.gameSpace.requestStart();
            if (startResult.isError()) {
                MutableComponent message = Component.translatable("text.plasmid.game.waiting_lobby.bar.cancel").append(startResult.error());
                this.gameSpace.getPlayers().sendMessage(message.withStyle(ChatFormatting.RED));
                this.started = false;
                this.startRequested = false;
                this.countdownStart = -1;
            } else {
                for (var player : this.gameSpace.getPlayers()) {
                    var gui = SguiUtils.getCurrentGui(player);

                    if (gui instanceof WaitingLobbyUi) {
                        player.doCloseContainer();
                    }

                    PolymerUtils.reloadInventory(player);
                }
            }
        } else if (this.gameSpace.getPlayers().participants().size() >= this.playerConfig.minPlayers()) {
            this.showVoteReminder();
        }
    }

    @Nullable
    private GameResult requestStart() {
        if (this.gameSpace.getPlayers().participants().size() < this.playerConfig.minPlayers()) {
            return GameResult.error(GameComponents.Start.notEnoughPlayers());
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

    private JoinOfferResult offerPlayer(JoinOffer offer) {
        this.updateCountdown();
        return offer.pass();
    }

    private void onAddPlayer(ServerPlayer player) {
        var ui = new WaitingLobbyUi(player, this.gameSpace);
        ui.open();
    }

    private void onRemovePlayer(ServerPlayer player) {
        this.updateCountdown();
        if (this.playerVotes.remove(player) && this.gameSpace.getPlayers().participants().size() >= this.playerConfig.minPlayers()) {
            this.showVoteReminder();
        }
    }

    private void onBuildUiLayout(WaitingLobbyUiLayout layout, ServerPlayer player) {
        layout.addTrailing(new LeaveGameWaitingLobbyUiElement(this.gameSpace, player));
        layout.addTrailing(new ReadyGameWaitingLobbyUiElement(layout, this.gameSpace, this.playerConfig.minPlayers(), (hasVoted) -> {
            if (hasVoted) {
                this.playerVotes.add(player);
            } else {
                this.playerVotes.remove(player);
            }
            if (this.gameSpace.getPlayers().participants().size() >= this.playerConfig.minPlayers()) {
                double playersNeededToStart = Math.ceil((double) this.gameSpace.getPlayers().participants().size() / 2);
                if (playersNeededToStart <= this.playerVotes.size()) {
                    this.updateCountdown();
                }
                this.showVoteReminder();
            }
        }));
        this.playerToLayout.put(player, layout);
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
        if (this.gameSpace.getPlayers().participants().size() >= this.playerConfig.minPlayers()) {
            double playersNeededToStart = Math.ceil((double) this.gameSpace.getPlayers().participants().size() / 2);
            if (playersNeededToStart <= this.playerVotes.size()) {
                return START_REQUESTED_COUNTDOWN;
            } else if (this.isActiveFull(this.gameSpace.getPlayers().size())) {
                return countdown.fullSeconds() * 20L;
            } else if (this.isReady(this.gameSpace.getPlayers().participants().size())) {
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

            this.bar.setTitle(Component.translatable("text.plasmid.game.waiting_lobby.bar.countdown", remainingSeconds));
            this.bar.setStyle(COUNTING_COLOR, BOSS_BAR_STYLE);
            this.bar.setProgress((float) remainingTicks / this.countdownDuration);
        } else {
            this.bar.setTitle(WAITING_TITLE);
            this.bar.setStyle(WAITING_COLOR, BOSS_BAR_STYLE);
            this.bar.setProgress(1.0F);
        }
    }

    private void updateSidebar() {
        this.sidebar.set((b) -> {
            b.add(PADDING_LINE);
            if (this.countdownStart != -1) {
                long time = this.gameSpace.getTime();
                long remainingTicks = this.getRemainingTicks(time);
                long remainingSeconds = remainingTicks / 20;

                b.add(Component.translatable("text.plasmid.game.waiting_lobby.bar.countdown", remainingSeconds));
            } else {
                b.add(WAITING_TITLE);
            }
            b.add(CommonComponents.EMPTY);

            if (this.playerConfig.playerConfig().maxPlayers().isEmpty()) {
                b.add(Component.translatable("text.plasmid.game.waiting_lobby.sidebar.players",
                        Component.literal("" + this.gameSpace.getPlayers().participants().size()).withStyle(ChatFormatting.AQUA), "", ""));
            } else {
                b.add(Component.translatable("text.plasmid.game.waiting_lobby.sidebar.players",
                        Component.literal("" + this.gameSpace.getPlayers().participants().size()).withStyle(ChatFormatting.AQUA),
                        Component.literal("/").withStyle(ChatFormatting.GRAY),
                        Component.literal("" + this.playerConfig.playerConfig().maxPlayers().orElse(0)).withStyle(ChatFormatting.AQUA)));
            }
            if (this.sidebarText != null && !this.sidebarText.isEmpty()) {
                b.add(CommonComponents.EMPTY);
                for (Component text : this.sidebarText) {
                    b.add(text);
                }
            }
            b.add(CommonComponents.EMPTY);
        });
    }

    private void tickCountdownSound() {
        if (this.countdownStart != -1) {
            long time = this.gameSpace.getTime();
            long remainingSeconds = this.getRemainingTicks(time) / 20;

            if (remainingSeconds <= 3) {
                var players = this.gameSpace.getPlayers();

                float pitch = remainingSeconds == 0 ? 1.5F : 1.0F;
                players.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, pitch);
            }
        }
    }

    private long getRemainingTicks(long time) {
        return Math.max(this.countdownStart + this.countdownDuration - time, 0);
    }

    private boolean isReady(int count) {
        return count >= this.playerConfig.thresholdPlayers();
    }

    private boolean isFull() {
        return this.limiter.isFull();
    }

    private boolean isActiveFull(int count) {
        if (this.isFull()) {
            return true;
        }

        // if all players on the server are in this lobby
        var server = this.gameSpace.getServer();
        if (count >= server.getPlayerCount()) {
            return true;
        }

        // if all filtered players are in this lobby
        int playersThatCanJoin = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (this.gameSpace.isPlayerAllowed(PlayerRef.of(player))) {
                playersThatCanJoin++;
            }
        }
        if (count >= playersThatCanJoin) {
            return true;
        }

        // if there are no players outside of a game on the server
        for (var world : server.getAllLevels()) {
            if (hasActivePlayer(world) && !GameSpaceManagerImpl.get().hasGame(world)) {
                return false;
            }
        }

        return true;
    }

    private static boolean hasActivePlayer(ServerLevel world) {
        for (var player : world.players()) {
            if (AfkDisplayCompatibility.isActive(player)) {
                return true;
            }
        }

        return false;
    }
}
