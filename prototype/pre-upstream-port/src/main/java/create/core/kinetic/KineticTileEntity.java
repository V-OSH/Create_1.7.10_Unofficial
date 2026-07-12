package create.core.kinetic;

import create.shim.MyBlockPos;
import create.shim.MyDirection;
import java.util.Optional;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Base class for all kinetic tile entities. Implements {@link IKineticTile}
 * so the BFS propagator can traverse the network.
 *
 * <p>Face connections (6 booleans) and visual state are persisted to NBT.
 * Speed, source position, stress, and network ID are transient and are
 * recalculated by the kinetic network on load/placement.</p>
 */
public class KineticTileEntity extends TileEntity implements IKineticTile {

    /** Connected faces, one per ForgeDirection ordinal. Persisted to NBT. */
    protected final boolean[] connections = new boolean[6];

    /** The axis this block rotates around. Set by subclass. */
    protected MyDirection.Axis rotationAxis = MyDirection.Axis.Y;

    /** The kinetic block type for speed-ratio calculations. Set by subclass. */
    protected KineticBlockType kineticType = KineticBlockType.SHAFT;

    // --- Transient network fields ---

    /** Current rotation speed in RPM. Set by the propagator. */
    protected float speed;

    /** Base stress impact at 1 RPM (consumers) or 0 (relays/sources). */
    protected float stress;

    /** The network this tile belongs to, or null if not connected. */
    protected Long networkId;

    /** The position of the tile driving this one, or null. */
    protected MyBlockPos sourcePosition;

    /** Set to true to re-attach to the network on the next tick. */
    public boolean updateSpeed;

    /** Previous angle for partial-tick interpolation. */
    public float prevAngle;

    /** Current visual angle of the rotating part, in radians. Updated every client tick. */
    public float angle;

    // --- IKineticTile: speed ---

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public void setSpeed(float speed) {
        this.speed = speed;
        // Notify client of speed change for TESR animation
        if (worldObj != null && !worldObj.isRemote) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    // --- IKineticTile: stress ---

    @Override
    public float calculateStressApplied() {
        if (worldObj == null) return 0;
        return (float) BlockStressValues.getImpact(getBlockType());
    }

    @Override
    public float calculateAddedStressCapacity() {
        if (worldObj == null) return 0;
        return (float) BlockStressValues.getCapacity(getBlockType());
    }

    // --- IKineticTile: connections ---

    @Override
    public boolean isConnected(ForgeDirection side) {
        return connections[side.ordinal()];
    }

    @Override
    public void setConnected(ForgeDirection side, boolean connected) {
        connections[side.ordinal()] = connected;
    }

    // --- IKineticTile: source ---

    @Override
    public boolean isSource() {
        return getGeneratedSpeed() != 0;
    }

    @Override
    public float getGeneratedSpeed() {
        return 0; // overridden by generator subclasses
    }

    @Override
    public Optional<MyBlockPos> getSourcePosition() {
        return Optional.ofNullable(sourcePosition);
    }

    @Override
    public void setSourcePosition(MyBlockPos pos) {
        this.sourcePosition = pos;
    }

    @Override
    public void removeSource() {
        this.sourcePosition = null;
        this.speed = 0;
    }

    // --- IKineticTile: network ---

    @Override
    public Long getNetworkId() {
        return networkId;
    }

    @Override
    public void setNetworkId(Long id) {
        this.networkId = id;
    }

    // --- IKineticTile: position ---

    @Override
    public MyBlockPos getKineticPos() {
        return new MyBlockPos(xCoord, yCoord, zCoord);
    }

    @Override
    public int getDimensionId() {
        return worldObj != null ? worldObj.provider.dimensionId : 0;
    }

    // --- IKineticTile: type ---

    @Override
    public KineticBlockType getKineticType() {
        return kineticType;
    }

    /** Set the kinetic block type (called by block's createTileEntity). */
    public void setKineticType(KineticBlockType type) {
        this.kineticType = type;
    }

    @Override
    public MyDirection.Axis getRotationAxis() {
        return rotationAxis;
    }

    /** Set the rotation axis (called by block's createTileEntity). */
    public void setRotationAxis(MyDirection.Axis axis) {
        this.rotationAxis = axis;
    }

    // --- NBT ---

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        for (int i = 0; i < 6; i++) {
            connections[i] = tag.getBoolean("conn_" + i);
        }
        networkId = null;
        sourcePosition = null;
        speed = 0;
        angle = tag.getFloat("angle");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        for (int i = 0; i < 6; i++) {
            if (connections[i]) {
                tag.setBoolean("conn_" + i, true);
            }
        }
        tag.setFloat("angle", angle);
    }

    // --- Client sync ---

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        writeTransientSyncData(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        NBTTagCompound tag = pkt.func_148857_g();
        readFromNBT(tag);
        readTransientSyncData(tag);
    }

    /** Add transient runtime fields that are needed by client-side rendering. */
    protected void writeTransientSyncData(NBTTagCompound tag) {
        tag.setFloat("speed", speed);
    }

    /** Read transient runtime fields from a tile update packet. */
    protected void readTransientSyncData(NBTTagCompound tag) {
        speed = tag.getFloat("speed");
    }

    // --- Tick ---

    @Override
    public void validate() {
        super.validate();
        if (worldObj != null && !worldObj.isRemote) {
            updateSpeed = true;
        }
    }

    @Override
    public void updateEntity() {
        if (worldObj != null && worldObj.isRemote && speed != 0) {
            prevAngle = angle;
            // Correct RPM → radians/tick: 1 RPM = 2π/60 rad/sec = 2π/1200 rad/tick
            angle += (float) (speed * Math.PI * 2.0 / 60.0 / 20.0);
            float fullTurn = (float) (Math.PI * 2);
            if (angle > fullTurn || angle < -fullTurn) {
                angle %= fullTurn;
            }
        }
        if (worldObj != null && !worldObj.isRemote && updateSpeed) {
            updateSpeed = false;
            RotationPropagator.handleAdded(worldObj, this);
            KineticNetworkManager.clearDirty(networkId, getDimensionId());
        }
    }

    /**
     * Get the interpolated render angle for smooth animation.
     * Call from TESR with the partial tick time.
     */
    public float getRenderAngle(float partialTicks) {
        return prevAngle + (angle - prevAngle) * partialTicks;
    }
}
