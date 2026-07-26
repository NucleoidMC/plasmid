package xyz.nucleoid.plasmid.test;

import eu.pb4.sgui.api.elements.SimpleGuiElement;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElement;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Items;
import xyz.nucleoid.plasmid.api.game.common.ui.WaitingLobbyUiLayout;
import xyz.nucleoid.plasmid.impl.game.common.ui.ExtensionGuiElement;
import xyz.nucleoid.plasmid.impl.game.common.ui.WaitingLobbyUiLayoutImpl;

import java.util.List;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;

public class WaitingLobbyUiLayoutTests {
    @BeforeAll
    public static void beforeAll() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        for (var item : BuiltInRegistries.ITEM.asHolderIdMap()) {
            ((Holder.Reference) item).bindComponents(DataComponentMap.EMPTY);
        }
    }

    @Test
    public void emptyLayout() {
        expectBuiltLayout((layout, map) -> {}, "         ");
    }

    @Test
    public void simpleLeadingLayout() {
        expectBuiltLayout((layout, map) -> {
            layout.addLeading(new StaticWaitingLobbyUiElement(map.get('A')));
        }, "A        ");
    }

    @Test
    public void simpleTrailingLayout() {
        expectBuiltLayout((layout, map) -> {
            layout.addTrailing(new StaticWaitingLobbyUiElement(map.get('A')));
        }, "        A");
    }

    @Test
    public void simpleBothLayout() {
        expectBuiltLayout((layout, map) -> {
            layout.addLeading(new StaticWaitingLobbyUiElement(map.get('A')));
            layout.addTrailing(new StaticWaitingLobbyUiElement(map.get('B')));
        }, "A       B");
    }

    @Test
    public void extendedLeadingLayout() {
        expectBuiltLayout((layout, map) -> {
            layout.addLeading(new StaticWaitingLobbyUiElement(map.get('E'), List.of(
                    map.get('A'),
                    map.get('B'),
                    map.get('C'),
                    map.get('D')
            )));
        }, "ABCD     ");
    }

    @Test
    public void extendedTrailingLayout() {
        expectBuiltLayout((layout, map) -> {
            layout.addTrailing(new StaticWaitingLobbyUiElement(map.get('E'), List.of(
                    map.get('A'),
                    map.get('B'),
                    map.get('C'),
                    map.get('D')
            )));
        }, "     DCBA");
    }

    @Test
    public void extendedBothLayout() {
        expectBuiltLayout((layout, map) -> {
            layout.addLeading(new StaticWaitingLobbyUiElement(map.get('E'), List.of(
                    map.get('A'),
                    map.get('B'),
                    map.get('C'),
                    map.get('D')
            )));

            layout.addTrailing(new StaticWaitingLobbyUiElement(map.get('J'), List.of(
                    map.get('F'),
                    map.get('G'),
                    map.get('H'),
                    map.get('I')
            )));
        }, "ABCD IHGF");
    }

    @Test
    public void packedLayout() {
        expectBuiltLayout((layout, map) -> {
            layout.addLeading(new StaticWaitingLobbyUiElement(map.get('D'), List.of(
                    map.get('A'),
                    map.get('B'),
                    map.get('C')
            )));

            layout.addTrailing(new StaticWaitingLobbyUiElement(map.get('K'), List.of(
                    map.get('E'),
                    map.get('F'),
                    map.get('G'),
                    map.get('H'),
                    map.get('I'),
                    map.get('J')
            )));
        }, "ABCJIHGFE");
    }

    @Test
    public void shrunkLayout() {
        expectBuiltLayout((layout, map) -> {
            layout.addLeading(new StaticWaitingLobbyUiElement(map.get('D'), List.of(
                    map.get('A'),
                    map.get('B'),
                    map.get('C')
            )));

            layout.addTrailing(new StaticWaitingLobbyUiElement(map.get('L'), List.of(
                    map.get('E'),
                    map.get('F'),
                    map.get('G'),
                    map.get('H'),
                    map.get('I'),
                    map.get('J'),
                    map.get('K')
            )));
        }, "D KJIHGFE");
    }

    @Test
    public void cannotAddDuplicateElement() {
        assertThrows(IllegalArgumentException.class, () -> {
            var layout = createNonBuildingLayout();

            var guiElement = new GuiElementBuilder(Items.MELON).build();
            var element = new StaticWaitingLobbyUiElement(guiElement);

            layout.addLeading(element);
            layout.addLeading(element);
        });
    }

    @Test
    public void cannotAddDuplicateElementToEitherSide() {
        assertThrows(IllegalArgumentException.class, () -> {
            var layout = createNonBuildingLayout();

            var guiElement = new GuiElementBuilder(Items.PUMPKIN).build();
            var element = new StaticWaitingLobbyUiElement(guiElement);

            layout.addLeading(element);
            layout.addTrailing(element);
        });
    }

    @Test
    public void cannotExceedMaximumElements() {
        assertThrows(IllegalStateException.class, () -> {
            var layout = createNonBuildingLayout();

            for (int i = 0; i < 10; i++) {
                var guiElement = new GuiElementBuilder(Items.DIRT).build();
                var element = new StaticWaitingLobbyUiElement(guiElement);

                if (i % 2 == 0) {
                    layout.addLeading(element);
                } else {
                    layout.addTrailing(element);
                }
            }
        });
    }

    private static void expectBuiltLayout(BiConsumer<WaitingLobbyUiLayout, Char2ObjectMap<GuiElement>> consumer, String expectedPattern) {
        var map = createGuiElementMap();
        var expected = buildGuiElementsFromPattern(expectedPattern, map);

        var layout = WaitingLobbyUiLayout.of(actual -> {
            extractExtensionGuiElements(actual);
            assertArrayEquals(expected, actual);
        });

        consumer.accept(layout, map);
        layout.refresh();
    }

    private static Char2ObjectMap<GuiElement> createGuiElementMap() {
        var map = new Char2ObjectOpenHashMap<GuiElement>();

        for (char c = 'A'; c <= 'Z'; c++) {
            var element = new GuiElementBuilder(Items.PAPER)
                    .setName(Component.literal("" + c))
                    .build();

            map.put(c, element);
        }

        return map;
    }

    private static GuiElement[] buildGuiElementsFromPattern(String pattern, Char2ObjectMap<GuiElement> map) {
        var array = new GuiElement[pattern.length()];

        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            array[i] = c == ' ' ? SimpleGuiElement.EMPTY : map.get(c);
        }

        return array;
    }

    private static void extractExtensionGuiElements(GuiElement[] elements) {
        for (int index = 0; index < elements.length; index++) {
            if (elements[index] instanceof ExtensionGuiElement element) {
                elements[index] = element.delegate();
            }
        }
    }

    private static WaitingLobbyUiLayout createNonBuildingLayout() {
        // WaitingLobbyUiLayout#refresh isn't expected to be called,
        // so the callback can be null
        return new WaitingLobbyUiLayoutImpl(null);
    }
}
