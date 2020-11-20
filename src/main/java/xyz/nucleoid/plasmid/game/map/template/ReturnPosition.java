package xyz.nucleoid.plasmid.game.map.template;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public final class ReturnPosition {
    public final RegistryKey<World> dimension;
    public final Vec3d position;
    public final float yaw;
    public final float pitch;

    public ReturnPosition(RegistryKey<World> dimension, Vec3d position, float yaw, float pitch) {
        this.dimension = dimension;
        this.position = position;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static ReturnPosition capture(PlayerEntity player) {
        return new ReturnPosition(player.world.getRegistryKey(), player.getPos(), player.yaw, player.pitch);
    }

    public void applyTo(ServerPlayerEntity player) {
        ServerWorld world = player.getServer().getWorld(this.dimension);
        player.teleport(world, this.position.x, this.position.y, this.position.z, this.yaw, this.pitch);
    }

    public CompoundTag write(CompoundTag root) {
        root.putString("dimension", this.dimension.getValue().toString());
        root.putDouble("x", this.position.x);
        root.putDouble("y", this.position.y);
        root.putDouble("z", this.position.z);
        root.putFloat("yaw", this.yaw);
        root.putFloat("pitch", this.pitch);
        return root;
    }

    public static ReturnPosition read(CompoundTag root) {
        RegistryKey<World> dimension = RegistryKey.of(Registry.DIMENSION, new Identifier(root.getString("dimension")));
        double x = root.getDouble("x");
        double y = root.getDouble("y");
        double z = root.getDouble("z");
        float yaw = root.getFloat("yaw");
        float pitch = root.getFloat("pitch");
        return new ReturnPosition(dimension, new Vec3d(x, y, z), yaw, pitch);
    }
}
