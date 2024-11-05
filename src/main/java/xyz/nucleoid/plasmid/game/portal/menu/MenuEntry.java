package xyz.nucleoid.plasmid.game.portal.menu;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import jdk.jfr.Experimental;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.portal.GamePortalBackend;

import java.util.List;
import java.util.function.Consumer;

public interface MenuEntry {
    Text name();

    List<Text> description();

    ItemStack icon();

    void click(ServerPlayerEntity player);

    default int getPlayerCount() {
        return -1;
    }

    default int getSpectatorCount() {
        return -1;
    }

    default GamePortalBackend.ActionType getActionType() {
        return GamePortalBackend.ActionType.NONE;
    }

    default void provideGameSpaces(Consumer<GameSpace> consumer) {

    }

    default GuiElement createGuiElement() {
        var element = GuiElementBuilder.from(this.icon().copy()).hideDefaultTooltip()
                .setName(Text.empty().append(this.name()));

        for (var line : this.description()) {
            var text = line.copy();

            if (line.getStyle().getColor() == null) {
                text.setStyle(line.getStyle().withFormatting(Formatting.GRAY));
            }

            element.addLoreLine(text);
        }

        var playerCount = this.getPlayerCount();
        var spectatorCount = this.getSpectatorCount();
        boolean allowSpace = true;

        if (playerCount > -1) {
            if (allowSpace) {
                element.addLoreLine(ScreenTexts.EMPTY);
                allowSpace = false;
            }
            element.addLoreLine(Text.empty()
                    .append(Text.literal("» ").formatted(Formatting.DARK_GRAY))
                    .append(Text.translatable("text.plasmid.ui.game_join.players",
                            Text.literal(playerCount + "").formatted(Formatting.YELLOW)).formatted(Formatting.GOLD))
            );
        }

        if (spectatorCount > -1) {
            if (allowSpace) {
                element.addLoreLine(ScreenTexts.EMPTY);
                allowSpace = false;
            }

            element.addLoreLine(Text.empty()
                    .append(Text.literal("» ").formatted(Formatting.DARK_GRAY))
                    .append(Text.translatable("text.plasmid.ui.game_join.spectators",
                            Text.literal(playerCount + "").formatted(Formatting.YELLOW)).formatted(Formatting.GOLD))
            );
        }

        var actionType = this.getActionType();

        if (actionType != GamePortalBackend.ActionType.NONE) {
            element.addLoreLine(Text.empty().append(Text.literal(" [ ").formatted(Formatting.GRAY))
                    .append(actionType.text())
                    .append(Text.literal(" ]").formatted(Formatting.GRAY)).setStyle(Style.EMPTY.withColor(0x76ed6f)));
        }

        element.setCallback((a, b, c, gui) -> {
            this.click(gui.getPlayer());
        });

        return element.build();
    }
}