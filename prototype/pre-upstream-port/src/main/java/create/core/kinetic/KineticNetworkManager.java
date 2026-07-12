package create.core.kinetic;

import create.shim.MyBlockPos;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    private static final Map<Integer, Set<Long>> dirtyNetworks = new HashMap<>();

    private KineticNetworkManager() {}

    /** Initialize a dimension's network map (called on WorldEvent.Load). */
    public static void onWorldLoad(int dimId) {
        networks.put(dimId, new HashMap<>());
        dirtyNetworks.put(dimId, new HashSet<>());
    }

    /** Discard a dimension's network map (called on WorldEvent.Unload). */
    public static void onWorldUnload(int dimId) {
        networks.remove(dimId);
        dirtyNetworks.remove(dimId);
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

    /** Mark a network dirty after chunk unload so it will be rebuilt when touched. */
    public static void markDirty(Long networkId, int dimId) {
        if (networkId == null) return;
        dirtyNetworks.computeIfAbsent(dimId, ignored -> new HashSet<>()).add(networkId);
    }

    /** True when chunk unload invalidated this network's topology. */
    public static boolean isDirty(Long networkId, int dimId) {
        Set<Long> dirty = dirtyNetworks.get(dimId);
        return dirty != null && dirty.contains(networkId);
    }

    /** Clear the dirty marker after scheduling or completing a rebuild. */
    public static void clearDirty(Long networkId, int dimId) {
        Set<Long> dirty = dirtyNetworks.get(dimId);
        if (dirty != null) {
            dirty.remove(networkId);
        }
    }

    /** For testing: clear all networks. */
    public static void reset() {
        networks.clear();
        dirtyNetworks.clear();
    }
}
