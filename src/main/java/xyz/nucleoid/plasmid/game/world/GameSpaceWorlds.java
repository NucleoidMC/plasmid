package xyz.nucleoid.plasmid.game.world;

import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;
import xyz.nucleoid.fantasy.util.GameRuleStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public final class GameSpaceWorlds implements Iterable<ServerWorld> {
    private final MinecraftServer server;

    private final Map<RegistryKey<World>, RuntimeWorldHandle> worlds = new Reference2ObjectOpenHashMap<>();

    public GameSpaceWorlds(MinecraftServer server) {
        this.server = server;
    }

    public RuntimeWorldHandle add(RuntimeWorldConfig worldConfig) {
        applyDefaultsTo(worldConfig);

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

    private static void applyDefaultsTo(RuntimeWorldConfig worldConfig) {
        var rules = worldConfig.getGameRules();

        setDefaultRule(rules, GameRules.ANNOUNCE_ADVANCEMENTS, false);
        setDefaultRule(rules, GameRules.DO_DAYLIGHT_CYCLE, false);
        setDefaultRule(rules, GameRules.DO_WEATHER_CYCLE, false);
        setDefaultRule(rules, GameRules.KEEP_INVENTORY, false);
        setDefaultRule(rules, GameRules.DO_MOB_SPAWNING, false);
    }

    private static void setDefaultRule(GameRuleStore rules, GameRules.Key<GameRules.BooleanRule> key, boolean value) {
        if (!rules.contains(key)) {
            rules.set(key, value);
        }
    }
}
