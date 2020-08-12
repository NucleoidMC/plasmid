package xyz.nucleoid.plasmid.fake;

/**
 * Allows for Vanilla blocks/items to be faked for the client side
 *
 * @param <F> The type being faked to
 */
public interface Fake<F> {

    /**
     * The proxy to be sent to the client
     *
     * @return The proxy to be sent
     */
    F getFaking();
}
