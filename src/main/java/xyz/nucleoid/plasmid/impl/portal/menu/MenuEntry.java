package xyz.nucleoid.plasmid.impl.portal.menu;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.impl.portal.backend.GamePortalBackend;
import xyz.nucleoid.plasmid.api.game.GameSpace;

import java.util.List;
import java.util.function.Consumer;

public interface MenuEntry {
    Text name();

    List<Text> description();

    ItemStack icon();

    void click(ServerPlayerEntity player, boolean alt);

    default int getPlayerCount() {
        return -1;
    }

    default int getSpectatorCount() {
        return -1;
    }

    default int getMaxPlayerCount() {
        return -1;
    }

    @Nullable
    default Text getState() {
        return null;
    }

    default boolean isHidden() {
        return false;
    }

    default GamePortalBackend.ActionType getActionType() {
        return GamePortalBackend.ActionType.NONE;
    }

    default GamePortalBackend.ActionType getAltActionType() {
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
        var maxPlayerCount = this.getMaxPlayerCount();
        var spectatorCount = this.getSpectatorCount();
        boolean allowSpace = true;

        var state = this.getState();
        if (state != null) {
            element.addLoreLine(ScreenTexts.EMPTY);
            element.addLoreLine(Text.literal(" ").append(state).formatted(Formatting.WHITE));
            allowSpace = false;
        }

        if (playerCount > -1) {
            if (allowSpace) {
                element.addLoreLine(ScreenTexts.EMPTY);
                allowSpace = false;
            }
            element.addLoreLine(Text.empty()
                    .append(Text.literal("» ").formatted(Formatting.DARK_GRAY))
                    .append(Text.translatable("text.plasmid.ui.game_join.players",
                            Text.literal(playerCount + (maxPlayerCount > 0 ? " / " + maxPlayerCount : "")).formatted(Formatting.YELLOW)).formatted(Formatting.GOLD))
            );
        }

        if (spectatorCount > 0) {
            if (allowSpace) {
                element.addLoreLine(ScreenTexts.EMPTY);
                allowSpace = false;
            }

            element.addLoreLine(Text.empty()
                    .append(Text.literal("» ").formatted(Formatting.DARK_GRAY))
                    .append(Text.translatable("text.plasmid.ui.game_join.spectators",
                            Text.literal( spectatorCount + "").formatted(Formatting.YELLOW)).formatted(Formatting.GOLD))
            );
        }

        var actionType = this.getActionType();

        if (actionType != GamePortalBackend.ActionType.NONE) {
            element.addLoreLine(Text.empty().append(Text.literal(" [ ").formatted(Formatting.GRAY))
                    .append(actionType.text())
                    .append(Text.literal(" ]").formatted(Formatting.GRAY)).setStyle(Style.EMPTY.withColor(0x76ed6f)));
        }

        var altActionType = this.getAltActionType();

        if (altActionType != GamePortalBackend.ActionType.NONE) {
            element.addLoreLine(Text.empty().append(Text.literal(" [ ").formatted(Formatting.GRAY))
                    .append(actionType.text())
                    .append(Text.literal(" ]").formatted(Formatting.GRAY)).setStyle(Style.EMPTY.withColor(0x76ed6f)));
        }

        element.setCallback((a, b, c, gui) -> {
            this.click(gui.getPlayer(), b.shift);
        });

        return element.build();
    }
}