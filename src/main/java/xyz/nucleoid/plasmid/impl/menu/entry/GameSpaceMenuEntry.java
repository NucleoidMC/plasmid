package xyz.nucleoid.plasmid.impl.menu.entry;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.api.game.player.GamePlayerJoiner;
import xyz.nucleoid.plasmid.api.game.player.JoinIntent;
import xyz.nucleoid.plasmid.api.menu.GameMenuEntry;

import java.util.List;
import java.util.function.Consumer;

/**
 * A running game presented as a menu entry, used to build the open-games view. Going through
 * {@link GameMenuEntry} means the active theme restyles open games like anything else.
 */
public record GameSpaceMenuEntry(GameSpace gameSpace) implements GameMenuEntry {
    @Override
    public Component name() {
        return GameConfig.name(this.gameSpace.getMetadata().sourceConfig());
    }

    @Override
    public List<Component> description() {
        return this.gameSpace.getMetadata().sourceConfig().value().description();
    }

    @Override
    public ItemStack icon() {
        return this.gameSpace.getMetadata().sourceConfig().value().icon();
    }

    @Override
    public void click(ServerPlayer player, boolean alt) {
        var intent = alt ? JoinIntent.SPECTATE : JoinIntent.PLAY;

        player.level().getServer().submit(() -> {
            var result = GamePlayerJoiner.tryJoin(player, this.gameSpace, intent);
            if (result.isError()) {
                player.sendSystemMessage(result.errorCopy().withStyle(ChatFormatting.RED));
            }
        });
    }

    @Override
    public int getPlayerCount() {
        return this.gameSpace.getState().players();
    }

    @Override
    public int getMaxPlayerCount() {
        return this.gameSpace.getState().maxPlayers();
    }

    @Override
    public int getSpectatorCount() {
        return this.gameSpace.getState().spectators();
    }

    @Nullable
    @Override
    public Component getState() {
        var state = this.gameSpace.getState().state();
        return state.hidden() ? null : state.display();
    }

    @Override
    public Action getAction() {
        return Action.PLAY;
    }

    @Override
    public boolean isGameEntry() {
        return true;
    }

    @Override
    public void provideGameSpaces(Consumer<GameSpace> consumer) {
        consumer.accept(this.gameSpace);
    }
}
