package xyz.nucleoid.plasmid.world.bubble;

import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;

import javax.annotation.Nullable;

final class GameRuleStore {
    final Reference2BooleanMap<GameRules.Key<GameRules.BooleanRule>> booleanRules = new Reference2BooleanOpenHashMap<>();
    final Reference2IntMap<GameRules.Key<GameRules.IntRule>> intRules = new Reference2IntOpenHashMap<>();

    void set(GameRules.Key<GameRules.BooleanRule> key, boolean value) {
        this.booleanRules.put(key, value);
    }

    void set(GameRules.Key<GameRules.IntRule> key, int value) {
        this.intRules.put(key, value);
    }

    void applyTo(GameRules rules, @Nullable MinecraftServer server) {
        Reference2BooleanMaps.fastForEach(this.booleanRules, entry -> {
            GameRules.BooleanRule rule = rules.get(entry.getKey());
            rule.set(entry.getBooleanValue(), server);
        });

        Reference2IntMaps.fastForEach(this.intRules, entry -> {
            GameRules.IntRule rule = rules.get(entry.getKey());
            rule.value = entry.getIntValue();
        });
    }
}
