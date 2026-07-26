package xyz.nucleoid.plasmid.impl.game.manager;

import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeLevelConfig;
import xyz.nucleoid.fantasy.RuntimeLevelHandle;
import xyz.nucleoid.fantasy.util.GameRuleStore;
import xyz.nucleoid.plasmid.api.game.level.GameSpaceLevels;

import java.util.Iterator;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;

public final class ManagedGameSpaceLevels implements GameSpaceLevels {
    private final ManagedGameSpace space;

    private final Map<ResourceKey<Level>, RuntimeLevelHandle> worlds = new Reference2ObjectOpenHashMap<>();

    ManagedGameSpaceLevels(ManagedGameSpace space) {
        this.space = space;
    }

    @Override
    public ServerLevel add(RuntimeLevelConfig levelConfig) {
        applyDefaultsTo(levelConfig);

        var worldHandle = Fantasy.get(this.space.getServer()).openTemporaryLevel(levelConfig);
        this.worlds.put(worldHandle.asLevel().dimension(), worldHandle);

        this.space.onAddLevel(worldHandle);

        return worldHandle.asLevel();
    }

    @Override
    public ServerLevel addPersistent(Identifier identifier, RuntimeLevelConfig worldConfig) {
        var world = this.space.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, identifier));
        if (world != null) {
            throw new RuntimeException("Level '" + identifier + "' is already loaded!");
        }

        applyDefaultsTo(worldConfig);

        var worldHandle = Fantasy.get(this.space.getServer()).getOrOpenPersistentLevel(identifier, worldConfig);
        this.worlds.put(worldHandle.asLevel().dimension(), worldHandle);
        this.space.onAddLevel(worldHandle);

        return worldHandle.asLevel();
    }

    @Override
    public boolean remove(ServerLevel level) {
        var dimension = level.dimension();
        var worldHandle = this.worlds.remove(dimension);
        if (worldHandle != null) {
            this.space.onRemoveLevel(dimension);
            worldHandle.unload();
            return true;
        } else {
            return false;
        }
    }

    void clear() {
        for (var worldHandler : this.worlds.values()) {
            worldHandler.unload();
        }
        this.worlds.clear();
    }

    @NotNull
    @Override
    public Iterator<ServerLevel> iterator() {
        return Iterators.transform(this.worlds.values().iterator(), RuntimeLevelHandle::asLevel);
    }

    private static void applyDefaultsTo(RuntimeLevelConfig worldConfig) {
        var rules = worldConfig.getGameRules();

        setDefaultRule(rules, GameRules.SHOW_ADVANCEMENT_MESSAGES, false);
        setDefaultRule(rules, GameRules.ADVANCE_TIME, false);
        setDefaultRule(rules, GameRules.ADVANCE_WEATHER, false);
        setDefaultRule(rules, GameRules.KEEP_INVENTORY, false);
        setDefaultRule(rules, GameRules.SPAWN_MOBS, false);
        setDefaultRule(rules, GameRules.LOCATOR_BAR, false);
    }

    private static <T> void setDefaultRule(GameRuleStore rules, GameRule<T> key, T value) {
        if (!rules.contains(key)) {
            rules.set(key, value);
        }
    }
}
