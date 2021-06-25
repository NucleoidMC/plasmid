package xyz.nucleoid.plasmid.game.player;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public final class PlayerOffer {
    private final ServerPlayerEntity player;

    public PlayerOffer(ServerPlayerEntity player) {
        this.player = player;
    }

    public ServerPlayerEntity getPlayer() {
        return this.player;
    }

    public PlayerOfferResult.Accept accept(ServerWorld world, Vec3d position) {
        return new PlayerOfferResult.Accept(world, position);
    }

    public PlayerOfferResult.Reject reject(Text reason) {
        return new PlayerOfferResult.Reject(reason);
    }

    public PlayerOfferResult pass() {
        return PlayerOfferResult.Pass.INSTANCE;
    }
}
