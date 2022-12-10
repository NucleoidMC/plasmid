package xyz.nucleoid.plasmid.game;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface GameSpaceList {
    GameSpaceList EMPTY = new GameSpaceList() {
        @Override
        public Collection<? extends ListedGameSpace> getOpenGameSpaces() {
            return List.of();
        }

        @Override
        @Nullable
        public ListedGameSpace byId(UUID id) {
            return null;
        }

        @Override
        @Nullable
        public ListedGameSpace byUserId(Identifier userId) {
            return null;
        }
    };

    Collection<? extends ListedGameSpace> getOpenGameSpaces();

    @Nullable
    ListedGameSpace byId(UUID id);

    @Nullable
    ListedGameSpace byUserId(Identifier userId);
}
