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
    void keepsTheUpstreamToothOverhangAndPerTextureUvScale() {
        CogWheelModel.Cuboid teeth = CogWheelModel.elements().get(1).bounds();

        assertEquals(-1.0 / 16, teeth.minX(), 0.0001);
        assertEquals(17.0 / 16, teeth.maxX(), 0.0001);
        assertEquals(0.5, CogWheelModel.textureCoordinateScale(CogWheelModel.Texture.COGWHEEL), 0.0001);
        assertEquals(1.0, CogWheelModel.textureCoordinateScale(CogWheelModel.Texture.AXIS), 0.0001);
        assertEquals(1.0, CogWheelModel.textureCoordinateScale(CogWheelModel.Texture.AXIS_TOP), 0.0001);
    }
}
