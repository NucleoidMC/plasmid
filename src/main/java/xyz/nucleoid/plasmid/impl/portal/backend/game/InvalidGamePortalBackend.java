package xyz.nucleoid.plasmid.impl.portal.backend.game;

import com.mojang.serialization.MapCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.api.game.config.CustomValuesConfig;
import xyz.nucleoid.plasmid.impl.portal.backend.GamePortalBackend;
import xyz.nucleoid.plasmid.impl.portal.config.GamePortalConfig;

public record InvalidGamePortalBackend(Identifier identifier) implements GamePortalBackend {
    public static final GamePortalConfig CONFIG = new GamePortalConfig() {
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
    public Text getName() {
        return Text.literal("Invalid portal'" + this.identifier + "'");
    }

    @Override
    public void applyTo(ServerPlayerEntity player, boolean alt) {

    }
}
