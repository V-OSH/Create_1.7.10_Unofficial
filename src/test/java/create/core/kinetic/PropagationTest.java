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
 * Tests for RotationPropagator.handleAdded and handleRemoved using
 * stub IKineticTile instances.
 */
class PropagationTest {

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

    // --- Source propagation ---

    @Test
    void sourceCreatesOwnNetwork() {
        TestKineticTile source = place(new TestKineticTile(0, 0, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y).asSource(64.0f, 512.0f));

        RotationPropagator.handleAdded(source, lookup());

        assertEquals(64.0f, source.getSpeed(), 0.001f);
        assertNotNull(source.getNetworkId());
        assertEquals(0L, source.getNetworkId());  // pos (0,0,0) asLong = 0
        assertTrue(source.getSourcePosition().isEmpty());
    }

    @Test
    void shaftPropagatesToConnectedShaft() {
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

    @Test
    void speedPropagatesThreeInLine() {
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

        assertEquals(64.0f, middle.getSpeed(), 0.001f);
        assertEquals(64.0f, end.getSpeed(), 0.001f);
        assertEquals(source.getNetworkId(), end.getNetworkId());
    }

    @Test
    void notConnected_noPropagation() {
        TestKineticTile source = place(new TestKineticTile(0, 0, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y)
                .asSource(64.0f, 512.0f)
                .withConnection(ForgeDirection.UP));

        // Shaft does NOT connect downward (only upward connection flagged)
        TestKineticTile shaft = place(new TestKineticTile(0, 1, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y));
        // Missing withConnection(DOWN)

        RotationPropagator.handleAdded(source, lookup());

        assertEquals(0.0f, shaft.getSpeed(), 0.001f);
        assertNull(shaft.getNetworkId());
    }

    // --- Cogwheel propagation ---

    @Test
    void largeCogwheel_drivesSmallCogwheel_doubledSpeed() {
        TestKineticTile large = place(new TestKineticTile(0, 0, 0, KineticBlockType.LARGE_COGWHEEL,
                MyDirection.Axis.Y)
                .asSource(32.0f, 512.0f)
                .withConnection(ForgeDirection.EAST));

        TestKineticTile small = place(new TestKineticTile(1, 0, 0, KineticBlockType.SMALL_COGWHEEL,
                MyDirection.Axis.Z)
                .withConnection(ForgeDirection.WEST));

        RotationPropagator.handleAdded(large, lookup());

        // Large (32 RPM) → Small: modifier = -2, so speed = -64 → abs = 64
        assertEquals(64.0f, Math.abs(small.getSpeed()), 0.001f);
    }

    @Test
    void smallCogwheel_drivesLargeCogwheel_halvedSpeed() {
        TestKineticTile small = place(new TestKineticTile(1, 0, 0, KineticBlockType.SMALL_COGWHEEL,
                MyDirection.Axis.Z)
                .asSource(64.0f, 256.0f)
                .withConnection(ForgeDirection.WEST));

        TestKineticTile large = place(new TestKineticTile(0, 0, 0, KineticBlockType.LARGE_COGWHEEL,
                MyDirection.Axis.Y)
                .withConnection(ForgeDirection.EAST));

        RotationPropagator.handleAdded(small, lookup());

        // Small (64 RPM) → Large: modifier = -0.5, so speed = -32 → abs = 32
        assertEquals(32.0f, Math.abs(large.getSpeed()), 0.001f);
    }

    // --- Diagonal cogwheel connection ---

    @Test
    void diagonalCogwheelConnection_propagates() {
        // Large cogwheel at (0,0,0) on Y axis
        TestKineticTile large = place(new TestKineticTile(0, 0, 0, KineticBlockType.LARGE_COGWHEEL,
                MyDirection.Axis.Y)
                .asSource(32.0f, 512.0f));

        // Small cogwheel at diagonal (1,0,1) on X axis — perpendicular axes
        TestKineticTile small = place(new TestKineticTile(1, 0, 1, KineticBlockType.SMALL_COGWHEEL,
                MyDirection.Axis.X));

        RotationPropagator.handleAdded(large, lookup());

        // Should propagate via diagonal check (distSq = 2)
        assertEquals(64.0f, Math.abs(small.getSpeed()), 0.001f);
        assertEquals(large.getNetworkId(), small.getNetworkId());
    }

    // --- Removal ---

    @Test
    void removingMiddleShaft_clearsOrphans() {
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

        // First propagate
        RotationPropagator.handleAdded(source, lookup());
        assertEquals(64.0f, end.getSpeed(), 0.001f);

        // Remove middle — in the real game the TE is removed from the world,
        // so it won't be found by findAndAttach. Simulate that here.
        world.remove(middle.getKineticPos());
        middle.setSpeed(64.0f); // simulate it was running
        RotationPropagator.handleRemoved(middle.getKineticPos(), middle, lookup());

        // End should be cleared (orphaned)
        assertEquals(0.0f, end.getSpeed(), 0.001f);
        assertNull(end.getNetworkId());
        assertTrue(end.getSourcePosition().isEmpty());
    }

    // --- Cycle avoidance ---

    @Test
    void visitedNodes_notRevisited() {
        // Create a triangle: source → A → B → back to source (if not guarded)
        TestKineticTile source = place(new TestKineticTile(0, 0, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y)
                .asSource(64.0f, 512.0f)
                .withConnection(ForgeDirection.UP));

        TestKineticTile a = place(new TestKineticTile(0, 1, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y)
                .withConnection(ForgeDirection.DOWN)
                .withConnection(ForgeDirection.UP));

        TestKineticTile b = place(new TestKineticTile(0, 2, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y)
                .withConnection(ForgeDirection.DOWN));
        // b also connects back DOWN, but source is already visited — no infinite loop

        RotationPropagator.handleAdded(source, lookup());

        // All should have speed assigned
        assertEquals(64.0f, source.getSpeed(), 0.001f);
        assertEquals(64.0f, a.getSpeed(), 0.001f);
        assertEquals(64.0f, b.getSpeed(), 0.001f);
    }

    @Test
    void addSilently_tracksUnloadedMembers() {
        TestKineticTile consumer = new TestKineticTile(1, 0, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y).asConsumer(4.0f);
        KineticNetwork net = KineticNetworkManager.getOrCreateNetwork(100L, 0);

        net.addSilently(consumer, 512.0f, 256.0f);

        assertEquals(1, net.getUnloadedMembers());
        // calculateCapacity returns unloadedCapacity + sum(sources * |speed|)
        // unloaded = 512, no loaded sources → 512
        assertEquals(512.0f, net.calculateCapacity(0), 0.001f);
        // calculateStress returns unloadedStress + sum(members * |speed|)
        // At speed 0: unloaded = 256, loaded members = 4 * 0 = 0 → 256
        assertEquals(256.0f, net.calculateStress(0), 0.001f);
        // At speed 64: unloaded = 256, loaded members = 4 * 64 = 256 → 512
        assertEquals(512.0f, net.calculateStress(64.0f), 0.001f);
    }

    // --- Source removal (Phase 2 gap fix) ---

    @Test
    void sourceRemoved_clearsDownstreamNetwork() {
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

        // All downstream tiles should be cleared
        assertEquals(0.0f, middle.getSpeed(), 0.001f);
        assertNull(middle.getNetworkId());
        assertTrue(middle.getSourcePosition().isEmpty());

        assertEquals(0.0f, end.getSpeed(), 0.001f);
        assertNull(end.getNetworkId());
        assertTrue(end.getSourcePosition().isEmpty());
    }

    @Test
    void sourceRemoved_clearsBranchingNetwork() {
        TestKineticTile source = place(new TestKineticTile(0, 0, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y)
                .asSource(64.0f, 512.0f)
                .withConnection(ForgeDirection.UP)
                .withConnection(ForgeDirection.EAST));

        TestKineticTile upBranch = place(new TestKineticTile(0, 1, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y)
                .withConnection(ForgeDirection.DOWN));

        TestKineticTile eastBranch = place(new TestKineticTile(1, 0, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.X)
                .withConnection(ForgeDirection.WEST));

        RotationPropagator.handleAdded(source, lookup());
        assertEquals(64.0f, upBranch.getSpeed(), 0.001f);
        assertEquals(64.0f, Math.abs(eastBranch.getSpeed()), 0.001f);

        // Remove the source
        world.remove(source.getKineticPos());
        RotationPropagator.handleRemoved(source.getKineticPos(), source, lookup());

        // Both branches should be cleared
        assertEquals(0.0f, upBranch.getSpeed(), 0.001f);
        assertNull(upBranch.getNetworkId());
        assertEquals(0.0f, eastBranch.getSpeed(), 0.001f);
        assertNull(eastBranch.getNetworkId());
    }

    @Test
    void sourceRemoved_networkCleanedUp() {
        TestKineticTile source = place(new TestKineticTile(0, 0, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y)
                .asSource(64.0f, 512.0f)
                .withConnection(ForgeDirection.UP));

        TestKineticTile shaft = place(new TestKineticTile(0, 1, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y)
                .withConnection(ForgeDirection.DOWN));

        RotationPropagator.handleAdded(source, lookup());
        Long networkId = source.getNetworkId();
        assertNotNull(networkId);
        assertNotNull(KineticNetworkManager.getNetwork(networkId, 0));

        // Remove the source
        world.remove(source.getKineticPos());
        RotationPropagator.handleRemoved(source.getKineticPos(), source, lookup());

        // Network should be gone (empty)
        assertNull(KineticNetworkManager.getNetwork(networkId, 0));
    }
}
