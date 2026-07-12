package com.simibubi.create.foundation.utility.legacy.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ShaftRenderGeometryTest {

    @Test
    void usesOnlyTheOpaqueFourPixelStripFromTheUpstreamAxisTextures() {
        assertEquals(6.0 / 16, ShaftRenderGeometry.MIN, 0.0001);
        assertEquals(10.0 / 16, ShaftRenderGeometry.MAX, 0.0001);
        assertEquals(6, ShaftRenderGeometry.MIN_UV, 0.0001);
        assertEquals(10, ShaftRenderGeometry.MAX_UV, 0.0001);
    }

    @Test
    void motorOutputMatchesTheUpstreamHalfShaftModel() {
        assertEquals(8.0 / 16, ShaftRenderGeometry.HALF_START, 0.0001);
        assertEquals(1.0, ShaftRenderGeometry.HALF_END, 0.0001);
    }
}
