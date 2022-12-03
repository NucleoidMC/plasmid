package xyz.nucleoid.plasmid.game.config;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public interface GameConfigList {
    GameConfigList EMPTY = new GameConfigList() {
        @Override
        @Nullable
        public ListedGameConfig byKey(Identifier key) {
            return null;
        }

        @Override
        public Stream<Identifier> keys() {
            return Stream.empty();
        }
    };

    @Nullable
    ListedGameConfig byKey(Identifier key);

    Stream<Identifier> keys();
}
