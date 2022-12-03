package xyz.nucleoid.plasmid.game.config;

import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public interface GameConfigList {
    @Nullable
    ListedGameConfig byKey(Identifier key);

    Stream<Identifier> keys();
}
