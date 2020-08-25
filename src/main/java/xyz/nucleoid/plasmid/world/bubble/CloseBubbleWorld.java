package xyz.nucleoid.plasmid.world.bubble;

public interface CloseBubbleWorld {
    static <T> void closeBubble(T value) {
        ((CloseBubbleWorld) value).closeBubble();
    }

    void closeBubble();
}
