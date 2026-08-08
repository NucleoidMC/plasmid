package xyz.nucleoid.plasmid.api.menu;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.HolderSet;
import xyz.nucleoid.plasmid.api.menu.layout.FixedLayout;
import xyz.nucleoid.plasmid.api.menu.layout.FlowLayout;
import xyz.nucleoid.plasmid.api.menu.layout.RowsLayout;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistries;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * How the contents of a {@link GameMenu} are arranged, without committing to slot indices in a container of
 * a known size. A layout says how much room it wants through {@link #preferredSize}, then places itself
 * inside the {@link GameMenuFrame} worked out for it.
 *
 * <ul>
 *     <li>{@link FlowLayout}: a flat list. The theme decides rows, spacing and centring.</li>
 *     <li>{@link RowsLayout}: a list of rows. The game groups, the theme spaces.</li>
 *     <li>{@link FixedLayout}: explicit coordinates.</li>
 * </ul>
 *
 * <p>The set is open. Register a {@link GameMenuLayoutType} to add your own; since a layout places itself
 * through {@link #place}, nothing in Plasmid needs to know the full list.
 *
 * <p>The type parameter lets one definition serve both datapack configuration, which {@link #CODEC} decodes,
 * and a built menu ({@code GameMenuLayout<GameMenuElement>}), bridged by {@link #map}.
 */
public interface GameMenuLayout<T> {
    GameMenuLayoutType type();

    /**
     * Every element, in reading order.
     */
    List<T> elements();

    <R> GameMenuLayout<R> map(Function<T, R> mapper);

    /**
     * Maps each cell to several elements. Order-based layouts splice them in place; coordinate-based ones
     * keep the first and report the rest, a cell being a single position.
     */
    <R> GameMenuLayout<R> flatMap(Function<T, List<R>> mapper);

    /**
     * The content region this would like, given the largest one the theme leaves free and the spacing it
     * applies. What a menu sizing itself by {@link GameMenuSizeRule.Auto#FIT} gets.
     *
     * <p>Layouts that can reflow or page ask for no more than {@code max}, shrinking the container for a
     * short list and paging a long one. Layouts with a shape of their own report it in full, and
     * {@link #validate} explains whatever then does not fit.
     */
    GameMenuSize preferredSize(GameMenuSize max, int paddingX, int paddingY);

    /**
     * Pages needed to show everything in the given frame.
     */
    int pageCount(GameMenuFrame frame);

    /**
     * Hands every element of {@code page} to {@code placer} with its content-region cell. Implementations
     * must not emit cells outside the frame; anything that does not fit is dropped and reported by
     * {@link #validate}.
     */
    void place(GameMenuFrame frame, int page, Placer<T> placer);

    /**
     * Reasons this would not render faithfully in the given frame, each phrased to follow "it ...". Checked
     * at datapack load against every known theme, and again on first draw to catch code-built menus.
     */
    default List<String> validate(GameMenuFrame frame) {
        return List.of();
    }

    @FunctionalInterface
    interface Placer<T> {
        void place(int x, int y, T element);
    }

    static <T> FlowLayout<T> flow(List<T> elements) {
        return new FlowLayout<>(List.copyOf(elements));
    }

    @SafeVarargs
    static <T> RowsLayout<T> rows(List<T>... rows) {
        return new RowsLayout<>(List.of(rows));
    }

    static <T> FixedLayout<T> fixed(Map<Pos, T> cells) {
        return new FixedLayout<>(Map.copyOf(cells));
    }

    /**
     * Drops elements the predicate rejects. Order-based layouts close the gap; coordinate-based ones leave
     * the cell empty.
     */
    default GameMenuLayout<T> filter(Predicate<T> keep) {
        return this;
    }

    /**
     * A cell coordinate, serialized as {@code "x:y"}.
     */
    record Pos(int x, int y) {
        public static final Codec<Pos> CODEC = Codec.STRING.comapFlatMap(Pos::parse, Pos::toString);

        private static DataResult<Pos> parse(String value) {
            var split = value.split(":");
            if (split.length != 2) {
                return DataResult.error(() -> "Expected a position of the form \"x:y\", got \"" + value + "\"");
            }

            try {
                return DataResult.success(new Pos(Integer.parseInt(split[0].trim()), Integer.parseInt(split[1].trim())));
            } catch (NumberFormatException e) {
                return DataResult.error(() -> "Expected a position of the form \"x:y\", got \"" + value + "\"");
            }
        }

        @Override
        public String toString() {
            return this.x + ":" + this.y;
        }
    }

    /**
     * The datapack form, dispatching on {@link GameMenuLayoutType}. A bare list is shorthand for
     * {@link FlowLayout} and a bare {@code "x:y"} map for {@link FixedLayout}.
     */
    Codec<GameMenuLayout<HolderSet<GameMenuEntryConfig>>> CODEC = codec();

    private static Codec<GameMenuLayout<HolderSet<GameMenuEntryConfig>>> codec() {
        Codec<GameMenuLayout<HolderSet<GameMenuEntryConfig>>> dispatched =
                PlasmidRegistries.GAME_MENU_LAYOUT_TYPE.byNameCodec()
                        .dispatchStable(GameMenuLayout::type, GameMenuLayoutType::codec);

        var object = Codec.either(dispatched, Codec.unboundedMap(Pos.CODEC, GameMenuEntryConfig.ENTRY_LIST_CODEC)).xmap(
                either -> either.map(Function.identity(), GameMenuLayout::fixed),
                layout -> layout instanceof FixedLayout<HolderSet<GameMenuEntryConfig>> fixed
                        ? Either.right(fixed.cells())
                        : Either.left(layout)
        );

        return Codec.either(GameMenuEntryConfig.ENTRY_LIST_CODEC.listOf(), object).xmap(
                either -> either.map(GameMenuLayout::flow, Function.identity()),
                layout -> layout instanceof FlowLayout<HolderSet<GameMenuEntryConfig>> flow
                        ? Either.left(flow.elements())
                        : Either.right(layout)
        );
    }
}
