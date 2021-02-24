package xyz.nucleoid.plasmid.command.argument;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.GeneratorOptions;

public final class DimensionOptionsArgument {
    public static final DynamicCommandExceptionType DIMENSION_NOT_FOUND = new DynamicCommandExceptionType(arg ->
            new TranslatableText("text.plasmid.dimension_options.dimension_not_found", arg)
    );

    public static RequiredArgumentBuilder<ServerCommandSource, Identifier> argument(String name) {
        return CommandManager.argument(name, IdentifierArgumentType.identifier())
                .suggests((context, builder) -> {
                    ServerCommandSource source = context.getSource();
                    GeneratorOptions options = source.getMinecraftServer().getSaveProperties().getGeneratorOptions();
                    SimpleRegistry<DimensionOptions> dimensions = options.getDimensions();

                    return CommandSource.suggestIdentifiers(
                            dimensions.getIds().stream(),
                            builder
                    );
                });
    }

    public static DimensionOptions get(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        Identifier identifier = IdentifierArgumentType.getIdentifier(context, name);

        ServerCommandSource source = context.getSource();
        GeneratorOptions options = source.getMinecraftServer().getSaveProperties().getGeneratorOptions();
        SimpleRegistry<DimensionOptions> dimensions = options.getDimensions();

        DimensionOptions dimension = dimensions.get(identifier);
        if (dimension == null) {
            throw DIMENSION_NOT_FOUND.create(identifier);
        }

        return dimension;
    }
}
