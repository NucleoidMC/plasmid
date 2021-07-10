package xyz.nucleoid.plasmid.map.template;

import com.google.common.base.Strings;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class MapTemplateSerializer {
    public static final MapTemplateSerializer INSTANCE = new MapTemplateSerializer();

    private static final Path EXPORT_ROOT = Paths.get(Plasmid.ID, "export");

    private ResourceManager resourceManager;

    private MapTemplateSerializer() {
    }

    public void register() {
        ResourceManagerHelper serverData = ResourceManagerHelper.get(ResourceType.SERVER_DATA);

        serverData.registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier(Plasmid.ID, "map_templates");
            }

            @Override
            public void reload(ResourceManager manager) {
                MapTemplateSerializer.this.resourceManager = manager;
            }
        });
    }

    public MapTemplate loadFromResource(Identifier identifier) throws IOException {
        Identifier path = getResourcePathFor(identifier);

        try (Resource resource = this.resourceManager.getResource(path)) {
            return this.loadFrom(resource.getInputStream());
        }
    }

    public MapTemplate loadFromExport(Identifier location) throws IOException {
        Path path = getExportPathFor(location);
        if (!Files.exists(path)) {
            throw new IOException("Export does not exist for " + location + "!");
        }

        try (InputStream input = Files.newInputStream(path)) {
            return this.loadFrom(input);
        }
    }

    public CompletableFuture<Void> saveToExport(MapTemplate template, Identifier identifier) {
        return CompletableFuture.supplyAsync(() -> {
            Path path = getExportPathFor(identifier);
            try {
                Files.createDirectories(path.getParent());
                try (OutputStream output = Files.newOutputStream(path)) {
                    this.saveTo(template, output);
                    return null;
                }
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, Util.getIoWorkerExecutor());
    }

    public MapTemplate loadFrom(InputStream input) throws IOException {
        MapTemplate template = MapTemplate.createEmpty();
        this.load(template, NbtIo.readCompressed(input));
        return template;
    }

    public void saveTo(MapTemplate template, OutputStream output) throws IOException {
        NbtCompound root = this.save(template);
        NbtIo.writeCompressed(root, output);
    }

    private void load(MapTemplate template, NbtCompound root) {
        NbtList chunkList = root.getList("chunks", NbtType.COMPOUND);
        for (int i = 0; i < chunkList.size(); i++) {
            NbtCompound chunkRoot = chunkList.getCompound(i);

            int[] posArray = chunkRoot.getIntArray("pos");
            if (posArray.length != 3) {
                Plasmid.LOGGER.warn("Invalid chunk pos key: {}", posArray);
                continue;
            }

            long pos = MapTemplate.chunkPos(posArray[0], posArray[1], posArray[2]);
            MapChunk chunk = MapChunk.deserialize(ChunkSectionPos.from(pos), chunkRoot);

            template.chunks.put(pos, chunk);
        }

        MapTemplateMetadata metadata = template.metadata;

        NbtList regionList = root.getList("regions", NbtType.COMPOUND);
        for (int i = 0; i < regionList.size(); i++) {
            NbtCompound regionRoot = regionList.getCompound(i);
            metadata.regions.add(TemplateRegion.deserialize(regionRoot));
        }

        NbtList blockEntityList = root.getList("block_entities", NbtType.COMPOUND);
        for (int i = 0; i < blockEntityList.size(); i++) {
            NbtCompound blockEntity = blockEntityList.getCompound(i);
            BlockPos pos = new BlockPos(
                    blockEntity.getInt("x"),
                    blockEntity.getInt("y"),
                    blockEntity.getInt("z")
            );
            template.blockEntities.put(pos.asLong(), blockEntity);
        }

        template.bounds = BlockBounds.deserialize(root.getCompound("bounds"));
        metadata.data = root.getCompound("data");

        String biomeId = root.getString("biome");
        if (!Strings.isNullOrEmpty(biomeId)) {
            template.biome = RegistryKey.of(Registry.BIOME_KEY, new Identifier(biomeId));
        }
    }

    private NbtCompound save(MapTemplate template) {
        NbtCompound root = new NbtCompound();

        NbtList chunkList = new NbtList();

        for (Long2ObjectMap.Entry<MapChunk> entry : Long2ObjectMaps.fastIterable(template.chunks)) {
            ChunkSectionPos pos = ChunkSectionPos.from(entry.getLongKey());
            MapChunk chunk = entry.getValue();

            NbtCompound chunkRoot = new NbtCompound();

            chunkRoot.putIntArray("pos", new int[] { pos.getX(), pos.getY(), pos.getZ() });
            chunk.serialize(chunkRoot);

            chunkList.add(chunkRoot);
        }

        root.put("chunks", chunkList);

        NbtList blockEntityList = new NbtList();
        blockEntityList.addAll(template.blockEntities.values());
        root.put("block_entities", blockEntityList);

        root.put("bounds", template.bounds.serialize(new NbtCompound()));

        if (template.biome != null) {
            root.putString("biome", template.biome.getValue().toString());
        }

        MapTemplateMetadata metadata = template.metadata;

        NbtList regionList = new NbtList();
        for (TemplateRegion region : metadata.regions) {
            regionList.add(region.serialize(new NbtCompound()));
        }
        root.put("regions", regionList);

        root.put("data", metadata.data);

        return root;
    }

    private static Identifier getResourcePathFor(Identifier identifier) {
        return new Identifier(identifier.getNamespace(), "map_templates/" + identifier.getPath() + ".nbt");
    }

    private static Path getExportPathFor(Identifier identifier) {
        identifier = getResourcePathFor(identifier);
        return EXPORT_ROOT.resolve(identifier.getNamespace()).resolve(identifier.getPath());
    }
}
