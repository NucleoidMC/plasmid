package xyz.nucleoid.plasmid.impl.menu.entry;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.menu.GameMenu;
import xyz.nucleoid.plasmid.api.menu.GameMenuConfig;
import xyz.nucleoid.plasmid.api.menu.GameMenuEntry;

import java.util.List;
import java.util.function.Consumer;

/**
 * An entry that opens another menu, and reports the games reachable through it.
 * <p>
 * The counts cascade: a category shows the total of every game under it, however deep, because
 * {@link #provideGameSpaces} walks into the menu's own entries.
 */
public final class MenuEntry implements GameMenuEntry {
    private final Holder<GameMenuConfig> config;
    private final Component name;
    private final List<Component> description;
    private final ItemStack icon;

    private GameMenu menu;
    private boolean walking;

    public MenuEntry(Holder<GameMenuConfig> config, Component name, List<Component> description, ItemStack icon) {
        this.config = config;
        this.name = name;
        this.description = description;
        this.icon = icon;
    }

    /**
     * Built once: rebuilding would give the menu a second set of entries with their own live state.
     */
    public GameMenu menu() {
        if (this.menu == null) {
            this.menu = this.config.value().build();
        }

        return this.menu;
    }

    @Override
    public Component name() {
        return this.name;
    }

    @Override
    public List<Component> description() {
        return this.description;
    }

    @Override
    public ItemStack icon() {
        return this.icon;
    }

    @Override
    public void click(ServerPlayer player, boolean alt) {
        this.menu().open(player);
    }

    @Override
    public boolean isGameEntry() {
        return this.menu().hasGameEntries();
    }

    @Override
    public void provideGameSpaces(Consumer<GameSpace> consumer) {
        // A menu reachable from itself would otherwise recurse forever.
        if (this.walking) {
            return;
        }

        this.walking = true;
        try {
            this.menu().provideGameSpaces(consumer);
        } finally {
            this.walking = false;
        }
    }

    @Override
    public int getPlayerCount() {
        int count = 0;
        for (var gameSpace : this.reachableGames()) {
            count += gameSpace.getState().players();
        }
        return count;
    }

    @Override
    public int getSpectatorCount() {
        int count = 0;
        for (var gameSpace : this.reachableGames()) {
            count += gameSpace.getState().spectators();
        }
        return count;
    }

    private java.util.Set<GameSpace> reachableGames() {
        var games = new it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet<GameSpace>();
        this.provideGameSpaces(games::add);
        return games;
    }
}
