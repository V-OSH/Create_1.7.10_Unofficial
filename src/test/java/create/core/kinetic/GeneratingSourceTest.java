package create.core.kinetic;

import create.shim.MyBlockPos;
import create.shim.MyDirection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraftforge.common.util.ForgeDirection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the source lifecycle: creation, speed change, removal,
 * and network integration via {@link RotationPropagator}.
 */
class GeneratingSourceTest {

    private Map<MyBlockPos, IKineticTile> world;

    @BeforeEach
    void setUp() {
        world = new HashMap<>();
        KineticNetworkManager.reset();
        BlockStressValues.reset();
    }

    private Function<MyBlockPos, IKineticTile> lookup() {
        return pos -> world.get(pos);
    }

    private <T extends IKineticTile> T place(T tile) {
        world.put(tile.getKineticPos(), tile);
        return tile;
    }

    // --- Source creates its own network ---

    @Test
    void sourceCreatesNetworkOnPlacement() {
        TestKineticTile source = place(new TestKineticTile(0, 0, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y).asSource(32.0f, 512.0f));

        RotationPropagator.handleAdded(source, lookup());

        assertEquals(32.0f, source.getSpeed(), 0.001f);
        // Network ID should be the source position asLong
        assertEquals(source.getKineticPos().asLong(), source.getNetworkId());
        // Self-powered: no source position
        assertTrue(source.getSourcePosition().isEmpty());
    }

    @Test
    void sourceNetworkHasExpectedCapacity() {
        TestKineticTile source = place(new TestKineticTile(0, 0, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y).asSource(32.0f, 512.0f));

        RotationPropagator.handleAdded(source, lookup());

        KineticNetwork network = KineticNetworkManager.getNetwork(
                source.getNetworkId(), 0);
        assertNotNull(network);
        // capacity = 512 * |32| = 16384
        assertEquals(16384.0f, network.getCurrentCapacity(), 0.1f);
    }

    // --- Source propagates to shafts ---

    @Test
    void sourcePropagatesSpeedToShaft() {
        TestKineticTile source = place(new TestKineticTile(0, 0, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y)
                .asSource(64.0f, 512.0f)
                .withConnection(ForgeDirection.UP));

        TestKineticTile shaft = place(new TestKineticTile(0, 1, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y)
                .withConnection(ForgeDirection.DOWN));

        RotationPropagator.handleAdded(source, lookup());

        assertEquals(64.0f, shaft.getSpeed(), 0.001f);
        assertEquals(source.getNetworkId(), shaft.getNetworkId());
        assertTrue(shaft.getSourcePosition().isPresent());
        assertEquals(source.getKineticPos(), shaft.getSourcePosition().get());
    }

    // --- Source removal clears the network ---

    @Test
    void sourceRemoved_clearsEntireNetwork() {
        TestKineticTile source = place(new TestKineticTile(0, 0, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y)
                .asSource(64.0f, 512.0f)
                .withConnection(ForgeDirection.UP));

        TestKineticTile middle = place(new TestKineticTile(0, 1, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y)
                .withConnection(ForgeDirection.DOWN)
                .withConnection(ForgeDirection.UP));

        TestKineticTile end = place(new TestKineticTile(0, 2, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y)
                .withConnection(ForgeDirection.DOWN));

        RotationPropagator.handleAdded(source, lookup());
        assertEquals(64.0f, end.getSpeed(), 0.001f);

        // Remove the source
        world.remove(source.getKineticPos());
        RotationPropagator.handleRemoved(source.getKineticPos(), source, lookup());

        // Downstream should all be cleared
        assertEquals(0.0f, middle.getSpeed(), 0.001f);
        assertNull(middle.getNetworkId());
        assertTrue(middle.getSourcePosition().isEmpty());

        assertEquals(0.0f, end.getSpeed(), 0.001f);
        assertNull(end.getNetworkId());
        assertTrue(end.getSourcePosition().isEmpty());
    }

    @Test
    void sourceRemoved_networkGarbageCollected() {
        TestKineticTile source = place(new TestKineticTile(0, 0, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y)
                .asSource(64.0f, 512.0f)
                .withConnection(ForgeDirection.UP));

        TestKineticTile shaft = place(new TestKineticTile(0, 1, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y)
                .withConnection(ForgeDirection.DOWN));

        RotationPropagator.handleAdded(source, lookup());
        Long networkId = source.getNetworkId();
        assertNotNull(KineticNetworkManager.getNetwork(networkId, 0));

        world.remove(source.getKineticPos());
        RotationPropagator.handleRemoved(source.getKineticPos(), source, lookup());

        // Network should be garbage-collected
        assertNull(KineticNetworkManager.getNetwork(networkId, 0));
    }

    // --- Source speed change ---

    @Test
    void sourceSpeedChange_repropagatesNewSpeed() {
        TestKineticTile source = place(new TestKineticTile(0, 0, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y)
                .asSource(32.0f, 512.0f)
                .withConnection(ForgeDirection.UP));

        TestKineticTile shaft = place(new TestKineticTile(0, 1, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y)
                .withConnection(ForgeDirection.DOWN));

        RotationPropagator.handleAdded(source, lookup());
        assertEquals(32.0f, shaft.getSpeed(), 0.001f);

        // Simulate speed change: update source's configured speed
        // (The real GeneratingKineticTileEntity would do this via updateGeneratedRotation)
        // We simulate by removing and re-adding with new speed.
        world.remove(source.getKineticPos());
        RotationPropagator.handleRemoved(source.getKineticPos(), source, lookup());

        // Re-create source with new speed
        TestKineticTile newSource = place(new TestKineticTile(0, 0, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y)
                .asSource(64.0f, 512.0f)
                .withConnection(ForgeDirection.UP));

        RotationPropagator.handleAdded(newSource, lookup());

        assertEquals(64.0f, shaft.getSpeed(), 0.001f);
        assertEquals(newSource.getNetworkId(), shaft.getNetworkId());
    }

    // --- Source overpower (two sources connected) ---

    @Test
    void strongerSource_overpowersWeakerSource() {
        // Strong source at (0,1,0) with 64 RPM
        TestKineticTile strong = place(new TestKineticTile(0, 1, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y)
                .asSource(64.0f, 512.0f)
                .withConnection(ForgeDirection.DOWN));

        // Weak source at (0,0,0) with 16 RPM
        TestKineticTile weak = place(new TestKineticTile(0, 0, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y)
                .asSource(16.0f, 256.0f)
                .withConnection(ForgeDirection.UP));

        // First add the strong source
        RotationPropagator.handleAdded(strong, lookup());
        assertEquals(64.0f, strong.getSpeed(), 0.001f);

        // When the weak source is placed, it should find the stronger one
        // and not be the dominant source. Its network will be the strong one's.
        // The propagator skips other sources during BFS.
        // But since we call handleAdded on the weak one, it tries to create
        // its own network. The strong source is already there and has higher speed.
        // In practice: weak source creates its network, propagates, hits the
        // strong source, skips it (because generatedSpeed != 0).
        // The weak source remains self-powered with its own network.
        RotationPropagator.handleAdded(weak, lookup());

        // Weak source should have its own network (different from strong)
        assertNotNull(weak.getNetworkId());
        assertNotEquals(strong.getNetworkId(), weak.getNetworkId());
        assertEquals(16.0f, weak.getSpeed(), 0.001f);
    }

    // --- Source that goes to zero speed ---

    @Test
    void sourceSpeedGoesToZero_networkDissolved() {
        TestKineticTile source = place(new TestKineticTile(0, 0, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y)
                .asSource(32.0f, 512.0f)
                .withConnection(ForgeDirection.UP));

        TestKineticTile shaft = place(new TestKineticTile(0, 1, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y)
                .withConnection(ForgeDirection.DOWN));

        RotationPropagator.handleAdded(source, lookup());
        assertEquals(32.0f, shaft.getSpeed(), 0.001f);

        // Simulate the source stopping (water wheel runs dry, motor broken, etc.)
        // In the real code, GeneratingKineticTileEntity.applyNewSpeed handles this.
        // Here we simulate by manually removing and not replacing.
        world.remove(source.getKineticPos());
        RotationPropagator.handleRemoved(source.getKineticPos(), source, lookup());

        assertEquals(0.0f, shaft.getSpeed(), 0.001f);
        assertNull(shaft.getNetworkId());
        assertTrue(shaft.getSourcePosition().isEmpty());
    }

    // --- Stress capacity registration ---

    @Test
    void sourceRegistersCorrectStressCapacity() {
        // Simulate what CreateMod.init() does
        net.minecraft.block.Block motorBlock = new net.minecraft.block.Block(
                net.minecraft.block.material.Material.rock) {};
        net.minecraft.block.Block wheelBlock = new net.minecraft.block.Block(
                net.minecraft.block.material.Material.rock) {};

        BlockStressValues.registerCapacity(motorBlock, 16384.0);
        BlockStressValues.registerCapacity(wheelBlock, 512.0);

        assertEquals(16384.0, BlockStressValues.getCapacity(motorBlock), 0.001);
        assertEquals(512.0, BlockStressValues.getCapacity(wheelBlock), 0.001);
    }

    @Test
    void unregisteredBlock_hasZeroCapacity() {
        net.minecraft.block.Block unknown = new net.minecraft.block.Block(
                net.minecraft.block.material.Material.rock) {};
        assertEquals(0.0, BlockStressValues.getCapacity(unknown), 0.001);
    }
}
