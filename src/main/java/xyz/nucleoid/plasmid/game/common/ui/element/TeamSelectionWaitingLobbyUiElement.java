package xyz.nucleoid.plasmid.game.common.ui.element;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.game.common.team.GameTeamList;
import xyz.nucleoid.plasmid.util.ColoredBlocks;

import java.util.ArrayList;
import java.util.SequencedCollection;
import java.util.function.Consumer;

public class TeamSelectionWaitingLobbyUiElement implements WaitingLobbyUiElement {
    private final GameTeamList teams;
    private final Consumer<GameTeamKey> selectCallback;

    public TeamSelectionWaitingLobbyUiElement(GameTeamList teams, Consumer<GameTeamKey> selectCallback) {
        this.teams = teams;
        this.selectCallback = selectCallback;
    }

    @Override
    public GuiElementInterface createMainElement() {
        return new GuiElementBuilder(Items.PAPER)
                .setItemName(Text.translatable("text.plasmid.team_selection.teams"))
                .build();
    }

    @Override
    public SequencedCollection<GuiElementInterface> createExtendedElements() {
        var extendedElements = new ArrayList<GuiElementInterface>(this.teams.list().size());

        for (var team : this.teams) {
            var key = team.key();
            var config = team.config();

            var name = Text.translatable("text.plasmid.team_selection.request_team", config.name())
                    .formatted(Formatting.BOLD, config.chatFormatting());

            var element = new GuiElementBuilder(ColoredBlocks.wool(config.blockDyeColor()).asItem())
                    .setItemName(name)
                    .setCallback((index, type, action) -> {
                        if (type.isRight) {
                            this.selectCallback.accept(key);
                        }
                    })
                    .build();

            extendedElements.add(element);
        }

        return extendedElements;
    }
}
