package xyz.nucleoid.plasmid.world.bubble;

import javax.annotation.Nullable;

public interface BubbleWorldHolder {
    void setBubbleWorld(BubbleWorld bubbleWorld);

    @Nullable
    BubbleWorld getBubbleWorld();
}
