package xyz.nucleoid.plasmid.shop;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

public final class ShopUi {
    static final int WIDTH = 9;

    private ShopUi() {
    }

    public static SimpleGui create(ServerPlayerEntity player, Text title, Consumer<ShopBuilder> builder) {
        var shop = new ShopBuilder();
        builder.accept(shop);

        ScreenHandlerType<?> type = selectScreenType(shop.rows.size());

        var gui = new SimpleGui(type, player, false);
        gui.setTitle(title);

        List<List<ShopEntry>> rows = shop.rows;
        for (int rowIdx = 0; rowIdx < rows.size(); rowIdx++) {
            List<ShopEntry> row = rows.get(rowIdx);
            int minColumn = (WIDTH - row.size()) / 2;
            for (int columnIdx = 0; columnIdx < row.size(); columnIdx++) {
                ShopEntry element = row.get(columnIdx);
                int slotIdx = (columnIdx + minColumn) + rowIdx * WIDTH;
                gui.setSlot(slotIdx, element.createGuiElement(player));
            }
        }

        return gui;
    }

    private static ScreenHandlerType<?> selectScreenType(int rowCount) {
        return switch (rowCount) {
            case 1 -> ScreenHandlerType.GENERIC_9X1;
            case 2 -> ScreenHandlerType.GENERIC_9X2;
            case 3 -> ScreenHandlerType.GENERIC_9X3;
            case 4 -> ScreenHandlerType.GENERIC_9X4;
            case 5 -> ScreenHandlerType.GENERIC_9X5;
            default -> ScreenHandlerType.GENERIC_9X6;
        };
    }
}
