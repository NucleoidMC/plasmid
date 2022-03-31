package xyz.nucleoid.plasmid.game.common.rust.network.message;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.Vec3f;
import xyz.nucleoid.codecs.MoreCodecs;

import java.util.UUID;

public final record TeleportPlayer(UUID player, Vec3f dest) implements RustGameMessage {
    public static final Codec<TeleportPlayer> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                MoreCodecs.UUID_STRING.fieldOf("player").forGetter(TeleportPlayer::player),
                Vec3f.CODEC.fieldOf("dest").forGetter(TeleportPlayer::dest)
        ).apply(instance, TeleportPlayer::new);
    });

    @Override
    public Codec<? extends RustGameMessage> getCodec() {
        return CODEC;
    }
}
