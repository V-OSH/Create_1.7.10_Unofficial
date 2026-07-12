package com.simibubi.create.foundation.utility.legacy.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class KineticRenderMathTest {

    @Test
    void angularTravelIsProportionalToSignedSpeed() {
        assertEquals(9.6f, KineticRenderMath.angleDelta(1, 32), 0.0001f);
        assertEquals(19.2f, KineticRenderMath.angleDelta(1, 64), 0.0001f);
        assertEquals(-9.6f, KineticRenderMath.angleDelta(1, -32), 0.0001f);
    }

    @Test
    void matchesUpstreamTimeToAngleFormula() {
        assertEquals(48.0f, KineticRenderMath.angleDegrees(5, 0, 32), 0.0001f);
        assertEquals(52.8f, KineticRenderMath.angleDegrees(5, 0.5f, 32), 0.0001f);
    }
}
