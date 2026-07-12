package com.simibubi.create.foundation.utility.legacy.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CogWheelModelTest {

    @Test
    void preservesTheUpstreamSevenElementGearModel() {
        assertEquals(7, CogWheelModel.elements().size());
        assertEquals(45, CogWheelModel.elements().get(2).rotation().angle(), 0.0001);
        assertEquals(-45, CogWheelModel.elements().get(3).rotation().angle(), 0.0001);
    }

    @Test
    void keepsTheUpstreamToothOverhang() {
        CogWheelModel.Cuboid teeth = CogWheelModel.elements().get(1).bounds();

        assertEquals(-1.0 / 16, teeth.minX(), 0.0001);
        assertEquals(17.0 / 16, teeth.maxX(), 0.0001);
    }

    @Test
    void shrinksFaceUvsLikeVanillaFaceBakery() {
        CogWheelModel.Uv toothFace = new CogWheelModel.Uv(7, 8, 16, 9.5);
        CogWheelModel.Uv toothResult = CogWheelModel.shrinkUv(toothFace, CogWheelModel.Texture.COGWHEEL);
        CogWheelModel.Uv axisFace = new CogWheelModel.Uv(6, 0, 10, 16);
        CogWheelModel.Uv axisResult = CogWheelModel.shrinkUv(axisFace, CogWheelModel.Texture.AXIS);

        // FaceBakery uses sprite.uvShrinkRatio() == 4 / sprite width.
        assertEquals(7.5625, toothResult.minU(), 0.0001);
        assertEquals(15.4375, toothResult.maxU(), 0.0001);
        assertEquals(8.09375, toothResult.minV(), 0.0001);
        assertEquals(9.40625, toothResult.maxV(), 0.0001);
        assertEquals(6.5, axisResult.minU(), 0.0001);
        assertEquals(9.5, axisResult.maxU(), 0.0001);
        assertEquals(2, axisResult.minV(), 0.0001);
        assertEquals(14, axisResult.maxV(), 0.0001);
    }
}
