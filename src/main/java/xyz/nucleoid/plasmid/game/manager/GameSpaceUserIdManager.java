package xyz.nucleoid.plasmid.game.manager;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.RandomStringUtils;
import xyz.nucleoid.plasmid.game.GameType;
import xyz.nucleoid.plasmid.game.config.GameConfig;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

final class GameSpaceUserIdManager {
    private final Multimap<GameType<?>, Identifier> typeToIds = HashMultimap.create();
    private final Map<Identifier, GameType<?>> idToType = new Object2ObjectOpenHashMap<>();

    public Identifier acquire(GameConfig<?> config) {
        GameType<?> type = config.type();
        var ids = this.typeToIds.get(type);

        var uniqueId = this.generateUniqueId(type, ids);
        ids.add(uniqueId);

        this.idToType.put(uniqueId, type);

        return uniqueId;
    }

    public void release(Identifier id) {
        var config = this.idToType.remove(id);
        if (config != null) {
            this.typeToIds.remove(config, id);
        }
    }

    private Identifier generateUniqueId(GameType<?> type, Collection<Identifier> ids) {
        var typeId = this.getIdForType(type);
        if (ids.isEmpty()) {
            return typeId;
        }

        Identifier uniqueId;
        do {
            uniqueId = this.generateRandomId(typeId);
        } while (ids.contains(uniqueId));

        return uniqueId;
    }

    private Identifier generateRandomId(Identifier typeId) {
        var random = RandomStringUtils.randomAlphabetic(4).toLowerCase(Locale.ROOT);
        return new Identifier(typeId.getNamespace(), typeId.getPath() + "/" + random);
    }

    private Identifier getIdForType(GameType<?> type) {
        return type.id();
    }
}
