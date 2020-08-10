package net.gegy1000.plasmid.item;

import net.gegy1000.plasmid.block.CustomBlock;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

public class CustomBlockItem {

    private final CustomBlock block;
    private final CustomItem item;
    private final Identifier id;

    public CustomBlockItem(String displayName, Identifier id) {
        this.id = id;
        block = CustomBlock.builder()
                .id(id)
                .name(new LiteralText(displayName))
                .register();
        item = CustomItem.builder()
                .id(id)
                .name(new LiteralText(displayName))
                .register();
    }

}
