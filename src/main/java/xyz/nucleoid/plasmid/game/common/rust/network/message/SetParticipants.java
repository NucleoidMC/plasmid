package xyz.nucleoid.plasmid.game.common.rust.network.message;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.codecs.MoreCodecs;

import java.util.List;
import java.util.UUID;

public final record SetParticipants(List<UUID> players) implements RustGameMessage {
    public static final Codec<SetParticipants> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                MoreCodecs.UUID_STRING.listOf().fieldOf("players").forGetter(SetParticipants::players)
        ).apply(instance, SetParticipants::new);
    });

    @Override
    public Codec<? extends RustGameMessage> getCodec() {
        return CODEC;
    }
}
