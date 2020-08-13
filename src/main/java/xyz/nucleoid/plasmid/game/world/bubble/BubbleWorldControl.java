package xyz.nucleoid.plasmid.game.world.bubble;

public interface BubbleWorldControl {
    static <T> void enable(T value) {
        ((BubbleWorldControl) value).enable();
    }

    static <T> void disable(T value) {
        ((BubbleWorldControl) value).disable();
    }

    void disable();

    void enable();
}
