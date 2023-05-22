package xyz.nucleoid.plasmid.game.manager;

import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;
import xyz.nucleoid.fantasy.util.GameRuleStore;
import xyz.nucleoid.plasmid.game.world.GameSpaceWorlds;

import java.util.Iterator;
import java.util.Map;

public final class ManagedGameSpaceWorlds implements GameSpaceWorlds {
    private final ManagedGameSpace space;

    private final Map<RegistryKey<World>, RuntimeWorldHandle> worlds = new Reference2ObjectOpenHashMap<>();

    ManagedGameSpaceWorlds(ManagedGameSpace space) {
        this.space = space;
    }

    @Override
    public ServerWorld add(RuntimeWorldConfig worldConfig) {
        applyDefaultsTo(worldConfig);

        var worldHandle = Fantasy.get(this.space.getServer()).openTemporaryWorld(worldConfig);
        this.worlds.put(worldHandle.asWorld().getRegistryKey(), worldHandle);

        this.space.onAddWorld(worldHandle);

        return worldHandle.asWorld();
    }

    @Override
    public boolean remove(ServerWorld world) {
        var dimension = world.getRegistryKey();
        var worldHandle = this.worlds.remove(dimension);
        if (worldHandle != null) {
            this.space.onRemoveWorld(dimension);
            worldHandle.delete();
            return true;
        } else {
            return false;
        }
    }

    void clear() {
        for (var worldHandler : this.worlds.values()) {
            worldHandler.delete();
        }
        this.worlds.clear();
    }

    @NotNull
    @Override
    public Iterator<ServerWorld> iterator() {
        return Iterators.transform(this.worlds.values().iterator(), RuntimeWorldHandle::asWorld);
    }

    @Override
    public boolean contains(RegistryKey<World> dimension) {
        return this.worlds.containsKey(dimension);
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
