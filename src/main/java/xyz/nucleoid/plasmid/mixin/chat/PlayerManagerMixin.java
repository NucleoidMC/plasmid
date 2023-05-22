package xyz.nucleoid.plasmid.mixin.chat;

import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import java.util.List;
import java.util.function.Predicate;


@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Shadow @Final
    private List<ServerPlayerEntity> players;

    /**
     * if enabled, chat messages will only be sent to players in the same game space as the sender
     */
    @Redirect(
        method = "broadcast(Lnet/minecraft/network/message/SignedMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V",
        at = @At(value = "FIELD", target = "Lnet/minecraft/server/PlayerManager;players:Ljava/util/List;"))
    private List<ServerPlayerEntity> sendMessage(PlayerManager instance, SignedMessage message, Predicate<ServerPlayerEntity> shouldSendFiltered, @Nullable ServerPlayerEntity sender, MessageType.Parameters params)
    {
        if(sender == null) return this.players;
        var gameSpace = GameSpaceManager.get().byPlayer(sender);
        if(gameSpace == null) return GameSpaceManager.get().getPlayersNotInGame().stream().toList();
        return gameSpace.getPlayers().stream().toList();
    }
}
