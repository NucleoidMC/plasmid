package xyz.nucleoid.plasmid.api.map.template.processor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.context.ContextParameterMap;
import xyz.nucleoid.map_templates.MapTemplate;

import java.util.Map;

/**
 * Template processor that replaces block entity NBT data in a {@link MapTemplate} based on a simple search and replace map.
 *
 * @param searchAndReplace the map of keys to replace with their corresponding values
 *
 * @author Hugman
 */
public record ReplaceBlockEntitiesTemplateProcessor(Map<String, String> searchAndReplace) implements MapTemplateProcessor {
    public static final MapCodec<ReplaceBlockEntitiesTemplateProcessor> CODEC = Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("search_and_replace").xmap(ReplaceBlockEntitiesTemplateProcessor::new, ReplaceBlockEntitiesTemplateProcessor::searchAndReplace);

    @Override
    public MapCodec<? extends MapTemplateProcessor> getCodec() {
        return CODEC;
    }

    @Override
    public void processTemplate(MapTemplate template, ContextParameterMap.Builder parameters) {
        template.getBounds().forEach(pos -> {
            var nbtCompound = template.getBlockEntityNbt(pos);
            if (nbtCompound instanceof NbtCompound) {
                if (searchAndReplace(nbtCompound, false)) {
                    template.setBlockEntityNbt(pos, nbtCompound);
                }
            }
        });
    }

    private boolean searchAndReplace(NbtCompound compound, boolean hasChanged) {
        for (var key : compound.getKeys()) {
            var stringValue = compound.getString(key);
            if (stringValue.isPresent()) {
                var val = stringValue.get();
                for (var entry : searchAndReplace.entrySet()) {
                    if (val.equals(entry.getKey())) {
                        compound.putString(key, entry.getValue());
                        hasChanged = true;
                        break;
                    }
                }
            }
            var compoundValue = compound.getCompound(key);
            if (compoundValue.isPresent()) {
                var val = compoundValue.get();
                if (searchAndReplace(val, false)) {
                    hasChanged = true;
                    break;
                }
            }
            var listValue = compound.getList(key);
            if (listValue.isPresent()) {
                var list = listValue.get();
                for (var i = 0; i < list.size(); i++) {
                    var item = list.get(i);
                    if (item instanceof NbtCompound itemCompound) {
                        if (searchAndReplace(itemCompound, false)) {
                            list.set(i, itemCompound);
                            hasChanged = true;
                        }
                    }
                }
            }
        }
        return hasChanged;
    }
}
