package xyz.nucleoid.plasmid.impl.portal.menu;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public record InvalidMenuEntry(Component name) implements MenuEntry {
    private static final Component DEFAULT_NAME = Component.translatable("text.plasmid.ui.game_join.invalid.name").withStyle(ChatFormatting.RED);
    private static final List<Component> DESCRIPTION = Collections.singletonList(Component.translatable("text.plasmid.ui.game_join.invalid.description"));
    private static final ItemStack ICON = Items.BARRIER.getDefaultInstance();

    public InvalidMenuEntry(Optional<Component> name) {
        this(name.orElse(DEFAULT_NAME));
    }

    @Override
    public List<Component> description() {
        return DESCRIPTION;
    }

    @Override
    public ItemStack icon() {
        return ICON;
    }

    @Override
    public void click(ServerPlayer player, boolean alt) {
    }

    @Override
    public int getPlayerCount() {
        return -1;
    }
}
