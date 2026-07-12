package com.simibubi.create.content.kinetics.simpleRelays;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.simibubi.create.foundation.utility.legacy.LegacyAxis;

import net.minecraftforge.common.util.ForgeDirection;

class ShaftBlockTest {

    @Test
    void placementStoresTheClickedFaceAxisInMetadata() {
        ShaftBlock shaft = new ShaftBlock();

        assertEquals(LegacyAxis.Y.getMetadata(), shaft.onBlockPlaced(null, 0, 0, 0, 1, 0, 0, 0, 0));
        assertEquals(LegacyAxis.Z.getMetadata(), shaft.onBlockPlaced(null, 0, 0, 0, 3, 0, 0, 0, 0));
        assertEquals(LegacyAxis.X.getMetadata(), shaft.onBlockPlaced(null, 0, 0, 0, 5, 0, 0, 0, 0));
    }

    @Test
    void shaftOnlyConnectsAlongItsRotationAxis() {
        ShaftBlock shaft = new ShaftBlock();

        assertEquals(true, shaft.hasShaftTowards(LegacyAxis.X.getMetadata(), ForgeDirection.WEST));
        assertEquals(true, shaft.hasShaftTowards(LegacyAxis.X.getMetadata(), ForgeDirection.EAST));
        assertEquals(false, shaft.hasShaftTowards(LegacyAxis.X.getMetadata(), ForgeDirection.UP));
        assertEquals(false, shaft.hasShaftTowards(LegacyAxis.Y.getMetadata(), ForgeDirection.UNKNOWN));
    }
}
