package xyz.nucleoid.plasmid.impl.command.ui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.gui.SimpleGui;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.api.util.PlayerRef;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;
import xyz.nucleoid.plasmid.impl.game.manager.ManagedGameSpace;
import xyz.nucleoid.plasmid.api.game.player.GamePlayerJoiner;
import xyz.nucleoid.plasmid.api.game.player.JoinIntent;
import xyz.nucleoid.plasmid.api.util.Guis;
import xyz.nucleoid.plasmid.impl.portal.GamePortalBackend;

import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

public class GameJoinUi extends SimpleGui {
    private static final GuiElement EMPTY = new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray()).hideTooltip().build();

    private static final int NAVBAR_POS = 81;
    private final ServerPlayer player;
    private final JoinIntent joinIntent;
    private int tick;
    private int page = 0;
    private int pageSize;

    public GameJoinUi(ServerPlayer player, JoinIntent intent) {
        super(MenuType.GENERIC_9x6, player, true);
        this.joinIntent = intent;
        this.player = player;
        this.setTitle(Component.translatable("text.plasmid.ui.game_join.title"));
        this.updateUi();
    }

    private static void tryJoinGame(ServerPlayer player, GameSpace gameSpace, JoinIntent joinIntent) {
        player.level().getServer().execute(() -> {
            var result = GamePlayerJoiner.tryJoin(player, gameSpace, joinIntent);
            if (result.isError()) {
                player.sendSystemMessage(result.errorCopy().withStyle(ChatFormatting.RED));
            }
        });
    }

    @Override
    public void onTick() {
        super.onTick();
        this.tick++;
        if (this.tick % 20 == 0) {
            this.updateUi();
        }
    }

    private void updateUi() {
        PlayerRef playerRef = PlayerRef.of(this.player);
        int i = 0;
        int gameI = 0;

        var games = new ArrayList<>(GameSpaceManagerImpl.get().getOpenGameSpaces());
        games.removeIf((gameSpace -> !gameSpace.isPlayerAllowed(playerRef)));
        games.sort(Comparator.comparingInt(space -> -space.getPlayers().size()));

        int limit = this.size;
        this.pageSize = 0;

        if (games.size() > this.size) {
            limit = NAVBAR_POS;
            this.pageSize = games.size() / NAVBAR_POS;
        }

        this.page = Mth.clamp(this.page, 0, this.pageSize);

        for (var gameSpace : games) {
            if (gameI >= this.page * NAVBAR_POS) {
                if (i < limit) {
                    this.setSlot(i++, this.createIconFor(gameSpace));
                }
            }
            gameI++;
        }

        for (; i < limit; i++) {
            this.clearSlot(i);
        }

        if (this.pageSize != 0) {
            boolean hasPrevious = this.page != 0;
            boolean hasNext = this.page < this.pageSize;

            this.setSlot(NAVBAR_POS, EMPTY);
            this.setSlot(NAVBAR_POS + 1, EMPTY);

            this.setSlot(NAVBAR_POS + 2, new GuiElementBuilder(hasPrevious ? Items.STAINED_GLASS_PANE.lime() : Items.STAINED_GLASS_PANE.black())
                    .setName(Component.translatable("spectatorMenu.previous_page").withStyle(hasPrevious ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY))
                    .setCallback(() -> this.changePage(-1))
            );
            int pageValue = this.page + 1;

            var registries = this.player.registryAccess();
            this.setSlot(NAVBAR_POS + 3, Guis.getNumericBanner(registries, pageValue / 100));
            this.setSlot(NAVBAR_POS + 4, Guis.getNumericBanner(registries, pageValue / 10));
            this.setSlot(NAVBAR_POS + 5, Guis.getNumericBanner(registries, pageValue));

            this.setSlot(NAVBAR_POS + 6, new GuiElementBuilder(hasNext ? Items.STAINED_GLASS_PANE.lime() : Items.STAINED_GLASS_PANE.black())
                    .setName(Component.translatable("spectatorMenu.next_page").withStyle(hasNext ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY))
                    .setCallback(() -> this.changePage(1))
            );

            this.setSlot(NAVBAR_POS + 7, EMPTY);
            this.setSlot(NAVBAR_POS + 8, EMPTY);
        }
    }

    private void changePage(int change) {
        this.page = Mth.clamp(this.page + change, 0, this.pageSize);
        this.updateUi();
    }

    private GuiElementBuilder createIconFor(GameSpace gameSpace) {
        var state = gameSpace.getState();
        var sourceConfig = gameSpace.getMetadata().sourceConfig();
        var element = GuiElementBuilder.from(sourceConfig.value().icon().copy()).hideDefaultTooltip()
                .setName(GameConfig.name(sourceConfig).copy());

        for (var line : sourceConfig.value().description()) {
            var text = line.copy();

            if (line.getStyle().getColor() == null) {
                text.setStyle(line.getStyle().withColor(ChatFormatting.GRAY));
            }

            element.addLoreLine(text);
        }

        boolean allowSpace = true;

        if (!state.state().hidden()) {
            element.addLoreLine(CommonComponents.EMPTY);
            element.addLoreLine(Component.literal(" ").append(state.state().display()).withStyle(ChatFormatting.WHITE));
            allowSpace = false;
        }

        if (state.players() > -1) {
            if (allowSpace) {
                element.addLoreLine(CommonComponents.EMPTY);
                allowSpace = false;
            }
            element.addLoreLine(Component.empty()
                    .append(Component.literal("» ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.translatable("text.plasmid.ui.game_join.players",
                            Component.literal(state.players() + (state.maxPlayers() > 0 ? " / " + state.maxPlayers() : "")).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GOLD))
            );
        }

        if (state.spectators() > 0) {
            if (allowSpace) {
                element.addLoreLine(CommonComponents.EMPTY);
                allowSpace = false;
            }

            element.addLoreLine(Component.empty()
                    .append(Component.literal("» ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.translatable("text.plasmid.ui.game_join.spectators",
                            Component.literal( state.spectators() + "").withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GOLD))
            );
        }

        var actionType = this.joinIntent == JoinIntent.PLAY ? GamePortalBackend.ActionType.PLAY : GamePortalBackend.ActionType.SPECTATE;

        if (actionType != GamePortalBackend.ActionType.NONE) {
            element.addLoreLine(Component.empty().append(Component.literal(" [ ").withStyle(ChatFormatting.GRAY))
                    .append(actionType.text())
                    .append(Component.literal(" ]").withStyle(ChatFormatting.GRAY)).setStyle(Style.EMPTY.withColor(0x76ed6f)));
        }

        element.hideDefaultTooltip();
        element.setCallback((a, b, c, d) -> tryJoinGame(this.getPlayer(), gameSpace, joinIntent));

        return element;
    }
}
