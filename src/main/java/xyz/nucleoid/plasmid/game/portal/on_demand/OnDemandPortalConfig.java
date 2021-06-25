package xyz.nucleoid.plasmid.game.portal.on_demand;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.portal.GamePortalBackend;
import xyz.nucleoid.plasmid.game.portal.GamePortalConfig;
import xyz.nucleoid.plasmid.game.config.CustomValuesConfig;

public final class OnDemandPortalConfig implements GamePortalConfig {
    public static final Codec<OnDemandPortalConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Identifier.CODEC.fieldOf("game").forGetter(c -> c.gameId),
                CustomValuesConfig.CODEC.optionalFieldOf("custom", CustomValuesConfig.empty()).forGetter(c -> c.custom)
        ).apply(instance, OnDemandPortalConfig::new);
    });

    private final Identifier gameId;
    private final CustomValuesConfig custom;

    public OnDemandPortalConfig(Identifier gameId, CustomValuesConfig custom) {
        this.gameId = gameId;
        this.custom = custom;
    }

    @Override
    public GamePortalBackend createBackend(MinecraftServer server, Identifier id) {
        return new OnDemandPortalBackend(this.gameId);
    }

    @Override
    public CustomValuesConfig getCustom() {
        return this.custom;
    }

    @Override
    public Codec<? extends GamePortalConfig> getCodec() {
        return CODEC;
    }
}
