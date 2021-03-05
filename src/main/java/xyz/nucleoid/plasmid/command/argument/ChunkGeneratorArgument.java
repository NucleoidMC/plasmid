package xyz.nucleoid.plasmid.command.argument;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.Codec;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public final class ChunkGeneratorArgument {
    public static final DynamicCommandExceptionType GENERATOR_NOT_FOUND = new DynamicCommandExceptionType(arg ->
            new TranslatableText("text.plasmid.chunk_generator.generator_not_found", arg)
    );

    public static RequiredArgumentBuilder<ServerCommandSource, Identifier> argument(String name) {
        return CommandManager.argument(name, IdentifierArgumentType.identifier())
                .suggests((context, builder) -> {
                    return CommandSource.suggestIdentifiers(
                            Registry.CHUNK_GENERATOR.getIds().stream(),
                            builder
                    );
                });
    }

    public static Codec<? extends ChunkGenerator> get(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        Identifier identifier = IdentifierArgumentType.getIdentifier(context, name);

        Codec<? extends ChunkGenerator> generator = Registry.CHUNK_GENERATOR.get(identifier);
        if (generator == null) {
            throw GENERATOR_NOT_FOUND.create(identifier);
        }

        return generator;
    }
}
