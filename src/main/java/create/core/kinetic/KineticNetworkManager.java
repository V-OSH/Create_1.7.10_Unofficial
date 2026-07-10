package create.core.kinetic;

import create.shim.MyBlockPos;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages kinetic networks per dimension.
 *
 * <p>Each Minecraft world dimension gets its own map of network IDs to
 * {@link KineticNetwork} instances. Networks are created on demand and
 * garbage-collected when empty.</p>
 */
public final class KineticNetworkManager {

    /** dimension ID → (network ID → network) */
    private static final Map<Integer, Map<Long, KineticNetwork>> networks = new HashMap<>();

    private KineticNetworkManager() {}

    /** Initialize a dimension's network map (called on WorldEvent.Load). */
    public static void onWorldLoad(int dimId) {
        networks.put(dimId, new HashMap<>());
    }

    /** Discard a dimension's network map (called on WorldEvent.Unload). */
    public static void onWorldUnload(int dimId) {
        networks.remove(dimId);
    }

    /**
     * Get or create the network with the given ID in the given dimension.
     */
    public static KineticNetwork getOrCreateNetwork(Long networkId, int dimId) {
        Map<Long, KineticNetwork> dimNetworks = networks.get(dimId);
        if (dimNetworks == null) {
            dimNetworks = new HashMap<>();
            networks.put(dimId, dimNetworks);
        }
        KineticNetwork network = dimNetworks.get(networkId);
        if (network == null) {
            network = new KineticNetwork(networkId);
            dimNetworks.put(networkId, network);
        }
        return network;
    }

    /**
     * Remove an empty network from the registry.
     */
    public static void removeIfEmpty(Long networkId, int dimId) {
        Map<Long, KineticNetwork> dimNetworks = networks.get(dimId);
        if (dimNetworks == null) return;
        KineticNetwork network = dimNetworks.get(networkId);
        if (network != null && network.isEmpty()) {
            dimNetworks.remove(networkId);
        }
    }

    /**
     * Look up a network by ID in the given dimension, or return null.
     */
    public static KineticNetwork getNetwork(Long networkId, int dimId) {
        Map<Long, KineticNetwork> dimNetworks = networks.get(dimId);
        if (dimNetworks == null) return null;
        return dimNetworks.get(networkId);
    }

    /** For testing: clear all networks. */
    public static void reset() {
        networks.clear();
    }
}
