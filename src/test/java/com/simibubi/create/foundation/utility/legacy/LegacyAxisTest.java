package com.simibubi.create.foundation.utility.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LegacyAxisTest {

    @Test
    void everyAxisRoundTripsThroughBlockMetadata() {
        for (LegacyAxis axis : LegacyAxis.values()) {
            assertEquals(axis, LegacyAxis.fromMetadata(axis.getMetadata()));
        }
    }

    @Test
    void placementFaceSelectsTheShaftAxis() {
        assertEquals(LegacyAxis.Y, LegacyAxis.fromPlacementSide(0));
        assertEquals(LegacyAxis.Y, LegacyAxis.fromPlacementSide(1));
        assertEquals(LegacyAxis.Z, LegacyAxis.fromPlacementSide(2));
        assertEquals(LegacyAxis.Z, LegacyAxis.fromPlacementSide(3));
        assertEquals(LegacyAxis.X, LegacyAxis.fromPlacementSide(4));
        assertEquals(LegacyAxis.X, LegacyAxis.fromPlacementSide(5));
    }
}
