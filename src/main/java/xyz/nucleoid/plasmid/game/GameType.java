package xyz.nucleoid.plasmid.game;

import com.mojang.serialization.Codec;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public final class GameType<C> {
    public static final TinyRegistry<GameType<?>> REGISTRY = TinyRegistry.newStable();

    private final Identifier identifier;
    private final Open<C> open;
    private final Codec<C> configCodec;

    private GameType(Identifier identifier, Open<C> open, Codec<C> configCodec) {
        this.identifier = identifier;
        this.open = open;
        this.configCodec = configCodec;
    }

    public static <C> GameType<C> register(Identifier identifier, Open<C> open, Codec<C> configCodec) {
        GameType<C> type = new GameType<>(identifier, open, configCodec);
        REGISTRY.register(identifier, type);
        return type;
    }

    public CompletableFuture<ManagedGameSpace> open(GameOpenContext<C> context) {
        return this.open.open(context);
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }

    public Codec<C> getConfigCodec() {
        return this.configCodec;
    }

    @Nullable
    public static GameType<?> get(Identifier identifier) {
        return REGISTRY.get(identifier);
    }

    @Override
    public int hashCode() {
        return this.identifier.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        if (obj instanceof GameType) {
            return ((GameType<?>) obj).identifier.equals(this.identifier);
        }

        return false;
    }

    public interface Open<C> {
        CompletableFuture<ManagedGameSpace> open(GameOpenContext<C> context);
    }
}
