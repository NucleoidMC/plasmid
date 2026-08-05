package xyz.nucleoid.plasmid.mixin.game.menu;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Brightness;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.api.menu.GameMenuEntryConfig;
import xyz.nucleoid.plasmid.api.menu.GameMenuTheme;
import xyz.nucleoid.plasmid.impl.compatibility.DisguiseLibCompatibility;
import xyz.nucleoid.plasmid.impl.menu.anchor.GameMenuAnchor;
import xyz.nucleoid.plasmid.impl.menu.anchor.GameMenuAnchors;
import xyz.nucleoid.plasmid.impl.menu.anchor.GameMenuDisplay;

@Mixin(Entity.class)
public abstract class EntityMixin implements GameMenuAnchor {
    @Shadow
    public Level level;

    @Shadow
    public abstract Vec3 position();

    @Unique
    private TextDisplayElement textDisplayElement;
    @Unique
    private ElementHolder hologram;
    @Unique
    private Holder<GameMenuEntryConfig> anchoredEntry;
    @Unique
    private Holder<GameMenuTheme> anchoredTheme;
    @Unique
    private GameMenuDisplay lastDisplay;
    @Unique
    private boolean anchorLoaded;

    @Override
    public boolean interactWithAnchor(ServerPlayer player) {
        if (this.anchoredEntry != null) {
            this.onInteract(player, false);
            return true;
        }
        return false;
    }

    @Override
    public void setAnchoredEntry(@Nullable Holder<GameMenuEntryConfig> entry) {
        this.anchoredEntry = entry;
        if (entry == null) {
            this.removeHologram();
        }
    }

    @Nullable
    @Override
    public Holder<GameMenuEntryConfig> getAnchoredEntry() {
        return this.anchoredEntry;
    }

    @Override
    public void setAnchoredTheme(@Nullable Holder<GameMenuTheme> theme) {
        this.anchoredTheme = theme;
    }

    @Nullable
    @Override
    public Holder<GameMenuTheme> getAnchoredTheme() {
        return this.anchoredTheme;
    }

    @Nullable
    @Override
    public GameMenuDisplay getLastDisplay() {
        return this.lastDisplay;
    }

    @Override
    public void setLastDisplay(@Nullable GameMenuDisplay display) {
        this.lastDisplay = display;
    }

    @Override
    public void setDisplay(GameMenuDisplay display) {
        var hologram = this.getOrCreateTextElement();

        var text = Component.empty();

        var name = display.get(GameMenuDisplay.NAME);
        var playerCount = display.get(GameMenuDisplay.PLAYER_COUNT);
        if (name != null && playerCount != null) {
            text.append(name);
            if (playerCount > -1) {
                text.append("\n").append(Component.translatable("text.plasmid.game.menu.player_count", playerCount));
            }
        }
        hologram.setText(text);

        if (hologram.isDirty()) {
            hologram.tick();
        }
    }

    @Unique
    private TextDisplayElement getOrCreateTextElement() {
        if (this.hologram != null) {
            return textDisplayElement;
        }

        var entity = (Entity) (Object) this;
        var offset = new Vec3(0.0, DisguiseLibCompatibility.getEntityHeight(entity) + 0.2, 0.0);

        this.hologram = new ElementHolder();
        this.textDisplayElement = new TextDisplayElement();
        this.textDisplayElement.setOffset(offset);
        this.textDisplayElement.setBrightness(new Brightness(15, 15));
        this.textDisplayElement.setBillboardMode(Display.BillboardConstraints.CENTER);
        this.textDisplayElement.setDisplayWidth(5);
        this.textDisplayElement.setDisplayHeight(1);
        this.textDisplayElement.setViewRange(0.5f);
        this.hologram.addElement(this.textDisplayElement);

        EntityAttachment.of(this.hologram, (Entity) (Object) this);

        return this.textDisplayElement;
    }

    private void removeHologram() {
        var hologram = this.hologram;
        this.hologram = null;

        if (hologram != null) {
            hologram.destroy();
            this.textDisplayElement = null;
        }
    }

    @Inject(method = "saveWithoutId", at = @At("RETURN"))
    private void writeAnchor(ValueOutput view, CallbackInfo ci) {
        this.serializeAnchor(view);
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void readAnchor(ValueInput view, CallbackInfo ci) {
        this.deserializeAnchor(view);
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void remove(CallbackInfo ci) {
        GameMenuAnchors.unbind(this);
        this.removeHologram();
    }
}
