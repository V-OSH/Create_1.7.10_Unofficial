package com.simibubi.create.content.kinetics.simpleRelays;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.simibubi.create.foundation.utility.legacy.LegacyAxis;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraft.util.AxisAlignedBB;

class CogWheelBlockTest {

    @Test
    void placementMatchesUpstreamCogwheelPrecedence() {
        assertEquals(LegacyAxis.Z,
            CogWheelBlock.resolvePlacementAxis(LegacyAxis.Y, LegacyAxis.Z, LegacyAxis.X, false));
        assertEquals(LegacyAxis.X,
            CogWheelBlock.resolvePlacementAxis(LegacyAxis.Y, null, LegacyAxis.X, false));
        assertEquals(LegacyAxis.Y,
            CogWheelBlock.resolvePlacementAxis(LegacyAxis.Y, LegacyAxis.Z, LegacyAxis.X, true));
    }

    @Test
    void shaftConnectionExistsOnlyAlongTheCogwheelAxis() {
        CogWheelBlock cogwheel = new CogWheelBlock();

        assertEquals(true, cogwheel.hasShaftTowards(LegacyAxis.X.getMetadata(), ForgeDirection.EAST));
        assertEquals(false, cogwheel.hasShaftTowards(LegacyAxis.X.getMetadata(), ForgeDirection.NORTH));
    }

    @Test
    void selectionIncludesBothTheGearBodyAndAxialShaft() {
        AxisAlignedBB bounds = CogWheelBlock.createSelectionBounds(LegacyAxis.X, 0, 0, 0);

        assertEquals(0, bounds.minX);
        assertEquals(1, bounds.maxX);
        assertEquals(2.0 / 16, bounds.minY);
        assertEquals(14.0 / 16, bounds.maxY);
        assertEquals(2.0 / 16, bounds.minZ);
        assertEquals(14.0 / 16, bounds.maxZ);
    }

    @Test
    void usesUpstreamWoodSoundsAndAcceptsAxeOrPickaxe() {
        CogWheelBlock cogwheel = new CogWheelBlock();

        assertEquals("dig.wood", cogwheel.stepSound.getBreakSound());
        assertEquals(true, cogwheel.isToolEffective("axe", 0));
        assertEquals(true, cogwheel.isToolEffective("pickaxe", 0));
    }
}
