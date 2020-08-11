package xyz.nucleoid.plasmid.game.world.bubble;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.plasmid.game.world.generator.VoidChunkGenerator;

public final class BubbleWorldConfig {
    private ChunkGenerator generator = VoidChunkGenerator.INSTANCE;
    private GameMode defaultGameMode = GameMode.ADVENTURE;
    private Vec3d spawnPos = new Vec3d(0.0, 64.0, 0.0);

    public BubbleWorldConfig setGenerator(ChunkGenerator generator) {
        this.generator = generator;
        return this;
    }

    public BubbleWorldConfig setDefaultGameMode(GameMode gameMode) {
        this.defaultGameMode = gameMode;
        return this;
    }

    public BubbleWorldConfig setSpawnPos(Vec3d spawnPos) {
        this.spawnPos = spawnPos;
        return this;
    }

    public ChunkGenerator getGenerator() {
        return this.generator;
    }

    public GameMode getDefaultGameMode() {
        return this.defaultGameMode;
    }

    public Vec3d getSpawnPos() {
        return this.spawnPos;
    }
}
