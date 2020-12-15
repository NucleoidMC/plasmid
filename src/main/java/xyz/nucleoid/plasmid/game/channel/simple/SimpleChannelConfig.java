package xyz.nucleoid.plasmid.game.channel.simple;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.channel.GameChannelBackend;
import xyz.nucleoid.plasmid.game.channel.GameChannelConfig;
import xyz.nucleoid.plasmid.game.channel.GameChannelMembers;

public final class SimpleChannelConfig implements GameChannelConfig {
    public static final Codec<SimpleChannelConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Identifier.CODEC.fieldOf("game_id").forGetter(channel -> channel.gameId)
        ).apply(instance, SimpleChannelConfig::new);
    });

    private final Identifier gameId;

    public SimpleChannelConfig(Identifier gameId) {
        this.gameId = gameId;
    }

    @Override
    public GameChannelBackend createBackend(GameChannelMembers members) {
        return new SimpleChannelBackend(this.gameId, members);
    }

    @Override
    public Codec<? extends GameChannelConfig> getCodec() {
        return CODEC;
    }
}
