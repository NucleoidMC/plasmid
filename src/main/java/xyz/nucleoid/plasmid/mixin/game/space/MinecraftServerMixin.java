package xyz.nucleoid.plasmid.mixin.game.space;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.impl.game.manager.HasForcedGameSpace;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements HasForcedGameSpace {
    @Unique
    private GameSpace forcedGameSpace = null;

    @Override
    public GameSpace getForcedGameSpace() {
        return this.forcedGameSpace;
    }

    @Override
    public void setForcedGameSpace(GameSpace gameSpace) {
        this.forcedGameSpace = gameSpace;
    }
}
