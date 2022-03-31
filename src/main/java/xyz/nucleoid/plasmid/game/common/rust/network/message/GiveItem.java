package xyz.nucleoid.plasmid.game.common.rust.network.message;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import xyz.nucleoid.codecs.MoreCodecs;

import java.util.UUID;

public final record GiveItem(UUID player, Identifier item, int quantity) implements RustGameMessage {
    public static final Codec<GiveItem> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                MoreCodecs.UUID_STRING.fieldOf("player").forGetter(GiveItem::player),
                Identifier.CODEC.fieldOf("item").forGetter(GiveItem::item),
                Codec.INT.fieldOf("quantity").forGetter(GiveItem::quantity)
        ).apply(instance, GiveItem::new);
    });

    @Override
    public Codec<? extends RustGameMessage> getCodec() {
        return CODEC;
    }
}
