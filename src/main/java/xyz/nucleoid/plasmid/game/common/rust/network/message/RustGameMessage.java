package xyz.nucleoid.plasmid.game.common.rust.network.message;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

import java.util.Optional;
import java.util.function.Function;

public interface RustGameMessage {
    TinyRegistry<Codec<? extends RustGameMessage>> REGISTRY = TinyRegistry.create();
    Codec<? extends RustGameMessage> CODEC = REGISTRY.dispatchStable(RustGameMessage::getCodec, Function.identity());

    static void register() {
        REGISTRY.register(new Identifier("plasmid", "participants"), SetParticipants.CODEC);
        REGISTRY.register(new Identifier("plasmid", "teleport"), TeleportPlayer.CODEC);
        REGISTRY.register(new Identifier("plasmid", "give_item"), GiveItem.CODEC);
        REGISTRY.register(new Identifier("plasmid", "set_block"), SetBlock.CODEC);
    }

    Codec<? extends RustGameMessage> getCodec();

    @SuppressWarnings("unchecked")
    default <M extends RustGameMessage, T> Optional<T> encode(DynamicOps<T> ops) {
        final Codec<M> codec = (Codec) this.getCodec();
        return codec.encodeStart(ops, (M) this).result();
    }
}
