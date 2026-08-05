package xyz.nucleoid.plasmid.impl.menu;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.SimpleGuiElement;
import eu.pb4.sgui.api.gui.layered.Layer;
import eu.pb4.sgui.api.gui.layered.LayeredGui;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.menu.*;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistryKeys;
import xyz.nucleoid.plasmid.api.util.PlayerUtil;
import xyz.nucleoid.plasmid.impl.Plasmid;
import xyz.nucleoid.plasmid.impl.menu.entry.GameSpaceMenuEntry;

import java.util.*;

/**
 * One open menu. Chrome goes on a layer spanning the container, content on a layer of the frame's size
 * positioned by the theme, so neither knows the other's coordinates.
 */
public final class GameMenuContextImpl implements GameMenuContext {
    /**
     * The menu each player last opened, validated against {@code isOpen} before being trusted.
     */
    private static final Map<UUID, GameMenuContextImpl> OPEN = new Object2ObjectOpenHashMap<>();

    private final ServerPlayer player;
    private final GameMenu menu;
    private final GameMenuTheme theme;
    private final Set<GameMenuFeature> pinned;
    private final GameMenuFrame frame;

    /**
     * The opening menu, and the back button target.
     */
    @Nullable
    private final GameMenuContextImpl parent;

    private final MenuGui gui;
    private final Layer chrome;
    private final Layer content;

    private Set<GameMenuFeature> features = Set.of();
    private View view = View.CONTENT;
    private int page;
    private int pageCount = 1;
    private List<GameMenuEntry> openGames = List.of();
    private boolean reportedOverflow;

    GameMenuContextImpl(ServerPlayer player, GameMenu menu, GameMenuTheme theme, @Nullable GameMenuContextImpl parent) {
        this.player = player;
        this.menu = menu;
        this.theme = theme;
        this.parent = parent;
        this.pinned = collectPinned(menu.layout());

        // The container cannot be resized once open, so its height is settled here, from the menu's own
        // contents. The open-games view then flows into whatever this menu earned.
        var inherited = parent == null ? null : new GameMenuSize(parent.frame().width(), parent.frame().height());
        this.frame = GameMenuFrame.resolve(theme, theme.insets(player), menu.sizeRule(), menu.layout(), inherited, this::reportOverflow);

        this.gui = new MenuGui(this.frame.type(), player, this.frame.includePlayerSlots());
        this.chrome = new Layer(this.gui.getHeight(), this.gui.getWidth());
        this.content = new Layer(this.frame.height(), this.frame.width());

        this.gui.addLayer(this.chrome, 0, 0).setZIndex(0);
        this.gui.addLayer(this.content, this.frame.x(), this.frame.y()).setZIndex(1);
    }

    private static Set<GameMenuFeature> collectPinned(GameMenuLayout<GameMenuElement> layout) {
        var pinned = new LinkedHashSet<GameMenuFeature>();

        for (var element : layout.elements()) {
            if (element instanceof GameMenuElement.Feature(GameMenuFeature feature)) {
                pinned.add(feature);
            }
        }

        return pinned;
    }

    void open() {
        this.draw();

        if (this.gui.open()) {
            OPEN.put(this.player.getUUID(), this);
        }
    }

    /**
     * The menu this player currently has open, if any. {@link eu.pb4.sgui.api.SguiUtils#getCurrentGui}
     * cannot answer this, since for a {@link LayeredGui} it returns the internal backing gui. The last menu
     * opened is remembered instead, and confirmed against sgui's {@code isOpen}.
     */
    @Nullable
    static GameMenuContextImpl openFor(ServerPlayer player) {
        var context = OPEN.get(player.getUUID());
        if (context == null) {
            return null;
        }

        if (!context.gui.isOpen()) {
            OPEN.remove(player.getUUID());
            return null;
        }

        return context;
    }

    public static void forget(ServerPlayer player) {
        OPEN.remove(player.getUUID());
    }

    public static void forgetAll() {
        OPEN.clear();
    }

    @Override
    public ServerPlayer player() {
        return this.player;
    }

    @Override
    public GameMenu menu() {
        return this.menu;
    }

    @Override
    public GameMenuTheme theme() {
        return this.theme;
    }

    /**
     * The menu this was opened from, if any.
     */
    @Nullable
    GameMenuContextImpl parent() {
        return this.parent;
    }

    @Override
    public GameMenuFrame frame() {
        return this.frame;
    }

    @Override
    public Set<GameMenuFeature> features() {
        return this.features;
    }

    @Override
    public boolean isPinned(GameMenuFeature feature) {
        return this.pinned.contains(feature);
    }

    @Override
    public boolean isActive(GameMenuFeature feature) {
        return feature == GameMenuFeatures.GAME_FILTER && this.view == View.OPEN_GAMES;
    }

    @Override
    public View view() {
        return this.view;
    }

    @Override
    public void setView(View view) {
        if (this.view != view) {
            this.view = view;
            this.page = 0;
            this.refresh();
        }
    }

    @Override
    public int page() {
        return this.page;
    }

    @Override
    public int pageCount() {
        return this.pageCount;
    }

    @Override
    public void setPage(int page) {
        int clamped = Mth.clamp(page, 0, this.pageCount - 1);
        if (clamped != this.page) {
            this.page = clamped;
            this.refresh();
        }
    }

    @Override
    public void refresh() {
        this.draw();
    }

    @Override
    public boolean canGoBack() {
        return this.parent != null;
    }

    @Override
    public void goBack() {
        if (this.parent != null) {
            // Rebuild rather than reopening the stale gui, so counts are current.
            GameMenuRenderer.reopen(this.parent);
        }
    }

    private void draw() {
        this.refreshOpenGames();
        this.pageCount = this.computePageCount();
        this.page = Mth.clamp(this.page, 0, this.pageCount - 1);
        this.features = this.computeFeatures();

        this.chrome.clearSlots();
        this.theme.decorate(this, this.chrome);
        this.drawFeatureChrome();

        this.content.clearSlots();
        this.drawContent();

        // Before the title: setting one makes sgui resend the whole container, so doing it first would ship
        // the previous contents.
        this.gui.pushLayers();

        var title = this.theme.title(this, this.currentTitle());
        if (!title.equals(this.gui.getTitle())) {
            // Only when changed: each call reopens the screen client-side.
            this.gui.setTitle(title);
        }
    }

    private Component currentTitle() {
        if (this.view == View.OPEN_GAMES) {
            return Component.empty()
                    .append(this.menu.title())
                    .append(" ")
                    .append(Component.translatable("text.plasmid.ui.game_menu.open_only"));
        }

        return this.menu.title();
    }

    private void refreshOpenGames() {
        if (this.view != View.OPEN_GAMES) {
            return;
        }

        var spaces = new ReferenceOpenHashSet<GameSpace>();
        this.menu.provideGameSpaces(spaces::add);

        var sorted = new ArrayList<>(spaces);
        sorted.sort(Comparator.comparingInt(space -> -space.getState().players()));

        this.openGames = sorted.stream().<GameMenuEntry>map(GameSpaceMenuEntry::new).toList();
    }

    private int computePageCount() {
        return this.currentLayout().pageCount(this.frame);
    }

    /**
     * The open-games view is a flow layout, so it pages and centres like anything else.
     */
    private GameMenuLayout<GameMenuElement> currentLayout() {
        if (this.view == View.OPEN_GAMES) {
            return GameMenuLayout.flow(this.openGames.stream().map(GameMenuElement::of).toList());
        }

        // Dropped rather than left as gaps: a menu referencing an absent mod's entry closes ranks.
        return this.menu.layout().filter(GameMenuContextImpl::isVisible);
    }

    private static boolean isVisible(GameMenuElement element) {
        return !(element instanceof GameMenuElement.Entry(GameMenuEntry entry)) || !entry.isHidden();
    }

    private Set<GameMenuFeature> computeFeatures() {
        var features = new LinkedHashSet<>(this.menu.features());
        features.addAll(this.pinned);

        // Any menu can be closed, so whether a button for it appears is left entirely to the theme.
        features.add(GameMenuFeatures.CLOSE);

        if (this.canGoBack()) {
            features.add(GameMenuFeatures.BACK);
        }

        if (this.pageCount > 1) {
            features.add(GameMenuFeatures.PREVIOUS_PAGE);
            features.add(GameMenuFeatures.NEXT_PAGE);
        }

        return features;
    }

    private void drawFeatureChrome() {
        var layout = this.theme.featureLayout(this);

        for (var feature : this.features) {
            // Pinned features are placed by the layout itself.
            if (this.pinned.contains(feature)) {
                continue;
            }

            // A feature the theme places nowhere, or outside the container, is simply not offered.
            int slot = layout.slot(feature, this.chrome.getWidth(), this.chrome.getHeight());
            if (slot < 0 || slot >= this.chrome.getSize()) {
                continue;
            }

            var element = this.buildChrome(feature);
            if (element != null) {
                this.chrome.setSlot(slot, element);
            }
        }
    }

    @Nullable
    private GuiElement buildChrome(GameMenuFeature feature) {
        var builder = this.theme.chrome(this, feature);
        if (builder == null) {
            return null;
        }

        return builder.setCallback((index, type, action, gui) -> this.onFeature(feature)).build();
    }

    private void playClick() {
        PlayerUtil.playSoundToPlayer(this.player, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.MASTER, 1, 1);
    }

    /**
     * Wraps an element so clicking it is audible. Done here rather than in the elements themselves so it
     * holds for every entry and every theme, including themes that rebuild elements from scratch.
     */
    private GuiElement audible(GuiElement element) {
        var callback = element.getGuiCallback();

        return new SimpleGuiElement(element.getItemStack(), (index, type, action, gui) -> {
            this.playClick();
            callback.click(index, type, action, gui);
        });
    }

    private void onFeature(GameMenuFeature feature) {
        this.playClick();

        if (feature == GameMenuFeatures.GAME_FILTER) {
            this.setView(this.view == View.OPEN_GAMES ? View.CONTENT : View.OPEN_GAMES);
        } else if (feature == GameMenuFeatures.BACK) {
            this.goBack();
        } else if (feature == GameMenuFeatures.CLOSE) {
            this.gui.close();
        } else if (feature == GameMenuFeatures.PREVIOUS_PAGE) {
            this.setPage(this.page - 1);
        } else if (feature == GameMenuFeatures.NEXT_PAGE) {
            this.setPage(this.page + 1);
        }
    }

    private void drawContent() {
        var layout = this.currentLayout();

        if (layout.elements().isEmpty()) {
            this.drawEmpty();
            return;
        }

        layout.place(this.frame, this.page, (x, y, element) -> {
            var built = this.build(element);
            if (built != null) {
                this.content.setSlot(x + y * this.frame.width(), built);
            }
        });

        for (var problem : layout.validate(this.frame)) {
            this.reportOverflow(problem);
        }
    }

    /**
     * Better than a blank grid, which reads as a control having done nothing.
     */
    private void drawEmpty() {
        var builder = this.theme.emptyElement(this);
        if (builder == null) {
            return;
        }

        int x = this.frame.columnOf(0, 1);
        int y = this.frame.rowOf(0, 1);

        if (this.frame.contains(x, y)) {
            this.content.setSlot(x + y * this.frame.width(), builder.build());
        }
    }

    /**
     * Warns that a layout does not fit the theme drawing it. The load-time check cannot cover code-built
     * menus or a theme switched to later. Reported once per opening.
     */
    private String themeName() {
        return this.player.registryAccess().lookupOrThrow(PlasmidRegistryKeys.GAME_MENU_THEME)
                .listElements()
                .filter(reference -> reference.value() == this.theme)
                .findFirst()
                .map(reference -> reference.key().identifier().toString())
                .orElseGet(() -> "<unregistered " + this.theme.getClass().getSimpleName() + ">");
    }

    private void reportOverflow(String detail) {
        if (this.reportedOverflow) {
            return;
        }

        this.reportedOverflow = true;
        Plasmid.LOGGER.warn("Game menu \"{}\" does not fit theme {}: {}",
                this.menu.title().getString(), this.themeName(), detail);
    }

    @Nullable
    private GuiElement build(GameMenuElement element) {
        return switch (element) {
            case GameMenuElement.Entry(GameMenuEntry entry) ->
                    entry.isHidden() ? null : this.audible(this.theme.entryElement(this, entry));
            case GameMenuElement.Feature(GameMenuFeature feature) -> this.buildChrome(feature);
            case GameMenuElement.Raw(GuiElement raw) -> this.audible(raw);
            case GameMenuElement.Empty ignored -> null;
        };
    }

    private static final class MenuGui extends LayeredGui {
        MenuGui(MenuType<?> type, ServerPlayer player, boolean manipulatePlayerSlots) {
            super(type, player, manipulatePlayerSlots);
        }

        /**
         * Flattens the layers into the backing gui now, instead of waiting for the next tick.
         */
        void pushLayers() {
            this.draw();
        }
    }
}
