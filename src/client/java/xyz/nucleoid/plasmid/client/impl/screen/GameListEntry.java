package xyz.nucleoid.plasmid.client.impl.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;

public class GameListEntry extends AlwaysSelectedEntryListWidget.Entry<GameListEntry> {
    private static final Identifier SLOT_TEXTURE = Identifier.ofVanilla("container/slot");

    protected final RegistryEntry<GameConfig<?>> game;

    protected final Text name;
    protected final Text description;

    private final GamesScreen screen;
    private final TextRenderer textRenderer;

    private long lastClickTime;

    public GameListEntry(RegistryEntry<GameConfig<?>> game, GamesScreen screen, TextRenderer textRenderer) {
        this.game = game;
        this.name = GameConfig.name(this.game);

        var description = this.game.value().description();
        this.description = description.isEmpty() ? null : ScreenTexts.joinLines(description);

        this.screen = screen;
        this.textRenderer = textRenderer;
    }

    private void activate() {
        this.screen.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1));

        this.screen.setSelectedGame(this.game);
        this.screen.play();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Util.getMeasuringTimeMs() - this.lastClickTime >= Element.MAX_DOUBLE_CLICK_INTERVAL) {
            this.lastClickTime = Util.getMeasuringTimeMs();
            return super.mouseClicked(mouseX, mouseY, button);
        }

        this.activate();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (KeyCodes.isToggle(keyCode)) {
            this.activate();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        context.drawGuiTexture(RenderLayer::getGuiTextured, SLOT_TEXTURE, x + 1, y + 1, 18, 18);

        context.drawItemWithoutEntity(this.game.value().icon(), x + 2, y + 2);
        context.drawStackOverlay(this.textRenderer, this.game.value().icon(), x + 2, y + 2);

        context.drawTextWithShadow(this.textRenderer, this.name, x + 18 + 5, y + 6, Colors.WHITE);
    }

    @Override
    public Text getNarration() {
        var contents = this.description == null ? this.name : ScreenTexts.joinSentences(this.name, this.description);
        return Text.translatable("narrator.select", contents);
    }
}
