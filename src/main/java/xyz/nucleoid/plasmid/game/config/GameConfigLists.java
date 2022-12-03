package xyz.nucleoid.plasmid.game.config;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class GameConfigLists {
    private static final List<GameConfigList> REGISTRY = new ArrayList<>();
    private static GameConfigList composite = GameConfigList.EMPTY;

    private GameConfigLists() {
    }

    public static GameConfigList composite() {
        return composite;
    }

    public static void register(GameConfigList list) {
        REGISTRY.add(list);
        composite = buildCompositeList(List.copyOf(REGISTRY));
    }

    public static void unregister(GameConfigList list) {
        REGISTRY.remove(list);
        composite = buildCompositeList(List.copyOf(REGISTRY));
    }

    private static GameConfigList buildCompositeList(List<GameConfigList> registry) {
        return new GameConfigList() {
            @Nullable
            @Override
            public ListedGameConfig byKey(Identifier key) {
                for (var list : registry) {
                    var config = list.byKey(key);
                    if (config != null) {
                        return config;
                    }
                }
                return null;
            }

            @Override
            public Stream<Identifier> keys() {
                return registry.stream().flatMap(GameConfigList::keys);
            }
        };
    }
}
