package xyz.nucleoid.plasmid.impl.portal.game;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xyz.nucleoid.plasmid.api.game.config.CustomValuesConfig;
import xyz.nucleoid.plasmid.impl.portal.GamePortalBackend;
import xyz.nucleoid.plasmid.impl.portal.GamePortalConfig;

public record InvalidGamePortalBackend(Identifier identifier) implements GamePortalBackend {
    public static final GamePortalConfig CONFIG = new GamePortalConfig() {
        @Override
        public GamePortalBackend createBackend(MinecraftServer server, Identifier id) {
            return new InvalidGamePortalBackend(id);
        }

        @Override
        public CustomValuesConfig custom() {
            return CustomValuesConfig.empty();
        }

        @Override
        public MapCodec<? extends GamePortalConfig> codec() {
            return MapCodec.unit(this);
        }
    };

    @Override
    public Component getName() {
        return Component.literal("Invalid portal'" + this.identifier + "'");
    }

    @Override
    public void applyTo(ServerPlayer player, boolean alt) {

    }
}
