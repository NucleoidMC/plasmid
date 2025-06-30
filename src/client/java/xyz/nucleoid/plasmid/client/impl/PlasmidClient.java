package xyz.nucleoid.plasmid.client.impl;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import xyz.nucleoid.plasmid.client.impl.screen.GamesScreen;

public class PlasmidClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof TitleScreen) {
                addTitleScreenButton(client, screen, width, height);
            }
        });
    }

    private static void addTitleScreenButton(MinecraftClient client, Screen screen, int width, int height) {
        var buttons = Screens.getButtons(screen);

        int x = (width - ButtonWidget.field_49479) / 2;
        int y = 0;

        int index = 0;
        int insertIndex = 0;

        for (var button : buttons) {
            if (button.getX() == x && button.getWidth() == ButtonWidget.field_49479) {
                if (button.getY() > y) {
                    y = button.getY();
                    insertIndex = index + 1;
                }

                button.setY(button.getY() - 24);
            }

            index += 1;
        }

        var gamesButton = ButtonWidget.builder(GamesScreen.TITLE, button -> {
            GamesScreen.show(client, screen);
        })
                .position(x, y)
                .width(ButtonWidget.field_49479)
                .build();

        buttons.add(insertIndex, gamesButton);
    }
}
