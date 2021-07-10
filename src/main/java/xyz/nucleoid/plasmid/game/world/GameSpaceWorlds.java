package xyz.nucleoid.plasmid.game.world;

import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.util.*;

public final class GameSpaceWorlds implements Iterable<ServerWorld> {
    private final MinecraftServer server;

    private final Map<RegistryKey<World>, RuntimeWorldHandle> worlds = new Reference2ObjectOpenHashMap<>();

    public GameSpaceWorlds(MinecraftServer server) {
        this.server = server;
    }

    public RuntimeWorldHandle add(RuntimeWorldConfig worldConfig) {
        var worldHandle = Fantasy.get(this.server).openTemporaryWorld(worldConfig);
        this.worlds.put(worldHandle.asWorld().getRegistryKey(), worldHandle);
        return worldHandle;
    }

    public boolean remove(RegistryKey<World> dimension) {
        var worldHandle = this.worlds.remove(dimension);
        if (worldHandle != null) {
            worldHandle.delete();
            return true;
        } else {
            return false;
        }
    }

    public Collection<RegistryKey<World>> close() {
        var worldKeys = new ArrayList<RegistryKey<World>>(this.worlds.keySet());

        this.worlds.values().forEach(RuntimeWorldHandle::delete);
        this.worlds.clear();

        return worldKeys;
    }

    @NotNull
    @Override
    public Iterator<ServerWorld> iterator() {
        return Iterators.transform(this.worlds.values().iterator(), RuntimeWorldHandle::asWorld);
    }
}
