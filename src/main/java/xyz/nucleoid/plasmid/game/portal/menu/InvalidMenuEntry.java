package xyz.nucleoid.plasmid.game.portal.menu;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.GameSpace;

public record InvalidMenuEntry(Text name) implements MenuEntry {
    private static final Text DEFAULT_NAME = new TranslatableText("text.plasmid.ui.game_join.invalid.name").formatted(Formatting.RED);
    private static final List<Text> DESCRIPTION = Collections.singletonList(new TranslatableText("text.plasmid.ui.game_join.invalid.description"));
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
    public void click(ServerPlayerEntity player, CompletableFuture<GameSpace> future) {
        return;
    }

    @Override
    public int getPlayerCount() {
        return 0;
    }
}
