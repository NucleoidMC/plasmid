package xyz.nucleoid.plasmid.mixin.bubble;

import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.level.UnmodifiableLevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.world.bubble.BubbleLevelProperties;
import xyz.nucleoid.plasmid.world.bubble.BubbleWorldConfig;

@Mixin(UnmodifiableLevelProperties.class)
public abstract class UnmodifiableLevelPropertiesMixin implements BubbleLevelProperties {
    @Shadow
    public abstract GameRules getGameRules();

    private BubbleWorldConfig config;
    private GameRules bubbleRules;

    @Override
    public void apply(BubbleWorldConfig config) {
        this.config = config;

        this.bubbleRules = this.getGameRules().copy();

        if (config.hasTimeOfDay()) {
            this.bubbleRules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, null);
        }
    }

    @Override
    public void close() {
        this.config = null;
        this.bubbleRules = null;
    }

    @Inject(method = "getGameRules", at = @At("HEAD"), cancellable = true)
    private void getGameRules(CallbackInfoReturnable<GameRules> ci) {
        if (this.bubbleRules != null) {
            ci.setReturnValue(this.bubbleRules);
        }
    }

    @Inject(method = "getTimeOfDay", at = @At("HEAD"), cancellable = true)
    private void getTimeOfDay(CallbackInfoReturnable<Long> ci) {
        if (this.config != null && this.config.hasTimeOfDay()) {
            long timeOfDay = this.config.getTimeOfDay();
            ci.setReturnValue(timeOfDay);
        }
    }

    @Inject(method = "getDifficulty", at = @At("HEAD"), cancellable = true)
    private void getDifficulty(CallbackInfoReturnable<Difficulty> ci) {
        if (this.config != null) {
            Difficulty difficulty = this.config.getDifficulty();
            if (difficulty != null) {
                ci.setReturnValue(difficulty);
            }
        }
    }
}
