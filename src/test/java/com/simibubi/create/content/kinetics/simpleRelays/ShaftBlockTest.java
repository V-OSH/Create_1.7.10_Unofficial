package com.simibubi.create.content.kinetics.simpleRelays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.simibubi.create.foundation.utility.legacy.LegacyAxis;

import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

class ShaftBlockTest {

    @Test
    void placementTemporarilyStoresTheClickedFaceAxisInMetadata() {
        ShaftBlock shaft = new ShaftBlock();

        assertEquals(LegacyAxis.Y.getMetadata(), shaft.onBlockPlaced(null, 0, 0, 0, 1, 0, 0, 0, 0));
        assertEquals(LegacyAxis.Z.getMetadata(), shaft.onBlockPlaced(null, 0, 0, 0, 3, 0, 0, 0, 0));
        assertEquals(LegacyAxis.X.getMetadata(), shaft.onBlockPlaced(null, 0, 0, 0, 5, 0, 0, 0, 0));
    }

    @Test
    void placementUsesLookAxisWithoutAConnectedNeighbour() {
        assertEquals(LegacyAxis.X,
            ShaftBlock.resolvePlacementAxis(LegacyAxis.Y, LegacyAxis.X, null, false));
    }

    @Test
    void placementPrefersTheAxisOfAConnectedNeighbour() {
        assertEquals(LegacyAxis.Z,
            ShaftBlock.resolvePlacementAxis(LegacyAxis.Y, LegacyAxis.X, LegacyAxis.Z, false));
    }

    @Test
    void sneakingOverridesAConnectedNeighbourWithTheClickedFace() {
        assertEquals(LegacyAxis.Y,
            ShaftBlock.resolvePlacementAxis(LegacyAxis.Y, LegacyAxis.X, LegacyAxis.Z, true));
    }

    @Test
    void conflictingNeighbourAxesHaveNoPreference() {
        assertEquals(LegacyAxis.X, ShaftBlock.mergePreferredAxis(null, LegacyAxis.X));
        assertEquals(LegacyAxis.X, ShaftBlock.mergePreferredAxis(LegacyAxis.X, LegacyAxis.X));
        assertNull(ShaftBlock.mergePreferredAxis(LegacyAxis.X, LegacyAxis.Z));
    }

    @Test
    void usesTheLegacyLogRendererForAxisAwareUvs() {
        ShaftBlock shaft = new ShaftBlock();

        assertEquals(31, shaft.getRenderType());
    }

    @Test
    void interactionBoundsMatchUpstreamSixVoxelPole() {
        AxisAlignedBB bounds = AbstractShaftBlock.createInteractionBounds(LegacyAxis.X, 0, 0, 0);

        assertEquals(0.0, bounds.minX);
        assertEquals(1.0, bounds.maxX);
        assertEquals(5.0 / 16.0, bounds.minY);
        assertEquals(11.0 / 16.0, bounds.maxY);
        assertEquals(5.0 / 16.0, bounds.minZ);
        assertEquals(11.0 / 16.0, bounds.maxZ);
    }

    @Test
    void inheritsUpstreamAndesiteBreakingProperties() {
        ShaftBlock shaft = new ShaftBlock();

        assertEquals(1.5f, shaft.getBlockHardness(null, 0, 0, 0));
        assertEquals("pickaxe", shaft.getHarvestTool(LegacyAxis.Y.getMetadata()));
        assertEquals(0, shaft.getHarvestLevel(LegacyAxis.Y.getMetadata()));
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
