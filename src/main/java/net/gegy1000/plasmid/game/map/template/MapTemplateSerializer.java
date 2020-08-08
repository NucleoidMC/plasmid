package net.gegy1000.plasmid.game.map.template;

import com.google.common.base.Strings;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import net.gegy1000.plasmid.Plasmid;
import net.gegy1000.plasmid.util.BlockBounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.Registry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class MapTemplateSerializer {
    private static final Path MAP_ROOT = Paths.get(Plasmid.ID, "map");

    public static CompletableFuture<MapTemplate> load(Identifier identifier) {
        return CompletableFuture.supplyAsync(() -> {
            Path path = getPathFor(identifier).resolve("map.nbt");

            try (InputStream input = Files.newInputStream(path)) {
                MapTemplate template = MapTemplate.createEmpty();
                load(template, NbtIo.readCompressed(input));
                return template;
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, Util.method_27958());
    }

    private static void load(MapTemplate template, CompoundTag root) {
        ListTag chunkList = root.getList("chunks", 10);
        for (int i = 0; i < chunkList.size(); i++) {
            CompoundTag chunkRoot = chunkList.getCompound(i);

            int[] posArray = chunkRoot.getIntArray("pos");
            if (posArray.length != 3) {
                Plasmid.LOGGER.warn("Invalid chunk pos key: {}", posArray);
                continue;
            }

            BlockPos pos = new BlockPos(posArray[0], posArray[1], posArray[2]);
            MapTemplate.Chunk chunk = MapTemplate.Chunk.deserialize(chunkRoot);

            template.chunks.put(pos.asLong(), chunk);
        }

        ListTag regionList = root.getList("regions", 10);
        for (int i = 0; i < regionList.size(); i++) {
            CompoundTag regionRoot = regionList.getCompound(i);
            template.regions.add(TemplateRegion.deserialize(regionRoot));
        }

        ListTag blockEntityList = root.getList("block_entities", 10);
        for (int i = 0; i < blockEntityList.size(); i++) {
            CompoundTag blockEntity = blockEntityList.getCompound(i);
            BlockPos pos = new BlockPos(
                    blockEntity.getInt("x"),
                    blockEntity.getInt("y"),
                    blockEntity.getInt("z")
            );
            template.blockEntities.put(pos.asLong(), blockEntity);
        }

        template.bounds = BlockBounds.deserialize(root.getCompound("bounds"));

        String biomeId = root.getString("biome");
        if (!Strings.isNullOrEmpty(biomeId)) {
            Identifier biomeKey = new Identifier(biomeId);
            if (Registry.BIOME.containsId(biomeKey)) {
                template.biome = Registry.BIOME.get(biomeKey);
            }
        }
    }

    public static void save(MapTemplate template, Identifier identifier) throws IOException {
        CompoundTag root = new CompoundTag();

        ListTag chunkList = new ListTag();

        for (Long2ObjectMap.Entry<MapTemplate.Chunk> entry : Long2ObjectMaps.fastIterable(template.chunks)) {
            ChunkSectionPos pos = ChunkSectionPos.from(entry.getLongKey());
            MapTemplate.Chunk chunk = entry.getValue();

            CompoundTag chunkRoot = new CompoundTag();

            chunkRoot.putIntArray("pos", new int[] { pos.getX(), pos.getY(), pos.getZ() });
            chunk.serialize(chunkRoot);

            chunkList.add(chunkRoot);
        }

        root.put("chunks", chunkList);

        ListTag regionList = new ListTag();
        for (TemplateRegion region : template.regions) {
            regionList.add(region.serialize(new CompoundTag()));
        }
        root.put("regions", regionList);

        ListTag blockEntityList = new ListTag();
        blockEntityList.addAll(template.blockEntities.values());
        root.put("block_entities", blockEntityList);

        root.put("bounds", template.bounds.serialize(new CompoundTag()));

        if (template.biome != null) {
            Identifier biomeId = Registry.BIOME.getId(template.biome);
            root.putString("biome", biomeId.toString());
        }

        Path parent = getPathFor(identifier);
        Files.createDirectories(parent);

        Path path = parent.resolve("map.nbt");

        try (OutputStream output = Files.newOutputStream(path)) {
            NbtIo.writeCompressed(root, output);
        }
    }

    private static Path getPathFor(Identifier identifier) {
        return MAP_ROOT.resolve(identifier.getNamespace()).resolve(identifier.getPath());
    }
}
