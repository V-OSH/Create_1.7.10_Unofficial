package create.core.kinetic.source;

import create.core.kinetic.GeneratingKineticTileEntity;
import create.core.kinetic.KineticBlockType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Creative-only motor that generates configurable rotational speed.
 *
 * <p>Always-on — no external conditions required. Produces configurable
 * speed (default 16 RPM, range -256 to +256 RPM) with extremely high
 * stress capacity (16,384 SU at 1 RPM).</p>
 *
 * <p>Speed is adjusted by right-clicking the block (see
 * {@link CreativeMotorBlock#onBlockActivated}).</p>
 */
public class CreativeMotorTileEntity extends GeneratingKineticTileEntity {

    public static final int DEFAULT_SPEED = 16;
    public static final int MAX_SPEED = 256;
    private static final int STEP = 16;

    /** Configured speed in RPM. Persisted to NBT. */
    private int configuredSpeed = DEFAULT_SPEED;

    public CreativeMotorTileEntity() {
        setKineticType(KineticBlockType.SOURCE);
    }

    // --- GeneratingKineticTileEntity ---

    @Override
    public float getGeneratedSpeed() {
        if (worldObj == null) return 0;
        // Safety: if the block was somehow removed but the TE lingers
        if (!(worldObj.getBlock(xCoord, yCoord, zCoord) instanceof CreativeMotorBlock)) {
            return 0;
        }
        // Direction is handled by the block's facing — for now, absolute speed.
        // Sign can be added in a later iteration for direction control.
        return configuredSpeed;
    }

    // --- Speed adjustment ---

    /**
     * Cycle the configured speed. Positive step = increase, negative = decrease.
     * Wraps around at boundaries, skipping zero.
     */
    public void cycleSpeed(boolean reverse) {
        int step = reverse ? -STEP : STEP;
        int newSpeed = configuredSpeed + step;

        // Wrap around
        if (newSpeed > MAX_SPEED) newSpeed = -MAX_SPEED;
        if (newSpeed < -MAX_SPEED) newSpeed = MAX_SPEED;
        // Skip zero
        if (newSpeed == 0) newSpeed = step > 0 ? STEP : -STEP;

        if (newSpeed != configuredSpeed) {
            configuredSpeed = newSpeed;
            updateGeneratedRotation();
            markDirty();
        }
    }

    /** Returns the currently configured speed (for potential goggle display). */
    public int getConfiguredSpeed() {
        return configuredSpeed;
    }

    /** Set the configured speed directly (used by GUI packet). */
    public void setConfiguredSpeed(int speed) {
        this.configuredSpeed = clamp(speed, -MAX_SPEED, MAX_SPEED);
        if (this.configuredSpeed == 0) this.configuredSpeed = (speed >= 0) ? STEP : -STEP;
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    /**
     * Override the default axis-based shaft detection. The motor only has
     * a shaft on its facing face — not both faces along the axis.
     */
    @Override
    public boolean hasShaftTowards(ForgeDirection face) {
        if (worldObj == null) return false;
        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        ForgeDirection facing = ForgeDirection.getOrientation(meta & 7);
        return face == facing;
    }

    // --- NBT ---

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        configuredSpeed = tag.getInteger("ConfiguredSpeed");
        if (configuredSpeed == 0) configuredSpeed = DEFAULT_SPEED;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("ConfiguredSpeed", configuredSpeed);
    }
}
