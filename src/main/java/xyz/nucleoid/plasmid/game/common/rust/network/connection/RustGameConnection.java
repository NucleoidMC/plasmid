package xyz.nucleoid.plasmid.game.common.rust.network.connection;

import xyz.nucleoid.plasmid.game.common.rust.network.message.RustGameMessage;

public interface RustGameConnection {
    int DEFAULT_PORT = 12345;

    boolean send(RustGameMessage message);

    interface Handler {
        void acceptConnection();

        void acceptMessage(RustGameMessage message);

        void acceptError(Throwable cause);

        void acceptClosed();
    }
}
