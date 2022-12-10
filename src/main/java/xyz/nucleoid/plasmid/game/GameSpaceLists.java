package xyz.nucleoid.plasmid.game;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class GameSpaceLists {
    private static final List<GameSpaceList> REGISTRY = new ArrayList<>();
    private static GameSpaceList composite = GameSpaceList.EMPTY;

    private GameSpaceLists() {
    }

    public static GameSpaceList composite() {
        return composite;
    }

    public static void register(GameSpaceList list) {
        REGISTRY.add(list);
        composite = buildCompositeList(List.copyOf(REGISTRY));
    }

    public static void unregister(GameSpaceList list) {
        REGISTRY.remove(list);
        composite = buildCompositeList(List.copyOf(REGISTRY));
    }

    private static GameSpaceList buildCompositeList(List<GameSpaceList> registry) {
        return new GameSpaceList() {
            @Override
            public Collection<? extends ListedGameSpace> getOpenGameSpaces() {
                var result = new ArrayList<ListedGameSpace>();
                for (var list : registry) {
                    result.addAll(list.getOpenGameSpaces());
                }
                return result;
            }

            @Override
            @Nullable
            public ListedGameSpace byId(UUID id) {
                for (var list : registry) {
                    var gameSpace = list.byId(id);
                    if (gameSpace != null) {
                        return gameSpace;
                    }
                }
                return null;
            }

            @Override
            @Nullable
            public ListedGameSpace byUserId(Identifier userId) {
                for (var list : registry) {
                    var gameSpace = list.byUserId(userId);
                    if (gameSpace != null) {
                        return gameSpace;
                    }
                }
                return null;
            }
        };
    }
}
