package xyz.nucleoid.plasmid.world.bubble;

import javax.annotation.Nullable;

public interface HasBubbleWorld {
    void setBubbleWorld(BubbleWorld bubbleWorld);

    @Nullable
    BubbleWorld getBubbleWorld();
}
