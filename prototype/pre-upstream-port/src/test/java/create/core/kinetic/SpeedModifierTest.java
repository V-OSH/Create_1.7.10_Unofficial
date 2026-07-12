package create.core.kinetic;

import create.shim.MyDirection;
import net.minecraftforge.common.util.ForgeDirection;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RotationPropagator.getRotationSpeedModifier across all
 * block type combinations.
 */
class SpeedModifierTest {

    // --- Shaft connections ---

    @Test
    void shaftToShaftSameAxis_returnsOne() {
        TestKineticTile from = new TestKineticTile(0, 0, 0, KineticBlockType.SHAFT, MyDirection.Axis.Y);
        TestKineticTile to = new TestKineticTile(0, 1, 0, KineticBlockType.SHAFT, MyDirection.Axis.Y);

        from.withConnection(ForgeDirection.UP);
        to.withConnection(ForgeDirection.DOWN);

        float modifier = RotationPropagator.getRotationSpeedModifier(
                from, to, ForgeDirection.UP, ForgeDirection.DOWN);
        assertEquals(1.0f, modifier, 0.001f);
    }

    @Test
    void shaftToShaftSameAxisDown_returnsOne() {
        TestKineticTile from = new TestKineticTile(0, 1, 0, KineticBlockType.SHAFT, MyDirection.Axis.Y);
        TestKineticTile to = new TestKineticTile(0, 0, 0, KineticBlockType.SHAFT, MyDirection.Axis.Y);

        from.withConnection(ForgeDirection.DOWN);
        to.withConnection(ForgeDirection.UP);

        float modifier = RotationPropagator.getRotationSpeedModifier(
                from, to, ForgeDirection.DOWN, ForgeDirection.UP);
        assertEquals(1.0f, modifier, 0.001f);
    }

    @Test
    void shaftToShaftXAxis_returnsOne() {
        TestKineticTile from = new TestKineticTile(0, 0, 0, KineticBlockType.SHAFT, MyDirection.Axis.X);
        TestKineticTile to = new TestKineticTile(1, 0, 0, KineticBlockType.SHAFT, MyDirection.Axis.X);

        from.withConnection(ForgeDirection.EAST);
        to.withConnection(ForgeDirection.WEST);

        // hasShaftTowards returns true for EAST/WEST on X axis
        float modifier = RotationPropagator.getRotationSpeedModifier(
                from, to, ForgeDirection.EAST, ForgeDirection.WEST);
        assertEquals(1.0f, modifier, 0.001f);
    }

    @Test
    void shaftMetadata_roundTripsAllAxes() {
        assertEquals(MyDirection.Axis.X, ShaftBlock.axisFromMeta(
                ShaftBlock.metaFromAxis(MyDirection.Axis.X)));
        assertEquals(MyDirection.Axis.Y, ShaftBlock.axisFromMeta(
                ShaftBlock.metaFromAxis(MyDirection.Axis.Y)));
        assertEquals(MyDirection.Axis.Z, ShaftBlock.axisFromMeta(
                ShaftBlock.metaFromAxis(MyDirection.Axis.Z)));
    }

    // --- Small cogwheel connections ---

    @Test
    void smallToSmallCogwheel_returnsNegativeOne() {
        TestKineticTile from = new TestKineticTile(1, 0, 0,
                KineticBlockType.SMALL_COGWHEEL, MyDirection.Axis.Z);
        TestKineticTile to = new TestKineticTile(0, 0, 0,
                KineticBlockType.SMALL_COGWHEEL, MyDirection.Axis.Y);

        // Neither is a shaft connection at this face
        float modifier = RotationPropagator.getRotationSpeedModifier(
                from, to, ForgeDirection.WEST, ForgeDirection.EAST);
        assertEquals(-1.0f, modifier, 0.001f);
    }

    // --- Large cogwheel connections ---

    @Test
    void largeToSmallCogwheel_doublesSpeed() {
        TestKineticTile from = new TestKineticTile(0, 0, 0,
                KineticBlockType.LARGE_COGWHEEL, MyDirection.Axis.Y);
        TestKineticTile to = new TestKineticTile(1, 0, 0,
                KineticBlockType.SMALL_COGWHEEL, MyDirection.Axis.Z);

        float modifier = RotationPropagator.getRotationSpeedModifier(
                from, to, ForgeDirection.EAST, ForgeDirection.WEST);
        assertEquals(-2.0f, modifier, 0.001f);
    }

    @Test
    void smallToLargeCogwheel_halvesSpeed() {
        TestKineticTile from = new TestKineticTile(1, 0, 0,
                KineticBlockType.SMALL_COGWHEEL, MyDirection.Axis.Z);
        TestKineticTile to = new TestKineticTile(0, 0, 0,
                KineticBlockType.LARGE_COGWHEEL, MyDirection.Axis.Y);

        float modifier = RotationPropagator.getRotationSpeedModifier(
                from, to, ForgeDirection.WEST, ForgeDirection.EAST);
        assertEquals(-0.5f, modifier, 0.001f);
    }

    @Test
    void largeToLargeCogwheel_returnsNegativeOne() {
        TestKineticTile from = new TestKineticTile(0, 0, 0,
                KineticBlockType.LARGE_COGWHEEL, MyDirection.Axis.Y);
        TestKineticTile to = new TestKineticTile(2, 0, 0,
                KineticBlockType.LARGE_COGWHEEL, MyDirection.Axis.Z);

        float modifier = RotationPropagator.getRotationSpeedModifier(
                from, to, ForgeDirection.EAST, ForgeDirection.WEST);
        assertEquals(-1.0f, modifier, 0.001f);
    }

    // --- Shaft to cogwheel ---

    @Test
    void shaftToSmallCogwheel_returnsNegativeOne() {
        TestKineticTile from = new TestKineticTile(0, 0, 0, KineticBlockType.SHAFT, MyDirection.Axis.Y);
        TestKineticTile to = new TestKineticTile(1, 0, 0,
                KineticBlockType.SMALL_COGWHEEL, MyDirection.Axis.Z);

        // Shaft-driven side does not have shaft face on EAST
        from.withConnection(ForgeDirection.EAST);
        to.withConnection(ForgeDirection.WEST);

        float modifier = RotationPropagator.getRotationSpeedModifier(
                from, to, ForgeDirection.EAST, ForgeDirection.WEST);
        assertEquals(-1.0f, modifier, 0.001f);
    }

    // --- Speed calculation examples ---

    @Test
    void largeDrivingSmall_doublesRPM() {
        // Large cogwheel at 32 RPM drives small cogwheel → 64 RPM (reversed)
        TestKineticTile large = new TestKineticTile(0, 0, 0,
                KineticBlockType.LARGE_COGWHEEL, MyDirection.Axis.Y)
                .withSpeed(32.0f)
                .asSource(32.0f, 512.0f);
        TestKineticTile small = new TestKineticTile(1, 0, 0,
                KineticBlockType.SMALL_COGWHEEL, MyDirection.Axis.Z);

        float modifier = RotationPropagator.getRotationSpeedModifier(
                large, small, ForgeDirection.EAST, ForgeDirection.WEST);
        float newSpeed = large.getSpeed() * modifier;
        assertEquals(64.0f, Math.abs(newSpeed), 0.001f);
    }

    @Test
    void smallDrivingLarge_halvesRPM() {
        // Small cogwheel at 64 RPM drives large cogwheel → 32 RPM (reversed)
        TestKineticTile small = new TestKineticTile(1, 0, 0,
                KineticBlockType.SMALL_COGWHEEL, MyDirection.Axis.Z)
                .withSpeed(64.0f)
                .asSource(64.0f, 256.0f);
        TestKineticTile large = new TestKineticTile(0, 0, 0,
                KineticBlockType.LARGE_COGWHEEL, MyDirection.Axis.Y);

        float modifier = RotationPropagator.getRotationSpeedModifier(
                small, large, ForgeDirection.WEST, ForgeDirection.EAST);
        float newSpeed = small.getSpeed() * modifier;
        assertEquals(32.0f, Math.abs(newSpeed), 0.001f);
    }
}
