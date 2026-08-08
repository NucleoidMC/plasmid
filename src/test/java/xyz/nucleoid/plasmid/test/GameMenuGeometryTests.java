package xyz.nucleoid.plasmid.test;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.inventory.MenuType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import xyz.nucleoid.plasmid.api.menu.*;
import xyz.nucleoid.plasmid.impl.menu.SimpleGameMenuTheme;
import xyz.nucleoid.plasmid.impl.menu.StyledGameMenuTheme;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The sizing contract: a menu asks for a region, a theme adds its chrome around it, and the container is
 * grown to hold both. Nothing here should ever cut a menu down silently.
 */
public class GameMenuGeometryTests {
    private static final GameMenuInsets BARE = GameMenuInsets.NONE;
    private static final GameMenuInsets BOTTOM_BAR = GameMenuInsets.ofRows(0, 1);
    private static final GameMenuInsets HEADER_AND_BAR = GameMenuInsets.ofRows(1, 1);

    @BeforeAll
    public static void beforeAll() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private static GameMenuFrame resolve(GameMenuSize size, GameMenuInsets insets, List<String> problems) {
        return GameMenuFrame.resolve(size, insets, false, true, problems::add);
    }

    private static GameMenuFrame resolve(GameMenuSize size, GameMenuInsets insets) {
        var problems = new ArrayList<String>();
        var frame = resolve(size, insets, problems);
        assertEquals(List.of(), problems, "expected no problems");
        return frame;
    }

    @Test
    public void chromeGrowsTheContainerRatherThanShrinkingTheMenu() {
        var frame = resolve(new GameMenuSize(9, 4), HEADER_AND_BAR);

        assertEquals(6, frame.containerRows());
        assertEquals(MenuType.GENERIC_9x6, frame.type());
        assertEquals(4, frame.height(), "the menu keeps every row it asked for");
        assertEquals(1, frame.y(), "and sits below the header");
        assertEquals(9, frame.width());
        assertEquals(0, frame.x());
    }

    @Test
    public void theSameMenuIsSmallerUnderALighterTheme() {
        assertEquals(4, resolve(new GameMenuSize(9, 4), BARE).containerRows());
        assertEquals(5, resolve(new GameMenuSize(9, 4), BOTTOM_BAR).containerRows());
        assertEquals(6, resolve(new GameMenuSize(9, 4), HEADER_AND_BAR).containerRows());
    }

    @Test
    public void aSmallMenuGetsASmallContainer() {
        var frame = resolve(new GameMenuSize(3, 1), HEADER_AND_BAR);

        assertEquals(3, frame.containerRows());
        assertEquals(MenuType.GENERIC_9x3, frame.type());
        assertEquals(1, frame.height());
        assertEquals(3, frame.width());
        assertEquals(3, frame.x(), "narrow content is centred in what the theme left free");
    }

    @Test
    public void sideInsetsNarrowTheRegionAndOffsetIt() {
        var frame = resolve(new GameMenuSize(7, 3), new GameMenuInsets(1, 1, 1, 1, 0, 0));

        assertEquals(5, frame.containerRows());
        assertEquals(7, frame.width());
        assertEquals(1, frame.x());
        assertEquals(1, frame.y());
    }

    @Test
    public void aMenuTooTallForTheChromeIsCutDownAndReported() {
        var problems = new ArrayList<String>();
        var frame = resolve(new GameMenuSize(9, 6), HEADER_AND_BAR, problems);

        assertEquals(GameMenuFrame.MAX_ROWS, frame.containerRows(), "never past a vanilla container");
        assertEquals(4, frame.height());
        assertEquals(1, problems.size(), () -> "expected one problem, got " + problems);
        assertTrue(problems.getFirst().contains("9:6"), problems::getFirst);
        assertTrue(problems.getFirst().contains("9:4"), problems::getFirst);
        assertTrue(problems.getFirst().contains("row above"), problems::getFirst);
    }

    @Test
    public void aMenuTooWideForTheChromeIsCutDownAndReported() {
        var problems = new ArrayList<String>();
        var frame = resolve(new GameMenuSize(9, 2), new GameMenuInsets(0, 0, 2, 0, 0, 0), problems);

        assertEquals(7, frame.width());
        assertEquals(2, frame.x());
        assertEquals(1, problems.size(), () -> "expected one problem, got " + problems);
    }

    @Test
    public void insetsUnionCoversAThemeAndItsFallback() {
        // A theme that hands over to another for some players reports both, so a menu that would not fit
        // either look is reported at load rather than when the right player opens it.
        var artwork = GameMenuInsets.ofRows(0, 1);
        var fallback = new GameMenuInsets(1, 1, 1, 0, 1, 0);

        var both = artwork.max(fallback);

        assertEquals(new GameMenuInsets(1, 1, 1, 0, 1, 0), both);
        assertEquals(new GameMenuSize(8, 4), both.available());
        assertTrue(both.available().fitsIn(artwork.available()), "the union can only be smaller than either");
        assertTrue(both.available().fitsIn(fallback.available()));
    }

    @Test
    public void aThemeWithAFallbackStillDrawsWithItsOwnInsets() {
        // The union belongs to validation. Letting it reach drawing gave the artwork look a row of chrome
        // that only the fallback ever asked for, which showed up as a stray line above every menu.
        var theme = new StyledGameMenuTheme(
                GameMenuInsets.ofRows(0, 1),
                Optional.empty(), Optional.empty(), Optional.empty(),
                GameMenuTextStyle.NONE, GameMenuElementStyle.NONE, GameMenuElementStyle.NONE,
                Map.of(), Optional.empty(),
                Optional.of(Holder.direct(new SimpleGameMenuTheme(
                        GameMenuInsets.ofRows(1, 1), Optional.empty(), Optional.empty()))));

        assertEquals(GameMenuInsets.ofRows(1, 1), theme.baseInsets(), "validation sees both looks");
        assertEquals(GameMenuInsets.ofRows(0, 1), theme.insets(null), "but this look draws with its own");
    }

    @Test
    public void insetsCannotReserveAContainerOutOfExistence() {
        var insets = new GameMenuInsets(5, 5, 8, 8, 0, 0);

        assertEquals(new GameMenuSize(1, 1), insets.available());
        assertEquals(1, resolve(new GameMenuSize(1, 1), insets).height());
        assertEquals(GameMenuFrame.MAX_ROWS, resolve(new GameMenuSize(1, 1), insets).containerRows());
    }

    @Test
    public void flowAsksForOneRowWhenItHasFewElements() {
        var max = BOTTOM_BAR.available();

        assertEquals(new GameMenuSize(5, 1), GameMenuLayout.flow(List.of(1, 2, 3)).preferredSize(max, 1, 0),
                "three elements at a padding of one span five slots on one row");
        assertEquals(new GameMenuSize(3, 1), GameMenuLayout.flow(List.of(1, 2, 3)).preferredSize(max, 0, 0));
    }

    @Test
    public void flowStopsAtTheMaximumAndLeavesTheRestToPaging() {
        var max = BOTTOM_BAR.available();
        var many = new ArrayList<Integer>();
        for (int i = 0; i < 100; i++) {
            many.add(i);
        }

        var size = GameMenuLayout.flow(many).preferredSize(max, 1, 0);

        assertTrue(size.fitsIn(max), () -> size + " should fit " + max);
        assertEquals(max.height(), size.height());
    }

    @Test
    public void rowsAsksForOneRegionRowPerDeclaredRow() {
        var max = BOTTOM_BAR.available();
        var layout = GameMenuLayout.rows(List.of(1, 2), List.of(3, 4, 5));

        assertEquals(new GameMenuSize(5, 2), layout.preferredSize(max, 1, 0));
        assertEquals(new GameMenuSize(3, 3), layout.preferredSize(max, 0, 1),
                "vertical padding costs a region row between each");
    }

    @Test
    public void rowsAsksForItsFullWidthEvenWhenItCannotFit() {
        var max = BOTTOM_BAR.available();
        var layout = GameMenuLayout.rows(List.of(1, 2, 3, 4, 5, 6, 7));

        // Nine columns cannot hold seven elements once padded, and a row cannot be broken up, so the ask
        // stands and resolving is what reports it.
        assertEquals(13, layout.preferredSize(max, 1, 0).width());
        assertFalse(layout.preferredSize(max, 1, 0).fitsIn(max));
    }

    @Test
    public void fixedAsksForItsOwnHeightAndTheFullWidth() {
        var layout = GameMenuLayout.fixed(Map.of(
                new GameMenuLayout.Pos(2, 1), 1,
                new GameMenuLayout.Pos(6, 3), 2));

        assertEquals(new GameMenuSize(9, 4), layout.preferredSize(BOTTOM_BAR.available(), 1, 1),
                "coordinates are absolute, so padding does not apply and the width is not trimmed");
        assertEquals(new GameMenuSize(7, 4), layout.preferredSize(new GameMenuSize(7, 5), 0, 0),
                "but a theme keeping columns still narrows it");
    }

    @Test
    public void fixedCoordinatesAreNotShiftedByCentring() {
        // A block that does not reach the right edge used to be centred, which moved every cell across by
        // the margin the author had already written into the coordinates.
        var layout = GameMenuLayout.fixed(Map.of(
                new GameMenuLayout.Pos(0, 0), 1,
                new GameMenuLayout.Pos(2, 0), 2,
                new GameMenuLayout.Pos(6, 0), 3));

        var frame = resolve(layout.preferredSize(BOTTOM_BAR.available(), 0, 0), BOTTOM_BAR);
        assertEquals(Map.of(1, 0, 2, 2, 3, 6), placedSlots(layout, frame));

        // One column kept either side moves the whole block across by exactly that column, and no more.
        var inset = new GameMenuInsets(0, 1, 1, 1, 0, 0);
        var narrowed = resolve(layout.preferredSize(inset.available(), 0, 0), inset);
        assertEquals(Map.of(1, 1, 2, 3, 3, 7), placedSlots(layout, narrowed));
    }

    @Test
    public void emptyLayoutsStillAskForSomethingDrawable() {
        var max = BOTTOM_BAR.available();

        assertEquals(new GameMenuSize(1, 1), GameMenuLayout.flow(List.of()).preferredSize(max, 0, 0));
        assertEquals(new GameMenuSize(max.width(), 1), GameMenuLayout.fixed(Map.of()).preferredSize(max, 0, 0));
    }

    private static GameMenuSize sized(GameMenuSizeRule sizing, GameMenuLayout<?> layout, GameMenuInsets insets, GameMenuSize inherited) {
        return sizing.resolve(insets.available(), layout, insets.paddingX(), insets.paddingY(), inherited);
    }

    @Test
    public void sizingTakesAKeywordOrASizeInTheOneField() {
        assertEquals(GameMenuSizeRule.Auto.FIT, GameMenuSizeRule.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive("fit")).getOrThrow());
        assertEquals(GameMenuSizeRule.Auto.INHERIT, GameMenuSizeRule.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive("inherit")).getOrThrow());
        assertEquals(GameMenuSizeRule.of(9, 4), GameMenuSizeRule.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive("9:4")).getOrThrow());

        var object = new JsonObject();
        object.addProperty("width", 9);
        object.addProperty("height", 4);
        assertEquals(GameMenuSizeRule.of(9, 4), GameMenuSizeRule.CODEC.parse(JsonOps.INSTANCE, object).getOrThrow());

        // Both keywords and sizes have to survive the round trip, which is why this is not withAlternative.
        assertEquals(new JsonPrimitive("inherit"), GameMenuSizeRule.CODEC.encodeStart(JsonOps.INSTANCE, GameMenuSizeRule.Auto.INHERIT).getOrThrow());
        assertEquals(new JsonPrimitive("9:4"), GameMenuSizeRule.CODEC.encodeStart(JsonOps.INSTANCE, GameMenuSizeRule.of(9, 4)).getOrThrow());

        assertTrue(GameMenuSizeRule.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive("huge")).isError());
        assertTrue(GameMenuSizeRule.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive("9x4")).isError());
    }

    @Test
    public void sizesReadEitherFormAndAreBoundedByTheContainer() {
        var object = new JsonObject();
        object.addProperty("width", 7);
        object.addProperty("height", 3);

        assertEquals(new GameMenuSize(7, 3), GameMenuSize.CODEC.parse(JsonOps.INSTANCE, object).getOrThrow());
        assertEquals(new GameMenuSize(7, 3), GameMenuSize.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive("7:3")).getOrThrow());

        // The short form is what gets written back, whichever way it was read.
        assertEquals(new JsonPrimitive("7:3"), GameMenuSize.CODEC.encodeStart(JsonOps.INSTANCE, new GameMenuSize(7, 3)).getOrThrow());

        assertTrue(GameMenuSize.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive("0:3")).isError());
        assertTrue(GameMenuSize.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive("9:7")).isError(), "taller than any container");
        assertTrue(GameMenuSize.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive("10:3")).isError(), "wider than any container");
    }

    @Test
    public void inheritTakesTheOpeningMenusRegionWhenItIsBigger() {
        var twoEntries = GameMenuLayout.flow(List.of(1, 2));

        // On its own it fits itself; opened from something larger it matches, per axis.
        assertEquals(new GameMenuSize(2, 1), sized(GameMenuSizeRule.Auto.INHERIT, twoEntries, BOTTOM_BAR, null));
        assertEquals(new GameMenuSize(9, 3), sized(GameMenuSizeRule.Auto.INHERIT, twoEntries, BOTTOM_BAR, new GameMenuSize(9, 3)));
        assertEquals(new GameMenuSize(9, 1), sized(GameMenuSizeRule.Auto.INHERIT, twoEntries, BOTTOM_BAR, new GameMenuSize(9, 1)));
    }

    @Test
    public void inheritNeverShrinksAMenuBelowWhatItNeeds() {
        var many = new ArrayList<Integer>();
        for (int i = 0; i < 20; i++) {
            many.add(i);
        }

        var own = sized(GameMenuSizeRule.Auto.FIT, GameMenuLayout.flow(many), BOTTOM_BAR, null);
        var inheriting = sized(GameMenuSizeRule.Auto.INHERIT, GameMenuLayout.flow(many), BOTTOM_BAR, new GameMenuSize(1, 1));

        assertEquals(own, inheriting, "a smaller opening menu must not squeeze this one");
    }

    @Test
    public void aFixedSizeIgnoresBothTheLayoutAndTheOpeningMenu() {
        var sizing = GameMenuSizeRule.of(9, 4);
        var twoEntries = GameMenuLayout.flow(List.of(1, 2));

        assertEquals(new GameMenuSize(9, 4), sized(sizing, twoEntries, BOTTOM_BAR, null));
        assertEquals(new GameMenuSize(9, 4), sized(sizing, twoEntries, BOTTOM_BAR, new GameMenuSize(9, 1)));
    }

    @Test
    public void featureSlotsAnchorToTheEdgeTheyWereWrittenAgainst() {
        var layout = GameMenuFeatureLayout.EMPTY
                .with(GameMenuFeatures.CLOSE, 0, -1)
                .with(GameMenuFeatures.BACK, -1, -1);

        // The same layout, in two containers of different height.
        assertEquals(2 * 9, layout.slot(GameMenuFeatures.CLOSE, 9, 3));
        assertEquals(5 * 9, layout.slot(GameMenuFeatures.CLOSE, 9, 6));
        assertEquals(2 * 9 + 8, layout.slot(GameMenuFeatures.BACK, 9, 3));
        assertEquals(5 * 9 + 8, layout.slot(GameMenuFeatures.BACK, 9, 6));
    }

    @Test
    public void featuresPlacedOutsideTheContainerAreNotDrawn() {
        var layout = GameMenuFeatureLayout.EMPTY.with(GameMenuFeatures.CLOSE, 0, 5);

        assertEquals(5 * 9, layout.slot(GameMenuFeatures.CLOSE, 9, 6));
        assertEquals(-1, layout.slot(GameMenuFeatures.CLOSE, 9, 3), "row 5 does not exist in a 3-row container");
        assertEquals(-1, layout.slot(GameMenuFeatures.BACK, 9, 6), "unplaced features are never drawn");
    }

    /**
     * Where a layout's elements end up in the container, which is what the inversion could quietly break.
     */
    private static Map<Integer, Integer> placedSlots(GameMenuLayout<Integer> layout, GameMenuFrame frame) {
        var slots = new java.util.LinkedHashMap<Integer, Integer>();
        layout.place(frame, 0, (x, y, element) -> slots.put(element, frame.slot(x, y)));
        return slots;
    }

    @Test
    public void contentIsPlacedBelowTheRowsTheThemeKept() {
        var frame = resolve(new GameMenuSize(9, 2), HEADER_AND_BAR);
        var placed = placedSlots(GameMenuLayout.rows(List.of(1, 2, 3), List.of(4)), frame);

        assertEquals(4, frame.containerRows());
        // Row 0 is the header, so the first content row is slots 9..17 and the second 18..26.
        assertEquals(Map.of(1, 9 + 3, 2, 9 + 4, 3, 9 + 5, 4, 18 + 4), placed);
    }

    @Test
    public void sideInsetsShiftContentAcrossTheContainer() {
        var insets = new GameMenuInsets(1, 1, 1, 1, 0, 0);
        var frame = resolve(new GameMenuSize(7, 1), insets);

        var placed = placedSlots(GameMenuLayout.fixed(Map.of(
                new GameMenuLayout.Pos(0, 0), 1,
                new GameMenuLayout.Pos(6, 0), 2)), frame);

        assertEquals(3, frame.containerRows());
        // One row and one column kept, so the region starts at slot 10 and ends at slot 16.
        assertEquals(Map.of(1, 10, 2, 16), placed);
    }

    @Test
    public void everySlotOfTheRegionIsInsideTheContainer() {
        for (var insets : List.of(BARE, BOTTOM_BAR, HEADER_AND_BAR, new GameMenuInsets(2, 1, 3, 1, 1, 1))) {
            for (int height = 1; height <= GameMenuFrame.MAX_ROWS; height++) {
                for (int width = 1; width <= GameMenuFrame.MAX_COLUMNS; width++) {
                    var frame = GameMenuFrame.resolve(new GameMenuSize(width, height), insets, false, true, problem -> {});
                    int size = frame.containerRows() * GameMenuFrame.MAX_COLUMNS;

                    int last = frame.slot(frame.width() - 1, frame.height() - 1);
                    assertTrue(frame.slot(0, 0) >= 0 && last < size,
                            () -> "region " + frame + " escapes its " + frame.containerRows() + "-row container");
                }
            }
        }
    }

    @Test
    public void theStandardBarFollowsTheRowTheThemeKeptFree() {
        var withBar = GameMenuFeatureLayout.standard(resolve(new GameMenuSize(9, 2), BOTTOM_BAR));
        assertEquals(new GameMenuLayout.Pos(4, 2), withBar.pos(GameMenuFeatures.GAME_FILTER));

        var taller = GameMenuFeatureLayout.standard(resolve(new GameMenuSize(9, 4), BOTTOM_BAR));
        assertEquals(new GameMenuLayout.Pos(4, 4), taller.pos(GameMenuFeatures.GAME_FILTER));

        // Nothing kept below, so the bar goes in the row kept above instead.
        var headerOnly = GameMenuFeatureLayout.standard(resolve(new GameMenuSize(9, 4), GameMenuInsets.ofRows(1, 0)));
        assertEquals(new GameMenuLayout.Pos(4, 0), headerOnly.pos(GameMenuFeatures.GAME_FILTER));

        // Nowhere at all to put it without covering an entry.
        assertEquals(GameMenuFeatureLayout.EMPTY, GameMenuFeatureLayout.standard(resolve(new GameMenuSize(9, 4), BARE)));
    }
}
