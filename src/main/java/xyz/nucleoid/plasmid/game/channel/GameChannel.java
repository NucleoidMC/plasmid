package xyz.nucleoid.plasmid.game.channel;

import com.mojang.serialization.Codec;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

import java.util.function.Function;

public interface GameChannel {
    TinyRegistry<Codec<? extends GameChannel>> REGISTRY = TinyRegistry.newStable();
    Codec<GameChannel> CODEC = REGISTRY.dispatchStable(GameChannel::getCodec, Function.identity());

    static void register(Identifier key, Codec<? extends GameChannel> codec) {
        REGISTRY.register(key, codec);
    }

    Identifier getId();

    void requestJoin(ServerPlayerEntity player);

    boolean connectTo(ChannelEndpoint endpoint);

    boolean removeConnection(ChannelEndpoint endpoint);

    void invalidate();

    GameChannelDisplay display();

    Codec<? extends GameChannel> getCodec();
}
