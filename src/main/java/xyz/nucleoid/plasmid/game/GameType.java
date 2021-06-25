package xyz.nucleoid.plasmid.game;

import com.mojang.serialization.Codec;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

public final class GameType<C> {
    public static final TinyRegistry<GameType<?>> REGISTRY = TinyRegistry.create();

    private final Identifier identifier;
    private final Codec<C> configCodec;
    private final Open<C> open;

    private GameType(Identifier identifier, Codec<C> configCodec, Open<C> open) {
        this.identifier = identifier;
        this.configCodec = configCodec;
        this.open = open;
    }

    public static <C> GameType<C> register(Identifier identifier, Codec<C> configCodec, Open<C> open) {
        GameType<C> type = new GameType<>(identifier, configCodec, open);
        REGISTRY.register(identifier, type);
        return type;
    }

    public GameOpenProcedure open(GameOpenContext<C> context) {
        return this.open.open(context);
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }

    public Codec<C> getConfigCodec() {
        return this.configCodec;
    }

    public Text getName() {
        return new TranslatableText(this.getTranslationKey());
    }

    public String getTranslationKey() {
        return "game." + this.identifier.getNamespace() + "." + this.identifier.getPath();
    }

    @Nullable
    public static GameType<?> get(Identifier identifier) {
        return REGISTRY.get(identifier);
    }

    public interface Open<C> {
        GameOpenProcedure open(GameOpenContext<C> context);
    }
}
