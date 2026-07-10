package create.core.kinetic;

import create.shim.MyBlockPos;
import create.shim.MyDirection;
import java.util.Optional;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Stub IKineticTile for unit testing the propagation algorithm.
 * No World or TileEntity dependency needed.
 */
class TestKineticTile implements IKineticTile {

    private final MyBlockPos pos;
    private final KineticBlockType type;
    private final MyDirection.Axis axis;
    private final boolean[] connections = new boolean[6];
    private float speed;
    private float generatedSpeed;
    private float stressApplied;
    private float capacityAdded;
    private Long networkId;
    private MyBlockPos sourcePosition;
    private int dimId;

    TestKineticTile(int x, int y, int z, KineticBlockType type, MyDirection.Axis axis) {
        this.pos = new MyBlockPos(x, y, z);
        this.type = type;
        this.axis = axis;
        this.dimId = 0;
    }

    // --- Configuration helpers ---

    TestKineticTile asSource(float generatedSpeed, float capacity) {
        this.generatedSpeed = generatedSpeed;
        this.capacityAdded = capacity;
        return this;
    }

    TestKineticTile asConsumer(float stressApplied) {
        this.stressApplied = stressApplied;
        return this;
    }

    TestKineticTile withConnection(ForgeDirection dir) {
        this.connections[dir.ordinal()] = true;
        return this;
    }

    TestKineticTile withConnection(ForgeDirection dir, boolean connected) {
        this.connections[dir.ordinal()] = connected;
        return this;
    }

    TestKineticTile withSpeed(float speed) {
        this.speed = speed;
        return this;
    }

    TestKineticTile withNetwork(Long networkId) {
        this.networkId = networkId;
        return this;
    }

    TestKineticTile withSource(MyBlockPos source) {
        this.sourcePosition = source;
        return this;
    }

    TestKineticTile withDim(int dimId) {
        this.dimId = dimId;
        return this;
    }

    // --- IKineticTile ---

    @Override
    public float getSpeed() { return speed; }

    @Override
    public void setSpeed(float speed) { this.speed = speed; }

    @Override
    public float calculateStressApplied() { return stressApplied; }

    @Override
    public float calculateAddedStressCapacity() { return capacityAdded; }

    @Override
    public boolean isConnected(ForgeDirection side) { return connections[side.ordinal()]; }

    @Override
    public void setConnected(ForgeDirection side, boolean connected) { connections[side.ordinal()] = connected; }

    @Override
    public boolean isSource() { return generatedSpeed != 0; }

    @Override
    public float getGeneratedSpeed() { return generatedSpeed; }

    @Override
    public Long getNetworkId() { return networkId; }

    @Override
    public void setNetworkId(Long id) { this.networkId = id; }

    @Override
    public Optional<MyBlockPos> getSourcePosition() { return Optional.ofNullable(sourcePosition); }

    @Override
    public void setSourcePosition(MyBlockPos pos) { this.sourcePosition = pos; }

    @Override
    public void removeSource() { this.sourcePosition = null; this.speed = 0; }

    @Override
    public MyBlockPos getKineticPos() { return pos; }

    @Override
    public int getDimensionId() { return dimId; }

    @Override
    public KineticBlockType getKineticType() { return type; }

    @Override
    public MyDirection.Axis getRotationAxis() { return axis; }

    // --- Helpers for test assertions ---

    MyBlockPos pos() { return pos; }
}
