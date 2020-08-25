package xyz.nucleoid.plasmid.game.map.template;

import com.google.common.base.Strings;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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
            public void apply(ResourceManager manager) {
                MapTemplateSerializer.this.resourceManager = manager;
            }
        });
    }

    public CompletableFuture<MapTemplate> load(Identifier identifier) {
        return CompletableFuture.supplyAsync(() -> {
            Identifier path = getResourcePathFor(identifier);

            try (Resource resource = this.resourceManager.getResource(path)) {
                MapTemplate template = MapTemplate.createEmpty();
                this.load(template, NbtIo.readCompressed(resource.getInputStream()));
                return template;
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, Util.getIoWorkerExecutor());
    }

    public CompletableFuture<Void> save(MapTemplate template, Identifier identifier) {
        return CompletableFuture.supplyAsync(() -> {
            Path path = getExportPathFor(identifier);
            try {
                Files.createDirectories(path.getParent());
                try (OutputStream output = Files.newOutputStream(path)) {
                    CompoundTag root = this.save(template);
                    NbtIo.writeCompressed(root, output);
                    return null;
                }
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, Util.getIoWorkerExecutor());
    }

    private void load(MapTemplate template, CompoundTag root) {
        ListTag chunkList = root.getList("chunks", NbtType.COMPOUND);
        for (int i = 0; i < chunkList.size(); i++) {
            CompoundTag chunkRoot = chunkList.getCompound(i);

            int[] posArray = chunkRoot.getIntArray("pos");
            if (posArray.length != 3) {
                Plasmid.LOGGER.warn("Invalid chunk pos key: {}", posArray);
                continue;
            }

            long pos = ChunkSectionPos.asLong(posArray[0], posArray[1], posArray[2]);
            MapTemplate.Chunk chunk = MapTemplate.Chunk.deserialize(chunkRoot);

            template.chunks.put(pos, chunk);
        }

        ListTag regionList = root.getList("regions", NbtType.COMPOUND);
        for (int i = 0; i < regionList.size(); i++) {
            CompoundTag regionRoot = regionList.getCompound(i);
            template.regions.add(TemplateRegion.deserialize(regionRoot));
        }

        ListTag blockEntityList = root.getList("block_entities", NbtType.COMPOUND);
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
            template.biome = RegistryKey.of(Registry.BIOME_KEY, new Identifier(biomeId));
        }
    }

    private CompoundTag save(MapTemplate template) {
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
            root.putString("biome", template.biome.getValue().toString());
        }

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
