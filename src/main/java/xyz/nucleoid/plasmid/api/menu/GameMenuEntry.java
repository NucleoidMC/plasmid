package xyz.nucleoid.plasmid.api.menu;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.GameSpace;

import java.util.List;
import java.util.function.Consumer;

/**
 * A selectable item within a {@link GameMenu}. Entries describe what they are, not where they sit:
 * placement comes from the {@link GameMenuLayout}, surrounding chrome from the {@link GameMenuTheme}.
 * <p>
 * The same interface backs what a sign or hologram shows in the world: a label, an icon, a live count and
 * something that happens on click.
 */
public interface GameMenuEntry {
    /**
     * The hint shown for what clicking does.
     */
    record Action(Component text, Component textAlt) {
        public static final Action NONE = new Action(Component.empty(), Component.empty());
        public static final Action PLAY = new Action(
                Component.translatable("text.plasmid.ui.game_join.action.play"),
                Component.translatable("text.plasmid.ui.game_join.action.play.alt"));
        public static final Action SPECTATE = new Action(
                Component.translatable("text.plasmid.ui.game_join.action.spectate"),
                Component.translatable("text.plasmid.ui.game_join.action.spectate.alt"));
    }

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

    default Action getAction() {
        return Action.NONE;
    }

    default Action getAltAction() {
        return Action.NONE;
    }

    default void provideGameSpaces(Consumer<GameSpace> consumer) {

    }

    /**
     * Whether this leads to a game, which is what earns a menu {@link GameMenuFeatures#GAME_FILTER}.
     */
    default boolean isGameEntry() {
        return false;
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

        var action = this.getAction();

        if (action != Action.NONE) {
            element.addLoreLine(Component.empty().append(Component.literal(" [ ").withStyle(ChatFormatting.GRAY))
                    .append(action.text())
                    .append(Component.literal(" ]").withStyle(ChatFormatting.GRAY)).setStyle(Style.EMPTY.withColor(0x76ed6f)));
        }

        var altAction = this.getAltAction();

        if (altAction != Action.NONE) {
            element.addLoreLine(Component.empty().append(Component.literal(" [ ").withStyle(ChatFormatting.GRAY))
                    .append(altAction.textAlt())
                    .append(Component.literal(" ]").withStyle(ChatFormatting.GRAY)).setStyle(Style.EMPTY.withColor(0x76ed6f)));
        }

        element.setCallback((a, b, c, gui) -> {
            this.click(gui.getPlayer(), b.shift);
        });

        return element.build();
    }
}
