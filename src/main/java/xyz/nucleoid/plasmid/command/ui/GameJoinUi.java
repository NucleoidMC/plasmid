package xyz.nucleoid.plasmid.command.ui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.game.manager.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.player.GamePlayerJoiner;

import java.util.ArrayList;
import java.util.Comparator;

// Todo: Maybe add paging support?
public class GameJoinUi extends SimpleGui {
    private int tick;

    public GameJoinUi(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, true);
        this.setTitle(new TranslatableText("text.plasmid.ui.game_join.title"));
        this.updateGames();
    }

    @Override
    public void onTick() {
        super.onTick();
        this.tick++;
        if (this.tick % 20 == 0) {
            this.updateGames();
        }
    }

    private void updateGames() {
        int i = 0;

        var games = new ArrayList<>(GameSpaceManager.get().getOpenGameSpaces());
        games.sort(Comparator.comparingInt(space -> space.getPlayers().size()));

        for (ManagedGameSpace gameSpace : games) {
            if (this.getFirstEmptySlot() != -1) {
                this.setSlot(i++, this.createIconFor(gameSpace));
            }
        }

        for (; i < this.getSize(); i++) {
            this.clearSlot(i);
        }
    }


    private GuiElementBuilder createIconFor(GameSpace gameSpace) {
        var sourceConfig = gameSpace.getMetadata().sourceConfig();
        var element = GuiElementBuilder.from(sourceConfig.icon().copy())
                .setName(sourceConfig.name().shallowCopy());

        for (var line : sourceConfig.description()) {
            var text = line.shallowCopy();

            if (line.getStyle().getColor() == null) {
                text.setStyle(line.getStyle().withColor(Formatting.GRAY));
            }

            element.addLoreLine(text);
        }
        element.addLoreLine(LiteralText.EMPTY);
        element.addLoreLine(new LiteralText("")
                .append(new LiteralText("» ").formatted(Formatting.DARK_GRAY))
                .append(new TranslatableText("text.plasmid.ui.game_join.players",
                        new LiteralText(gameSpace.getPlayers().size() + "").formatted(Formatting.YELLOW)).formatted(Formatting.GOLD))
        );

        element.setCallback((a, b, c, d) -> tryJoinGame(this.getPlayer(), gameSpace));

        return element;
    }

    private static void tryJoinGame(ServerPlayerEntity player, GameSpace gameSpace) {
        player.server.submit(() -> {
            var results = GamePlayerJoiner.tryJoin(player, gameSpace);
            results.sendErrorsTo(player);
        });
    }
}
