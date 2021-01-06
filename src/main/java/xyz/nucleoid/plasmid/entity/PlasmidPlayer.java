package xyz.nucleoid.plasmid.entity;

/**
 * Used currently for storing api variables
 */
public interface PlasmidPlayer {

    /**
     * Allows you to check if the player is using the client sided plasmid api
     * @return if the player is using the client side api
     */
    boolean isUsingPlasmidApi();

    void setUsingPlasmidApi(boolean usingPlasmidApi);
}
