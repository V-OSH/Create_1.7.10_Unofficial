package create.core.kinetic.source;

import create.core.kinetic.GeneratingKineticTileEntity;
import create.core.kinetic.KineticBlockType;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Water wheel — generates rotational power from adjacent water or lava.
 *
 * <p>Checks the 4 blocks in the wheel's plane (perpendicular to the
 * rotation axis). Any water or lava adjacent to the wheel contributes
 * flow. The flow score is clamped to [-1, 1] and used to scale the
 * generated speed.</p>
 *
 * <p>Generated speed: clamp(flowScore, -1, 1) × 8 RPM. Base capacity:
 * 512 SU at 1 RPM.</p>
 */
public class WaterWheelTileEntity extends GeneratingKineticTileEntity {

    /** Cumulative flow score from adjacent water/lava blocks. Persisted to NBT. */
    private int flowScore;

    /** Tick counter for periodic re-validation. */
    private int tickCounter;

    // Offset definitions for the 4 cardinal directions perpendicular to each axis.
    // For X-axis wheels (wheel rotates in Y-Z plane): check Z and Y directions.
    // For Z-axis wheels (wheel rotates in X-Y plane): check X and Y directions.
    private static final int[][] OFFSETS_X = {{0, 0, 1}, {0, 0, -1}, {0, 1, 0}, {0, -1, 0}};
    private static final int[][] OFFSETS_Z = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}};

    public WaterWheelTileEntity() {
        setKineticType(KineticBlockType.SOURCE);
    }

    /** Wheel size: 1 = small, 2 = large (future). */
    public int getSize() {
        return 1;
    }

    @Override
    public float getGeneratedSpeed() {
        return clamp(flowScore, -1, 1) * 8.0f / getSize();
    }

    // --- Flow detection ---

    /**
     * Recalculate the flow score based on adjacent water/lava blocks.
     * Called by the block on neighbor change and periodically by the TE tick.
     */
    public void determineAndApplyFlowScore() {
        if (worldObj == null || worldObj.isRemote) return;

        int axisMeta = getBlockMetadata() & 1; // 0 = X axis, 1 = Z axis
        int[][] offsets = (axisMeta == 0) ? OFFSETS_X : OFFSETS_Z;

        int score = 0;
        for (int[] off : offsets) {
            int bx = xCoord + off[0];
            int by = yCoord + off[1];
            int bz = zCoord + off[2];
            Block block = worldObj.getBlock(bx, by, bz);

            if (isWater(block) || isLava(block)) {
                score++;
            }
        }

        setFlowScoreAndUpdate(score);
    }

    private static boolean isWater(Block block) {
        return block == Blocks.water || block == Blocks.flowing_water;
    }

    private static boolean isLava(Block block) {
        return block == Blocks.lava || block == Blocks.flowing_lava;
    }

    private void setFlowScoreAndUpdate(int score) {
        if (flowScore == score) return;
        flowScore = score;
        updateGeneratedRotation();
        markDirty();
    }

    // --- Tick ---

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (worldObj != null && !worldObj.isRemote) {
            tickCounter++;
            if (tickCounter % 20 == 0) { // Every second
                determineAndApplyFlowScore();
            }
        }
    }

    // --- NBT ---

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        flowScore = tag.getInteger("FlowScore");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("FlowScore", flowScore);
    }

    // --- Math ---

    private static float clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
