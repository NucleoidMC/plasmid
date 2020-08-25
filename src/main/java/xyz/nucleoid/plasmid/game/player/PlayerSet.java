package xyz.nucleoid.plasmid.game.player;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.util.TranslatableLiteralText;

import java.util.Iterator;
import java.util.Set;

public final class PlayerSet implements Iterable<ServerPlayerEntity> {
    private final Set<ServerPlayerEntity> players = new ObjectOpenHashSet<>();
    private final Set<Listener> listeners = new ReferenceOpenHashSet<>();

    public boolean add(ServerPlayerEntity player) {
        if (this.players.add(player)) {
            for (Listener listener : this.listeners) {
                listener.onAddPlayer(player);
            }
            return true;
        }
        return false;
    }

    public boolean remove(ServerPlayerEntity player) {
        if (this.players.remove(player)) {
            for (Listener listener : this.listeners) {
                listener.onRemovePlayer(player);
            }
            return true;
        }
        return false;
    }

    public boolean contains(ServerPlayerEntity player) {
        return this.players.contains(player);
    }

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }

    public void sendPacket(Packet<?> packet) {
        for (ServerPlayerEntity player : this.players) {
            player.networkHandler.sendPacket(packet);
        }
    }

    public void sendMessage(Text message) {
        for (ServerPlayerEntity player : this.players) {
            if (message instanceof TranslatableLiteralText) message = ((TranslatableLiteralText) message).getText(player);
            player.sendMessage(message, false);
        }
    }

    public void sendTitle(Text message) {
        this.sendTitle(message, 1, 5, 3);
    }

    public void sendTitle(Text message, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        this.sendPacket(new TitleS2CPacket(fadeInTicks, stayTicks, fadeOutTicks));
        this.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.TITLE, message));
    }

    public void sendSound(SoundEvent sound) {
        this.sendSound(sound, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    public void sendSound(SoundEvent sound, SoundCategory category, float volume, float pitch) {
        for (ServerPlayerEntity player : this.players) {
            player.playSound(sound, category, volume, pitch);
        }
    }

    @Override
    public Iterator<ServerPlayerEntity> iterator() {
        return this.players.iterator();
    }

    public int size() {
        return this.players.size();
    }

    public PlayerSet copy() {
        PlayerSet copy = new PlayerSet();
        copy.players.addAll(this.players);
        return copy;
    }

    public interface Listener {
        default void onAddPlayer(ServerPlayerEntity player) {
        }

        default void onRemovePlayer(ServerPlayerEntity player) {
        }
    }
}
