package xyz.nucleoid.plasmid.impl.component;

import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import xyz.nucleoid.plasmid.impl.Plasmid;

public final class PlasmidDataComponentTypes {
    private PlasmidDataComponentTypes() {
    }

    public static final ComponentType<Unit> OLD_COMBAT = register("old_combat", ComponentType.<Unit>builder()
            .codec(Unit.CODEC)
            .build());

    private static <T> ComponentType<T> register(String path, ComponentType<T> type) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(Plasmid.ID, path), type);
    }

    public static void register() {
        PolymerComponent.registerDataComponent(OLD_COMBAT);
    }
}
