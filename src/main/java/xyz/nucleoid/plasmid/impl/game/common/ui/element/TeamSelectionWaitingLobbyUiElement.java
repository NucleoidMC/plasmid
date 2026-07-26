package xyz.nucleoid.plasmid.impl.game.common.ui.element;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElement;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamList;
import xyz.nucleoid.plasmid.api.game.common.ui.WaitingLobbyUiElement;
import xyz.nucleoid.plasmid.api.util.ColoredBlocks;

import java.util.ArrayList;
import java.util.SequencedCollection;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public class TeamSelectionWaitingLobbyUiElement implements WaitingLobbyUiElement {
    private final GameTeamList teams;

    private final Predicate<GameTeamKey> activePredicate;
    private final Consumer<GameTeamKey> selectCallback;

    public TeamSelectionWaitingLobbyUiElement(GameTeamList teams, Predicate<GameTeamKey> activePredicate, Consumer<GameTeamKey> selectCallback) {
        this.teams = teams;

        this.activePredicate = activePredicate;
        this.selectCallback = selectCallback;
    }

    @Override
    public GuiElement createMainElement() {
        return new GuiElementBuilder(Items.PAPER)
                .setItemName(Component.translatable("text.plasmid.team_selection.teams"))
                .build();
    }

    @Override
    public SequencedCollection<GuiElement> createExtendedElements() {
        var extendedElements = new ArrayList<GuiElement>(this.teams.list().size());

        for (var team : this.teams) {
            var key = team.key();
            var config = team.config();

            var name = Component.translatable("text.plasmid.team_selection.request_team", config.name())
                    .withStyle(ChatFormatting.BOLD, config.chatFormatting());

            var element = new GuiElementBuilder(ColoredBlocks.wool(config.blockDyeColor()).asItem())
                    .setItemName(name)
                    .setCallback((index, type, action, gui) -> {
                        if (WaitingLobbyUiElement.isClick(type, gui)) {
                            this.selectCallback.accept(key);
                        }
                    })
                    .glow(this.activePredicate.test(key))
                    .build();

            extendedElements.add(element);
        }

        return extendedElements;
    }
}
