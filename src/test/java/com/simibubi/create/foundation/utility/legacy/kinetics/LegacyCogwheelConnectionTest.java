package com.simibubi.create.foundation.utility.legacy.kinetics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.simibubi.create.content.kinetics.base.LegacyKineticNetwork.Position;
import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.foundation.utility.legacy.LegacyAxis;

class LegacyCogwheelConnectionTest {

    @Test
    void largeAndSmallCogwheelsUseUpstreamDiagonalTwoToOneRatio() {
        Position large = new Position(0, 0, 0);
        Position small = new Position(1, 0, 1);

        assertEquals(-2, RotationPropagator.getCogwheelSpeedModifier(true, LegacyAxis.Y, false, LegacyAxis.Y,
            small.x() - large.x(), small.y() - large.y(), small.z() - large.z()));
        assertEquals(-.5, RotationPropagator.getCogwheelSpeedModifier(false, LegacyAxis.Y, true, LegacyAxis.Y,
            large.x() - small.x(), large.y() - small.y(), large.z() - small.z()));
    }

    @Test
    void perpendicularLargeCogwheelsRelayAtOneToOne() {
        assertEquals(1, RotationPropagator.getCogwheelSpeedModifier(true, LegacyAxis.X, true, LegacyAxis.Y,
            1, 1, 0));
        assertEquals(1, RotationPropagator.getCogwheelSpeedModifier(true, LegacyAxis.Y, true, LegacyAxis.X,
            -1, -1, 0));
        Position oppositeSide = new Position(1, -1, 0);
        assertEquals(-1, RotationPropagator.getCogwheelSpeedModifier(true, LegacyAxis.X, true, LegacyAxis.Y,
            oppositeSide.x(), oppositeSide.y(), oppositeSide.z()));
    }

    @Test
    void potentialNeighboursMatchUpstreamSimpleKineticBlockEntity() {
        Position origin = new Position(0, 0, 0);
        List<Position> large = LegacyKineticWorldAdapter.cogwheelNeighbours(origin, true, LegacyAxis.Y);
        List<Position> small = LegacyKineticWorldAdapter.cogwheelNeighbours(origin, false, LegacyAxis.Y);

        assertEquals(18, large.size());
        assertEquals(10, small.size());
        assertTrue(large.contains(new Position(1, 1, 0)));
        assertTrue(small.contains(new Position(1, 0, 1)));
    }
}
