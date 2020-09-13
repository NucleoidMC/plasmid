package xyz.nucleoid.plasmid.world.bubble;

import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.UnmodifiableLevelProperties;

public final class BubbleWorldProperties extends UnmodifiableLevelProperties {
    private final BubbleWorldConfig config;
    private final GameRules bubbleRules;

    public BubbleWorldProperties(SaveProperties saveProperties, BubbleWorldConfig config) {
        super(saveProperties, saveProperties.getMainWorldProperties());
        this.config = config;
        this.bubbleRules = this.createBubbleRules(config);
    }

    private GameRules createBubbleRules(BubbleWorldConfig config) {
        GameRules bubbleRules = super.getGameRules().copy();

        if (config.hasTimeOfDay()) {
            bubbleRules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, null);
        }

        GameRuleStore rules = config.getGameRules();
        rules.applyTo(bubbleRules, null);

        return bubbleRules;
    }

    @Override
    public GameRules getGameRules() {
        return this.bubbleRules;
    }

    @Override
    public long getTimeOfDay() {
        if (this.config.hasTimeOfDay()) {
            return this.config.getTimeOfDay();
        }
        return super.getTimeOfDay();
    }

    @Override
    public Difficulty getDifficulty() {
        Difficulty difficulty = this.config.getDifficulty();
        if (difficulty != null) {
            return difficulty;
        }
        return super.getDifficulty();
    }
}
