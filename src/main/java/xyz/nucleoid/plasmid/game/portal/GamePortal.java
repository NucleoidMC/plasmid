package xyz.nucleoid.plasmid.game.portal;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.config.CustomValuesConfig;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import eu.pb4.sgui.api.elements.GuiElementInterface;

public final class GamePortal {
    private final MinecraftServer server;
    private final Identifier id;
    private final GamePortalBackend backend;
    private CustomValuesConfig custom = CustomValuesConfig.empty();

    private final Set<GamePortalInterface> interfaces = new ObjectOpenHashSet<>();

    private GamePortalDisplay lastDisplay = new GamePortalDisplay();
    private GamePortalDisplay currentDisplay = new GamePortalDisplay();

    public GamePortal(MinecraftServer server, Identifier id, GamePortalBackend.Factory backendFactory) {
        this.server = server;
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

    @Deprecated(forRemoval = true)
    @Nullable
    public GuiProvider getGuiProvider() {
        return this.backend.getGuiProvider();
    }

    public void requestJoin(ServerPlayerEntity player) {
        this.backend.applyTo(player);
    }

    public boolean addInterface(GamePortalInterface itf) {
        if (itf.getPortal() == null && this.interfaces.add(itf)) {
            itf.setPortal(this);
            itf.setDisplay(this.currentDisplay);
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

    @FunctionalInterface
    public interface GuiProvider {
        List<GuiElementInterface> getGuiElements();
    }
}
