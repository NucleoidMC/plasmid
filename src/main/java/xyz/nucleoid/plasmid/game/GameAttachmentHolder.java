package xyz.nucleoid.plasmid.game;

import org.jetbrains.annotations.Nullable;

public interface GameAttachmentHolder {
    @Nullable
    <T> T getAttachment(GameAttachment<? extends T> attachment);

    default <T> T getAttachmentOrThrow(GameAttachment<? extends T> attachment) {
        T value = this.getAttachment(attachment);
        if (value == null) {
            throw new IllegalArgumentException("Missing attachment " + attachment + " on " + this);
        }
        return value;
    }

    <T> void setAttachment(GameAttachment<? super T> attachment, @Nullable T value);
}
