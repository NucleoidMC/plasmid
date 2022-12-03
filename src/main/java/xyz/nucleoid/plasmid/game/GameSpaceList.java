package xyz.nucleoid.plasmid.game;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public interface GameSpaceList {
    Collection<? extends ListedGameSpace> getOpenGameSpaces();

    @Nullable
    ListedGameSpace byId(UUID id);

    @Nullable
    ListedGameSpace byUserId(Identifier userId);
}
