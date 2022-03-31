package xyz.nucleoid.plasmid.game.common.rust.network.message;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.codecs.MoreCodecs;

import java.util.UUID;

public final record PlayerDie(UUID player) implements RustGameMessage {
    public static final Codec<PlayerDie> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                MoreCodecs.UUID_STRING.fieldOf("player").forGetter(PlayerDie::player)
        ).apply(instance, PlayerDie::new);
    });

    @Override
    public Codec<? extends RustGameMessage> getCodec() {
        return CODEC;
    }
}
