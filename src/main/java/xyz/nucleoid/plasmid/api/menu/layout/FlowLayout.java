package xyz.nucleoid.plasmid.api.menu.layout;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.Mth;
import xyz.nucleoid.plasmid.api.menu.*;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A flat list, arranged entirely by the theme: rows fill left to right, each centred horizontally and the
 * block centred vertically, spaced by the frame padding.
 * <p>
 * Compatible with every theme: anything that does not fit a page is paginated.
 */
public record FlowLayout<T>(List<T> elements) implements GameMenuLayout<T> {
    public static final MapCodec<FlowLayout<HolderSet<GameMenuEntryConfig>>> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            GameMenuEntryConfig.ENTRY_LIST_CODEC.listOf().fieldOf("elements").forGetter(FlowLayout::elements)
    ).apply(i, FlowLayout::new));

    @Override
    public GameMenuLayoutType type() {
        return GameMenuLayoutType.FLOW;
    }

    @Override
    public <R> FlowLayout<R> map(Function<T, R> mapper) {
        return new FlowLayout<>(this.elements.stream().map(mapper).toList());
    }

    @Override
    public <R> FlowLayout<R> flatMap(Function<T, List<R>> mapper) {
        return new FlowLayout<>(this.elements.stream().flatMap(element -> mapper.apply(element).stream()).toList());
    }

    @Override
    public FlowLayout<T> filter(Predicate<T> keep) {
        return new FlowLayout<>(this.elements.stream().filter(keep).toList());
    }

    /**
     * As wide as the busiest row would be and as tall as the list needs, capped at what is free. Three
     * elements ask for one row; forty ask for the maximum and page the rest.
     */
    @Override
    public GameMenuSize preferredSize(GameMenuSize max, int paddingX, int paddingY) {
        int perRow = Math.max(1, (max.width() + paddingX) / (1 + paddingX));
        int count = Math.max(1, this.elements.size());

        int inWidestRow = Math.min(count, perRow);
        int rows = Mth.positiveCeilDiv(count, perRow);

        return new GameMenuSize(
                inWidestRow + (inWidestRow - 1) * paddingX,
                rows + (rows - 1) * paddingY
        ).clampTo(max);
    }

    @Override
    public int pageCount(GameMenuFrame frame) {
        if (!frame.paginated()) {
            return 1;
        }

        return Math.max(1, Mth.positiveCeilDiv(this.elements.size(), frame.capacity()));
    }

    @Override
    public void place(GameMenuFrame frame, int page, Placer<T> placer) {
        int perRow = frame.itemsPerRow();
        int capacity = frame.capacity();

        int from = Math.min(page * capacity, this.elements.size());
        int to = Math.min(from + capacity, this.elements.size());
        int count = to - from;

        int rows = Math.max(1, Mth.positiveCeilDiv(count, perRow));

        for (int i = 0; i < count; i++) {
            int row = i / perRow;
            int column = i % perRow;

            // The last row is centred on its own count.
            int inThisRow = Math.min(perRow, count - row * perRow);

            int x = frame.columnOf(column, inThisRow);
            int y = frame.rowOf(row, rows);

            if (frame.contains(x, y)) {
                placer.place(x, y, this.elements.get(from + i));
            }
        }
    }

    @Override
    public List<String> validate(GameMenuFrame frame) {
        if (!frame.paginated() && this.elements.size() > frame.capacity()) {
            return List.of("holds " + this.elements.size() + " elements but the theme fits "
                    + frame.capacity() + " and does not paginate");
        }

        return List.of();
    }
}
