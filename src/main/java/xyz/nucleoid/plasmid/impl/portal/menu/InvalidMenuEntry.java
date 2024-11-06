package xyz.nucleoid.plasmid.impl.portal.menu;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record InvalidMenuEntry(Text name) implements MenuEntry {
    private static final Text DEFAULT_NAME = Text.translatable("text.plasmid.ui.game_join.invalid.name").formatted(Formatting.RED);
    private static final List<Text> DESCRIPTION = Collections.singletonList(Text.translatable("text.plasmid.ui.game_join.invalid.description"));
    private static final ItemStack ICON = Items.BARRIER.getDefaultStack();

    public InvalidMenuEntry(Optional<Text> name) {
        this(name.orElse(DEFAULT_NAME));
    }

    @Override
    public List<Text> description() {
        return DESCRIPTION;
    }

    @Override
    public ItemStack icon() {
        return ICON;
    }

    @Override
    public void click(ServerPlayerEntity player) {
    }

    @Override
    public int getPlayerCount() {
        return -1;
    }
}
