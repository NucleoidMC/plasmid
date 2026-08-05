package xyz.nucleoid.plasmid.test;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import xyz.nucleoid.plasmid.api.menu.*;

import java.util.List;

/**
 * A game contributing an entry of its own, the shape UHC's custom-game creator needs.
 * <p>
 * Clicking opens a menu built in code holding no game entries, so it gets no open-games filter but does get a
 * back button and the theme of the menu it was opened from.
 */
public record TestCustomEntry() implements GameMenuEntry {
    @Override
    public Component name() {
        return Component.literal("Custom Game").withStyle(ChatFormatting.AQUA);
    }

    @Override
    public List<Component> description() {
        return List.of(Component.literal("Built in code, not from a datapack"));
    }

    @Override
    public ItemStack icon() {
        return new ItemStack(Items.COMPASS);
    }

    @Override
    public void click(ServerPlayer player, boolean alt) {
        GameMenu.builder(Component.literal("Custom Game Settings"))
                .layout(GameMenuLayout.rows(
                        List.of(setting("Mode", Items.REDSTONE_TORCH), setting("Team size", Items.SHIELD)),
                        List.of(setting("World border", Items.BARRIER), setting("PvP delay", Items.CLOCK))))
                .features()
                .open(player);
    }

    private static GameMenuElement setting(String label, Item item) {
        return GameMenuElement.of(new Setting(label, item));
    }

    private record Setting(String label, Item item) implements GameMenuEntry {
        @Override
        public Component name() {
            return Component.literal(this.label).withStyle(ChatFormatting.YELLOW);
        }

        @Override
        public List<Component> description() {
            return List.of(Component.literal("A setting of the custom game"));
        }

        @Override
        public ItemStack icon() {
            return new ItemStack(this.item);
        }

        @Override
        public void click(ServerPlayer player, boolean alt) {
            player.sendSystemMessage(Component.literal("Toggled " + this.label).withStyle(ChatFormatting.GREEN));
        }
    }

    public record Config() implements GameMenuEntryConfig {
        public static final MapCodec<Config> CODEC = MapCodec.unit(Config::new);

        @Override
        public GameMenuEntry createEntry() {
            return new TestCustomEntry();
        }

        @Override
        public MapCodec<? extends GameMenuEntryConfig> codec() {
            return CODEC;
        }
    }
}
