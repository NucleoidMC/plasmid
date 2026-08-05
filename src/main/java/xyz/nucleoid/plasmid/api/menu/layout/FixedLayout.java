package xyz.nucleoid.plasmid.api.menu.layout;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import xyz.nucleoid.plasmid.api.menu.*;
import xyz.nucleoid.plasmid.impl.Plasmid;

import java.util.*;
import java.util.function.Function;

/**
 * Elements at explicit coordinates. Padding and centring do not apply; the block is offset only by where the
 * theme puts its content region.
 * <p>
 * The only shape that can be incompatible with a theme: cells beyond the content region are dropped and
 * reported.
 */
public record FixedLayout<T>(Map<Pos, T> cells) implements GameMenuLayout<T> {
    public static final MapCodec<FixedLayout<HolderSet<GameMenuEntryConfig>>> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.unboundedMap(Pos.CODEC, GameMenuEntryConfig.ENTRY_LIST_CODEC).fieldOf("elements").forGetter(FixedLayout::cells)
    ).apply(i, FixedLayout::new));

    @Override
    public GameMenuLayoutType type() {
        return GameMenuLayoutType.FIXED;
    }

    /**
     * Derived from the cells.
     */
    public int width() {
        return this.cells.keySet().stream().mapToInt(Pos::x).max().orElse(-1) + 1;
    }

    public int height() {
        return this.cells.keySet().stream().mapToInt(Pos::y).max().orElse(-1) + 1;
    }

    @Override
    public List<T> elements() {
        return List.copyOf(this.cells.values());
    }

    @Override
    public <R> FixedLayout<R> map(Function<T, R> mapper) {
        var mapped = new LinkedHashMap<Pos, R>(this.cells.size());
        this.cells.forEach((pos, value) -> mapped.put(pos, mapper.apply(value)));
        return new FixedLayout<>(mapped);
    }

    /**
     * A cell is one position, so an expanded source keeps its first entry and the rest are reported.
     */
    @Override
    public <R> FixedLayout<R> flatMap(Function<T, List<R>> mapper) {
        var mapped = new LinkedHashMap<Pos, R>(this.cells.size());

        this.cells.forEach((pos, value) -> {
            var expanded = mapper.apply(value);

            if (!expanded.isEmpty()) {
                mapped.put(pos, expanded.getFirst());
            }

            if (expanded.size() > 1) {
                Plasmid.LOGGER.warn("Fixed menu cell {} expanded to {} entries; only the first is placed", pos, expanded.size());
            }
        });

        return new FixedLayout<>(mapped);
    }

    /**
     * As tall as the block its cells span, and as wide as the theme will allow.
     *
     * <p>The height is the real extent, since that is what decides how tall a container gets built. The
     * width is not. A container is nine columns wide whatever the region says, so asking for a narrower one
     * would only center it and shift every coordinate sideways. These coordinates are absolute, so
     * {@code 0:0} has to land in the corner of what the theme left free, and a leading empty column has to
     * read as the author's margin instead of being counted twice.
     *
     * <p>Cells past the edge are still dropped and named by {@link #validate}.
     */
    @Override
    public GameMenuSize preferredSize(GameMenuSize max, int paddingX, int paddingY) {
        return new GameMenuSize(max.width(), Math.max(1, this.height()));
    }

    @Override
    public int pageCount(GameMenuFrame frame) {
        return 1;
    }

    @Override
    public void place(GameMenuFrame frame, int page, Placer<T> placer) {
        this.cells.forEach((pos, value) -> {
            if (frame.contains(pos.x(), pos.y())) {
                placer.place(pos.x(), pos.y(), value);
            }
        });
    }

    @Override
    public List<String> validate(GameMenuFrame frame) {
        var outside = new ArrayList<>(this.cells.keySet().stream()
                .filter(pos -> !frame.contains(pos.x(), pos.y()))
                .toList());

        if (outside.isEmpty()) {
            return List.of();
        }

        outside.sort(Comparator.comparingInt(Pos::y).thenComparingInt(Pos::x));
        return List.of("needs a " + this.width() + "x" + this.height() + " region but the theme offers "
                + frame.width() + "x" + frame.height() + ", dropping " + outside);
    }
}
