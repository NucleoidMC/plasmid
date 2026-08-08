package xyz.nucleoid.plasmid.api.menu;

import net.minecraft.util.Mth;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * The geometry one menu is drawn with: how tall a container it earned, and which part of it the contents
 * occupy. Everything outside the content region belongs to the theme.
 *
 * <p>Nothing declares a frame. {@link #resolve} works one out from the {@link GameMenuSize} the menu asks
 * for and the {@link GameMenuInsets} the theme adds around it, so the container grows and shrinks with the
 * menu instead of every menu being stretched to one container.
 *
 * @param containerRows rows of the container shown to the client, 1 to {@value #MAX_ROWS}
 * @param includePlayerSlots whether the player inventory is part of the addressable area
 * @param x left edge of the content region, in slots
 * @param y top edge of the content region, in rows
 * @param width width of the content region, in slots
 * @param height height of the content region, in rows
 * @param paddingX empty slots between horizontally adjacent elements
 * @param paddingY empty rows between element rows
 * @param paginated whether overflowing content may be paged
 */
public record GameMenuFrame(
        int containerRows,
        boolean includePlayerSlots,
        int x,
        int y,
        int width,
        int height,
        int paddingX,
        int paddingY,
        boolean paginated
) {
    /**
     * Rows in the tallest vanilla container a menu can be shown in.
     */
    public static final int MAX_ROWS = 6;

    /**
     * Columns in every container a menu is shown in.
     */
    public static final int MAX_COLUMNS = 9;

    /**
     * Works out the frame a menu gets under a theme: the content region is whatever the menu asks for, cut
     * down only if the theme's own chrome leaves less room, and the container is grown to hold both.
     *
     * @param inherited the region the opening menu got, for {@link GameMenuSizeRule.Auto#INHERIT}
     * @param problems receives what could not be honoured, phrased to follow "it ..."
     */
    public static GameMenuFrame resolve(
            GameMenuTheme theme,
            GameMenuInsets insets,
            GameMenuSizeRule sizeRule,
            GameMenuLayout<?> layout,
            @Nullable GameMenuSize inherited,
            Consumer<String> problems
    ) {
        var wanted = sizeRule.resolve(insets.available(), layout, insets.paddingX(), insets.paddingY(), inherited);

        return resolve(wanted, insets, theme.includePlayerSlots(), theme.paginated(), problems);
    }

    /**
     * Fits a content region of {@code size} inside what {@code insets} leaves free.
     *
     * @param problems receives what could not be honoured, phrased to follow "it ..."
     */
    public static GameMenuFrame resolve(
            GameMenuSize size,
            GameMenuInsets insets,
            boolean includePlayerSlots,
            boolean paginated,
            Consumer<String> problems
    ) {
        var available = insets.available();
        var region = size.clampTo(available);

        if (!size.fitsIn(available)) {
            problems.accept("wants a " + size + " region but the theme leaves " + available
                    + " free, keeping its " + insets.describe());
        }

        // The container is sized to the content, not the content to the container. A two-row menu gets a
        // two-row window even under a theme built for tall ones.
        int containerRows = Mth.clamp(insets.top() + region.height() + insets.bottom(), 1, MAX_ROWS);

        // Narrow content is centred in what the theme left free, so a short row does not hug the left edge.
        int x = insets.left() + Math.max(0, (available.width() - region.width()) / 2);

        return new GameMenuFrame(containerRows, includePlayerSlots, x, insets.top(),
                region.width(), region.height(), insets.paddingX(), insets.paddingY(), paginated);
    }

    /**
     * The container shown to the client, the shortest vanilla one holding {@link #containerRows}.
     */
    public MenuType<?> type() {
        return switch (this.containerRows) {
            case 1 -> MenuType.GENERIC_9x1;
            case 2 -> MenuType.GENERIC_9x2;
            case 3 -> MenuType.GENERIC_9x3;
            case 4 -> MenuType.GENERIC_9x4;
            case 5 -> MenuType.GENERIC_9x5;
            default -> MenuType.GENERIC_9x6;
        };
    }

    public int containerWidth() {
        return MAX_COLUMNS;
    }

    /**
     * How many elements fit side by side once padding is taken out.
     */
    public int itemsPerRow() {
        return Math.max(1, (this.width + this.paddingX) / (1 + this.paddingX));
    }

    /**
     * How many element rows fit once padding is taken out.
     */
    public int rowsPerPage() {
        return Math.max(1, (this.height + this.paddingY) / (1 + this.paddingY));
    }

    /**
     * How many elements fit on one page.
     */
    public int capacity() {
        return this.itemsPerRow() * this.rowsPerPage();
    }

    /**
     * The column of element {@code index} in a centred, padded row of {@code count} elements. At a padding
     * of one across nine slots that gives columns 3 and 5 for two elements, 2, 4 and 6 for three.
     */
    public int columnOf(int index, int count) {
        int span = count + (count - 1) * this.paddingX;
        return Math.max(0, (this.width - span) / 2) + index * (1 + this.paddingX);
    }

    /**
     * The row of row {@code index} in a vertically centred block of {@code count} rows.
     */
    public int rowOf(int index, int count) {
        int span = count + (count - 1) * this.paddingY;
        return Math.max(0, (this.height - span) / 2) + index * (1 + this.paddingY);
    }

    /**
     * Whether a cell falls inside the content region.
     */
    public boolean contains(int cellX, int cellY) {
        return cellX >= 0 && cellX < this.width && cellY >= 0 && cellY < this.height;
    }

    /**
     * Absolute container slot of a content-region cell.
     */
    public int slot(int cellX, int cellY) {
        return (this.x + cellX) + (this.y + cellY) * MAX_COLUMNS;
    }
}
