package xyz.nucleoid.plasmid.game.common.rust.network.message;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public final record SetBlock(BlockPos pos, Identifier block) implements RustGameMessage {
    public static final Codec<SetBlock> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(SetBlock::pos),
                Identifier.CODEC.fieldOf("block").forGetter(SetBlock::block)
        ).apply(instance, SetBlock::new);
    });

    @Override
    public Codec<? extends RustGameMessage> getCodec() {
        return CODEC;
    }
}
