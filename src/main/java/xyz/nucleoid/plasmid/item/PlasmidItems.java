package xyz.nucleoid.plasmid.item;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import xyz.nucleoid.plasmid.Plasmid;

public final class PlasmidItems {
    public static final Item ADD_REGION = register("add_region", new AddRegionItem(new Item.Settings()));
    public static final Item INCLUDE_ENTITY = register("include_entity", new IncludeEntityItem(new Item.Settings()));

    private static Item register(String identifier, Item item) {
        return Registry.register(Registry.ITEM, new Identifier(Plasmid.ID, identifier), item);
    }
}
