package xyz.nucleoid.plasmid.api.menu.layout;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.Mth;
import xyz.nucleoid.plasmid.api.menu.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A list of rows: the game decides what shares a line, the theme spaces and centres them.
 * <p>
 * Compatible with any theme wide enough for the busiest row once padding is counted; extra rows are
 * paginated.
 */
public record RowsLayout<T>(List<List<T>> rows) implements GameMenuLayout<T> {
    public static final MapCodec<RowsLayout<HolderSet<GameMenuEntryConfig>>> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            GameMenuEntryConfig.ENTRY_LIST_CODEC.listOf().listOf().fieldOf("rows").forGetter(RowsLayout::rows)
    ).apply(i, RowsLayout::new));

    @Override
    public GameMenuLayoutType type() {
        return GameMenuLayoutType.ROWS;
    }

    /**
     * Elements on the busiest row.
     */
    public int widestRow() {
        int width = 0;
        for (var row : this.rows) {
            width = Math.max(width, row.size());
        }
        return width;
    }

    @Override
    public List<T> elements() {
        var elements = new ArrayList<T>();
        for (var row : this.rows) {
            elements.addAll(row);
        }
        return elements;
    }

    @Override
    public <R> RowsLayout<R> map(Function<T, R> mapper) {
        return new RowsLayout<>(this.rows.stream().map(row -> row.stream().map(mapper).toList()).toList());
    }

    @Override
    public <R> RowsLayout<R> flatMap(Function<T, List<R>> mapper) {
        return new RowsLayout<>(this.rows.stream()
                .map(row -> row.stream().flatMap(element -> mapper.apply(element).stream()).toList())
                .toList());
    }

    @Override
    public RowsLayout<T> filter(Predicate<T> keep) {
        return new RowsLayout<>(this.rows.stream()
                .map(row -> row.stream().filter(keep).toList())
                .filter(row -> !row.isEmpty())
                .toList());
    }

    /**
     * As wide as the busiest row once padded, and one region row per declared row. The width is asked for in
     * full even when it does not fit. A row cannot be broken up, so {@link #validate} reports it rather
     * than this quietly narrowing.
     */
    @Override
    public GameMenuSize preferredSize(GameMenuSize max, int paddingX, int paddingY) {
        int widest = Math.max(1, this.widestRow());
        int rows = Math.max(1, this.rows.size());

        return new GameMenuSize(
                widest + (widest - 1) * paddingX,
                Math.min(rows + (rows - 1) * paddingY, max.height())
        );
    }

    @Override
    public int pageCount(GameMenuFrame frame) {
        if (!frame.paginated()) {
            return 1;
        }

        return Math.max(1, Mth.positiveCeilDiv(this.rows.size(), frame.rowsPerPage()));
    }

    @Override
    public void place(GameMenuFrame frame, int page, Placer<T> placer) {
        int perPage = frame.rowsPerPage();

        int from = Math.min(page * perPage, this.rows.size());
        int to = Math.min(from + perPage, this.rows.size());
        int count = to - from;

        for (int row = 0; row < count; row++) {
            var elements = this.rows.get(from + row);
            int y = frame.rowOf(row, count);

            for (int column = 0; column < elements.size(); column++) {
                int x = frame.columnOf(column, elements.size());

                if (frame.contains(x, y)) {
                    placer.place(x, y, elements.get(column));
                }
            }
        }
    }

    @Override
    public List<String> validate(GameMenuFrame frame) {
        var problems = new ArrayList<String>();
        int widest = this.widestRow();

        if (widest > frame.itemsPerRow()) {
            problems.add("has a row of " + widest + " elements but the theme fits " + frame.itemsPerRow()
                    + " per row" + (frame.paddingX() > 0 ? " at a padding of " + frame.paddingX() : ""));
        }

        if (!frame.paginated() && this.rows.size() > frame.rowsPerPage()) {
            problems.add("has " + this.rows.size() + " rows but the theme fits " + frame.rowsPerPage()
                    + " and does not paginate");
        }

        return problems;
    }
}
