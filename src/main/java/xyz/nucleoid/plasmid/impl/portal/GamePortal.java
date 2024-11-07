package xyz.nucleoid.plasmid.impl.portal;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.config.CustomValuesConfig;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public final class GamePortal {
    private final Identifier id;
    private final GamePortalBackend backend;
    private CustomValuesConfig custom = CustomValuesConfig.empty();

    private final Set<GamePortalInterface> interfaces = new ObjectOpenHashSet<>();

    private GamePortalDisplay lastDisplay = new GamePortalDisplay();
    private GamePortalDisplay currentDisplay = new GamePortalDisplay();

    public GamePortal(MinecraftServer server, Identifier id, GamePortalBackend.Factory backendFactory) {
        this.id = id;
        this.backend = backendFactory.create(server, id);
    }

    void setCustom(CustomValuesConfig custom) {
        this.custom = custom;
    }

    public Identifier getId() {
        return this.id;
    }

    public CustomValuesConfig getCustom() {
        return this.custom;
    }

    public Text getName() {
        return this.backend.getName();
    }

    public List<Text> getDescription() {
        return this.backend.getDescription();
    }

    public ItemStack getIcon() {
        return this.backend.getIcon();
    }

    public int getPlayerCount() {
        return this.backend.getPlayerCount();
    }

    public int getSpectatorCount() {
        return this.backend.getSpectatorCount();
    }

    public int getMaxPlayerCount() {
        return this.backend.getMaxPlayerCount();
    }

    public void requestJoin(ServerPlayerEntity player, boolean alt) {
        this.backend.applyTo(player, alt);
    }

    public boolean addInterface(GamePortalInterface itf) {
        if (itf.getPortal() == null && this.interfaces.add(itf)) {
            itf.setPortal(this);
            if (itf.updatePortalImmediately()) {
                itf.setDisplay(this.currentDisplay);
            }
            return true;
        }
        return false;
    }

    public boolean removeInterface(GamePortalInterface itf) {
        if (this.interfaces.remove(itf)) {
            itf.invalidatePortal();
            return true;
        }
        return false;
    }

    public void invalidate() {
        for (var itf : this.interfaces) {
            itf.invalidatePortal();
        }
        this.interfaces.clear();
    }

    void updateDisplay() {
        this.flipDisplay();

        var display = this.currentDisplay;
        this.backend.populateDisplay(display);

        if (!display.equals(this.lastDisplay)) {
            for (var itf : this.interfaces) {
                itf.setDisplay(display);
            }
        }
    }

    void flipDisplay() {
        var swap = this.currentDisplay;
        this.currentDisplay = this.lastDisplay;
        this.lastDisplay = swap;

        this.currentDisplay.clear();
    }

    public void provideGameSpaces(Consumer<GameSpace> consumer) {
        this.backend.provideGameSpaces(consumer);
    }

    public GamePortalBackend getBackend() {
        return this.backend;
    }

    @FunctionalInterface
    public interface GuiProvider {
        List<GuiElementInterface> getGuiElements();
    }
}
