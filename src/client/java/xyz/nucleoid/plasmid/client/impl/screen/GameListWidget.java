package xyz.nucleoid.plasmid.client.impl.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.api.game.config.GameConfigs;

import java.util.Comparator;
import java.util.Locale;

public class GameListWidget extends AlwaysSelectedEntryListWidget<GameListEntry> {
    private static final Comparator<RegistryEntry<GameConfig<?>>> ENTRY_ORDERING = Comparator.comparing(
            entry -> GameConfig.name(entry).getString(),
            String::compareToIgnoreCase
    );

    private final Registry<GameConfig<?>> registry;
    private final GamesScreen screen;

    private String search = "";

    public GameListWidget(DynamicRegistryManager registryManager, GamesScreen screen) {
        super(screen.client, screen.width, screen.height - 85, 48, 24);

        this.registry = registryManager.getOrThrow(GameConfigs.REGISTRY_KEY);
        this.screen = screen;

        this.updateEntries();
    }

    public void setSearch(String search) {
        if (!this.search.equalsIgnoreCase(search)) {
            this.search = search.toLowerCase(Locale.ROOT);
            this.updateEntries();
        }
    }

    private void updateEntries() {
        this.clearEntries();

        this.registry.streamEntries()
                .filter(entry -> {
                    String name = GameConfig.name(entry).getString().toLowerCase(Locale.ROOT);
                    return name.contains(this.search);
                })
                .sorted(ENTRY_ORDERING)
                .map(entry -> new GameListEntry(entry, this.screen, this.client.textRenderer))
                .forEachOrdered(this::addEntry);

        this.refreshScroll();
    }

    @Override
    public void setSelected(GameListEntry entry) {
        super.setSelected(entry);
        this.screen.setSelectedGame(entry == null ? null : entry.game);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);

        var entry = this.getHoveredEntry();

        if (entry != null && entry.description != null) {
            this.screen.setTooltip(entry.description);
        }
    }
}
