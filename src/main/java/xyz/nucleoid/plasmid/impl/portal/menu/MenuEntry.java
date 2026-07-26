package xyz.nucleoid.plasmid.impl.portal.menu;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.impl.portal.GamePortalBackend;
import xyz.nucleoid.plasmid.api.game.GameSpace;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public interface MenuEntry {
    Component name();

    List<Component> description();

    ItemStack icon();

    void click(ServerPlayer player, boolean alt);

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
    default Component getState() {
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
                .setName(Component.empty().append(this.name()));

        for (var line : this.description()) {
            var text = line.copy();

            if (line.getStyle().getColor() == null) {
                text.setStyle(line.getStyle().applyFormat(ChatFormatting.GRAY));
            }

            element.addLoreLine(text);
        }

        var playerCount = this.getPlayerCount();
        var maxPlayerCount = this.getMaxPlayerCount();
        var spectatorCount = this.getSpectatorCount();
        boolean allowSpace = true;

        var state = this.getState();
        if (state != null) {
            element.addLoreLine(CommonComponents.EMPTY);
            element.addLoreLine(Component.literal(" ").append(state).withStyle(ChatFormatting.WHITE));
            allowSpace = false;
        }

        if (playerCount > -1) {
            if (allowSpace) {
                element.addLoreLine(CommonComponents.EMPTY);
                allowSpace = false;
            }
            element.addLoreLine(Component.empty()
                    .append(Component.literal("» ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.translatable("text.plasmid.ui.game_join.players",
                            Component.literal(playerCount + (maxPlayerCount > 0 ? " / " + maxPlayerCount : "")).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GOLD))
            );
        }

        if (spectatorCount > 0) {
            if (allowSpace) {
                element.addLoreLine(CommonComponents.EMPTY);
                allowSpace = false;
            }

            element.addLoreLine(Component.empty()
                    .append(Component.literal("» ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.translatable("text.plasmid.ui.game_join.spectators",
                            Component.literal( spectatorCount + "").withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GOLD))
            );
        }

        var actionType = this.getActionType();

        if (actionType != GamePortalBackend.ActionType.NONE) {
            element.addLoreLine(Component.empty().append(Component.literal(" [ ").withStyle(ChatFormatting.GRAY))
                    .append(actionType.text())
                    .append(Component.literal(" ]").withStyle(ChatFormatting.GRAY)).setStyle(Style.EMPTY.withColor(0x76ed6f)));
        }

        var altActionType = this.getAltActionType();

        if (altActionType != GamePortalBackend.ActionType.NONE) {
            element.addLoreLine(Component.empty().append(Component.literal(" [ ").withStyle(ChatFormatting.GRAY))
                    .append(actionType.text())
                    .append(Component.literal(" ]").withStyle(ChatFormatting.GRAY)).setStyle(Style.EMPTY.withColor(0x76ed6f)));
        }

        element.setCallback((a, b, c, gui) -> {
            this.click(gui.getPlayer(), b.shift);
        });

        return element.build();
    }
}