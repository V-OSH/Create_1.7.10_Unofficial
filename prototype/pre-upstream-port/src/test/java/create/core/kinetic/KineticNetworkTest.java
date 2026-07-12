package create.core.kinetic;

import create.shim.MyBlockPos;
import create.shim.MyDirection;
import net.minecraftforge.common.util.ForgeDirection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for KineticNetwork stress/capacity aggregation and overstress detection.
 */
class KineticNetworkTest {

    @BeforeEach
    void setUp() {
        KineticNetworkManager.reset();
        BlockStressValues.reset();
    }

    // --- Network creation ---

    @Test
    void newNetwork_hasNoMembers() {
        KineticNetwork net = KineticNetworkManager.getOrCreateNetwork(42L, 0);
        assertEquals(0, net.getSize());
        assertEquals(0.0f, net.getCurrentCapacity(), 0.001f);
        assertEquals(0.0f, net.getCurrentStress(), 0.001f);
    }

    @Test
    void getOrCreateNetwork_returnsSameInstance() {
        KineticNetwork a = KineticNetworkManager.getOrCreateNetwork(100L, 0);
        KineticNetwork b = KineticNetworkManager.getOrCreateNetwork(100L, 0);
        assertSame(a, b);
    }

    @Test
    void differentIds_createDifferentNetworks() {
        KineticNetwork a = KineticNetworkManager.getOrCreateNetwork(1L, 0);
        KineticNetwork b = KineticNetworkManager.getOrCreateNetwork(2L, 0);
        assertNotSame(a, b);
    }

    // --- Stress and capacity ---

    @Test
    void sourceAddsCapacity() {
        TestKineticTile source = new TestKineticTile(0, 0, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y).asSource(64.0f, 512.0f);
        KineticNetwork net = KineticNetworkManager.getOrCreateNetwork(100L, 0);

        net.add(source, 64.0f);
        net.updateFromSpeed(64.0f);

        assertEquals(512.0f * 64.0f, net.getCurrentCapacity(), 0.001f);
        assertEquals(0.0f, net.getCurrentStress(), 0.001f);
    }

    @Test
    void consumerAddsStress() {
        TestKineticTile consumer = new TestKineticTile(1, 0, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y).asConsumer(4.0f);
        KineticNetwork net = KineticNetworkManager.getOrCreateNetwork(100L, 0);

        net.add(consumer, 64.0f);
        net.updateFromSpeed(64.0f);

        assertEquals(0.0f, net.getCurrentCapacity(), 0.001f);
        assertEquals(4.0f * 64.0f, net.getCurrentStress(), 0.001f);
    }

    @Test
    void overstressDetected() {
        TestKineticTile source = new TestKineticTile(0, 0, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y).asSource(64.0f, 256.0f);  // 256 SU at 1 RPM
        TestKineticTile consumer = new TestKineticTile(1, 0, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y).asConsumer(16.0f);        // 16 SU at 1 RPM

        KineticNetwork net = KineticNetworkManager.getOrCreateNetwork(100L, 0);

        net.add(source, 64.0f);
        net.add(consumer, 64.0f);
        net.updateFromSpeed(64.0f);

        // capacity = 256 * 64 = 16384, stress = 16 * 64 = 1024
        assertFalse(net.isOverStressed(), "network should not be overstressed");

        // Add another heavy consumer
        TestKineticTile heavyConsumer = new TestKineticTile(2, 0, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y).asConsumer(300.0f);
        net.add(heavyConsumer, 64.0f);
        net.updateFromSpeed(64.0f);

        // stress = (16 + 300) * 64 = 20224, capacity = 16384
        assertTrue(net.isOverStressed(), "network should be overstressed");
    }

    @Test
    void removingConsumerReducesStress() {
        TestKineticTile consumer = new TestKineticTile(1, 0, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y).asConsumer(4.0f).withSpeed(64.0f);
        KineticNetwork net = KineticNetworkManager.getOrCreateNetwork(100L, 0);

        net.add(consumer, 64.0f);
        net.updateFromSpeed(64.0f);
        assertEquals(4.0f * 64.0f, net.getCurrentStress(), 0.001f);

        net.remove(consumer);
        net.updateFromSpeed(64.0f);
        assertEquals(0.0f, net.getCurrentStress(), 0.001f);
    }

    // --- Network size ---

    @Test
    void sizeIncrementsWithMembers() {
        KineticNetwork net = KineticNetworkManager.getOrCreateNetwork(100L, 0);
        assertEquals(0, net.getSize());

        TestKineticTile t1 = new TestKineticTile(1, 0, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y).asConsumer(1.0f);
        net.add(t1, 64.0f);
        assertEquals(1, net.getSize());
    }

    @Test
    void emptyNetworkRemoval() {
        KineticNetwork net = KineticNetworkManager.getOrCreateNetwork(100L, 0);
        assertNotNull(KineticNetworkManager.getNetwork(100L, 0));

        KineticNetworkManager.removeIfEmpty(100L, 0);
        assertNull(KineticNetworkManager.getNetwork(100L, 0));
    }

    // --- Dimension isolation ---

    @Test
    void differentDimensions_isolatedNetworks() {
        KineticNetwork net0 = KineticNetworkManager.getOrCreateNetwork(100L, 0);
        KineticNetwork net1 = KineticNetworkManager.getOrCreateNetwork(100L, 1);
        assertNotSame(net0, net1);
    }

    @Test
    void dirtyNetworksAreDimensionScoped() {
        KineticNetworkManager.markDirty(100L, 0);

        assertTrue(KineticNetworkManager.isDirty(100L, 0));
        assertFalse(KineticNetworkManager.isDirty(100L, 1));

        KineticNetworkManager.clearDirty(100L, 0);

        assertFalse(KineticNetworkManager.isDirty(100L, 0));
    }

    @Test
    void worldUnloadClearsDirtyNetworks() {
        KineticNetworkManager.markDirty(100L, 0);
        assertTrue(KineticNetworkManager.isDirty(100L, 0));

        KineticNetworkManager.onWorldUnload(0);

        assertFalse(KineticNetworkManager.isDirty(100L, 0));
    }

    // --- Speed scaling ---

    @Test
    void stressScalesWithSpeed() {
        TestKineticTile consumer = new TestKineticTile(1, 0, 0, KineticBlockType.SHAFT,
                MyDirection.Axis.Y).asConsumer(4.0f);
        KineticNetwork net = KineticNetworkManager.getOrCreateNetwork(100L, 0);

        net.add(consumer, 16.0f);
        net.updateFromSpeed(16.0f);
        float stressAt16 = net.getCurrentStress();

        net.remove(consumer);
        net.add(consumer, 64.0f);
        net.updateFromSpeed(64.0f);
        float stressAt64 = net.getCurrentStress();

        // Stress at 64 RPM = 4x stress at 16 RPM (same base impact)
        assertEquals(stressAt16 * 4.0f, stressAt64, 0.01f);
    }
}
