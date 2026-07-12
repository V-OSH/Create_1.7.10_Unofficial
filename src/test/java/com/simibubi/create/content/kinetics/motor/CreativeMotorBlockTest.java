package com.simibubi.create.content.kinetics.motor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.simibubi.create.foundation.utility.legacy.LegacyAxis;

import net.minecraftforge.common.util.ForgeDirection;

class CreativeMotorBlockTest {

    @Test
    void placementMatchesUpstreamLookAndNeighbourPrecedence() {
        assertEquals(ForgeDirection.WEST,
            CreativeMotorBlock.resolvePlacementFacing(ForgeDirection.EAST, null, false));
        assertEquals(ForgeDirection.SOUTH,
            CreativeMotorBlock.resolvePlacementFacing(ForgeDirection.EAST, ForgeDirection.SOUTH, false));
        assertEquals(ForgeDirection.EAST,
            CreativeMotorBlock.resolvePlacementFacing(ForgeDirection.EAST, ForgeDirection.SOUTH, true));
    }

    @Test
    void exposesAShaftOnlyOnItsFacingSide() {
        CreativeMotorBlock motor = new CreativeMotorBlock();
        int metadata = ForgeDirection.EAST.ordinal();

        assertTrue(motor.hasShaftTowards(metadata, ForgeDirection.EAST));
        assertFalse(motor.hasShaftTowards(metadata, ForgeDirection.WEST));
        assertEquals(LegacyAxis.X, motor.getRotationAxis(metadata));
    }

    @Test
    void boundsMatchUpstreamDirectionalMotorShape() {
        CreativeMotorBlock motor = new CreativeMotorBlock();
        motor.setMotorBounds(ForgeDirection.UP);

        assertEquals(3.0 / 16.0, motor.getBlockBoundsMinX());
        assertEquals(13.0 / 16.0, motor.getBlockBoundsMaxX());
        assertEquals(0.0, motor.getBlockBoundsMinY());
        assertEquals(14.0 / 16.0, motor.getBlockBoundsMaxY());
        assertEquals(3.0 / 16.0, motor.getBlockBoundsMinZ());
        assertEquals(13.0 / 16.0, motor.getBlockBoundsMaxZ());
    }
}
