package xyz.nucleoid.plasmid.game.channel;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

public interface GameChannelSystem {
    Set<Identifier> keySet();

    Collection<GameChannel> getChannels();

    @Nullable
    GameChannel byId(Identifier id);
}
