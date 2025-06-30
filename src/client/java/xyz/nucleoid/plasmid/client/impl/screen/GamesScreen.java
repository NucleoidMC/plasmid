package xyz.nucleoid.plasmid.client.impl.screen;

import net.fabricmc.fabric.impl.resource.loader.ModResourcePackCreator;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.LevelScreenProvider;
import net.minecraft.client.gui.screen.world.WorldCreationSettings;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.VanillaDataPackProvider;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.SaveLoading;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Util;
import net.minecraft.world.gen.FlatLevelGeneratorPreset;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.level.WorldGenSettings;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.client.impl.GameWorldCreator;

import java.util.function.Function;

public class GamesScreen extends Screen {
    public static final Text TITLE = Text.translatable("text.plasmid.menu.games");
    public static final Text PREPARING_MESSAGE = Text.translatable("text.plasmid.menu.games.preparing");

    private static final Text SEARCH_TEXT = Text.translatable("text.plasmid.menu.games.search");

    private static final Text PLAY_BUTTON = Text.translatable("text.plasmid.menu.games.play");
    private static final Text CANCEL_BUTTON = ScreenTexts.CANCEL;

    protected final MinecraftClient client;
    private final Screen parent;
    private final GeneratorOptionsHolder generatorOptionsHolder;

    private TextFieldWidget searchBox;
    private ButtonWidget playButton;

    private RegistryEntry<GameConfig<?>> selectedGame;

    public GamesScreen(MinecraftClient client, Screen parent, GeneratorOptionsHolder generatorOptionsHolder) {
        super(TITLE);

        this.client = client;
        this.parent = parent;
        this.generatorOptionsHolder = generatorOptionsHolder;
    }

    public void setSelectedGame(RegistryEntry<GameConfig<?>> selectedGame) {
        this.selectedGame = selectedGame;
        this.playButton.active = selectedGame != null;
    }

    public void play() {
        GameWorldCreator.create(this.client, this.generatorOptionsHolder, this.selectedGame);
    }

    @Override
    protected void init() {
        var gameList = new GameListWidget(this.generatorOptionsHolder.getCombinedRegistryManager(), this);

        this.searchBox = new TextFieldWidget(this.textRenderer, (this.width - ButtonWidget.field_49479) / 2, 22, ButtonWidget.field_49479, ButtonWidget.DEFAULT_HEIGHT, this.searchBox, SEARCH_TEXT);
        this.searchBox.setChangedListener(search -> gameList.setSearch(search));

        this.playButton = ButtonWidget.builder(PLAY_BUTTON, button -> this.play())
                .position(this.width / 2 - 155, this.height - 28)
                .build();

        var cancelButton = ButtonWidget.builder(CANCEL_BUTTON, button -> this.close())
                .position(this.width / 2 + 5, this.height - 28)
                .build();

        this.addDrawableChild(this.searchBox);
        this.addDrawableChild(gameList);
        this.addDrawableChild(this.playButton);
        this.addDrawableChild(cancelButton);

        this.setSelectedGame(this.selectedGame);
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.searchBox);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, Colors.WHITE);
    }

    public static void show(MinecraftClient client, Screen parent) {
        Function<SaveLoading.LoadContextSupplierContext, WorldGenSettings> settingsSupplier = context -> new WorldGenSettings(
                GeneratorOptions.createTestWorld(),
                WorldPresets.createTestOptions(context.worldGenRegistryManager())
        );

        CreateWorldScreen.showMessage(client, PREPARING_MESSAGE);
        var packManager = new ResourcePackManager(new VanillaDataPackProvider(client.getSymlinkFinder()));

        // See https://github.com/FabricMC/fabric/blob/9ceeb58c7d15e9a5a9e311b0595d7908b8d5f3b3/fabric-resource-loader-v0/src/client/java/net/fabricmc/fabric/mixin/resource/loader/client/CreateWorldScreenMixin.java
        packManager.providers.add(new ModResourcePackCreator(ResourceType.SERVER_DATA));
        var config = CreateWorldScreen.createServerConfig(packManager, ModResourcePackUtil.createDefaultDataConfiguration());

        var future = SaveLoading.load(
                config,
                context -> new SaveLoading.LoadContext<>(
                        new WorldCreationSettings(settingsSupplier.apply(context),
                        context.dataConfiguration()),
                        context.dimensionsRegistryManager()
                ),
                (resourceManager, dataPackContents, registries, settings) -> {
                    resourceManager.close();

                    RegistryEntry<FlatLevelGeneratorPreset> preset = registries.getCombinedRegistryManager()
                            .getOrThrow(RegistryKeys.FLAT_LEVEL_GENERATOR_PRESET)
                            .getOrThrow(GameWorldCreator.OPTIONS.flatLevelPreset());

                    return new GeneratorOptionsHolder(
                            settings.worldGenSettings().generatorOptions(),
                            settings.worldGenSettings().dimensionOptionsRegistryHolder(),
                            registries,
                            dataPackContents,
                            settings.dataConfiguration(),
                            GameWorldCreator.OPTIONS
                    ).apply(LevelScreenProvider.createModifier(preset.value().settings()));
                },
                Util.getMainWorkerExecutor(),
                client
        );

        client.runTasks(future::isDone);
        client.setScreen(new GamesScreen(client, parent, future.join()));
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}
