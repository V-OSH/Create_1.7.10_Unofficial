package create.core.kinetic;

import create.shim.MyBlockPos;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.Block;

/**
 * Registry for stress impact and capacity values per block type.
 *
 * <p>Each kinetic block registers its base stress properties (at 1 RPM).
 * The effective stress/capacity is scaled by |speed| at the network level.</p>
 */
public final class BlockStressValues {

    private static final Map<Block, Double> IMPACTS = new HashMap<>();
    private static final Map<Block, Double> CAPACITIES = new HashMap<>();

    private BlockStressValues() {}

    /** Register a stress impact for a block (how much SU it consumes at 1 RPM). */
    public static void registerImpact(Block block, double baseImpact) {
        IMPACTS.put(block, baseImpact);
    }

    /** Register a stress capacity for a block (how much SU it provides at 1 RPM). */
    public static void registerCapacity(Block block, double baseCapacity) {
        CAPACITIES.put(block, baseCapacity);
    }

    /** Get the base stress impact for a block at 1 RPM, or 0. */
    public static double getImpact(Block block) {
        Double val = IMPACTS.get(block);
        return val != null ? val : 0.0;
    }

    /** Get the base stress capacity for a block at 1 RPM, or 0. */
    public static double getCapacity(Block block) {
        Double val = CAPACITIES.get(block);
        return val != null ? val : 0.0;
    }

    /** For testing: clear all registered values. */
    public static void reset() {
        IMPACTS.clear();
        CAPACITIES.clear();
    }
}
