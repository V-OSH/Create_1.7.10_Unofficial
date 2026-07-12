package com.simibubi.create.foundation.utility.legacy.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraftforge.common.util.ForgeDirection;

class CreativeMotorModelTest {

    @Test
    void matchesUpstreamHorizontalAndVerticalElementCounts() {
        assertEquals(12, CreativeMotorModel.elementsFor(ForgeDirection.SOUTH).size());
        assertEquals(12, CreativeMotorModel.elementsFor(ForgeDirection.EAST).size());
        assertEquals(11, CreativeMotorModel.elementsFor(ForgeDirection.UP).size());
        assertEquals(11, CreativeMotorModel.elementsFor(ForgeDirection.DOWN).size());
    }

    @Test
    void transformsTheHorizontalModelTowardTheOutputFace() {
        CreativeMotorModel.Cuboid southRim = CreativeMotorModel.elementsFor(ForgeDirection.SOUTH).get(1).bounds();
        CreativeMotorModel.Cuboid northRim = CreativeMotorModel.elementsFor(ForgeDirection.NORTH).get(1).bounds();

        assertTrue(southRim.maxZ() > 0.8);
        assertTrue(northRim.minZ() < 0.2);
    }

    @Test
    void keepsUpstreamSideAndEndTexturesSeparate() {
        CreativeMotorModel.Element rim = CreativeMotorModel.elementsFor(ForgeDirection.SOUTH).get(0);
        CreativeMotorModel.Element rail = CreativeMotorModel.elementsFor(ForgeDirection.SOUTH).get(5);

        assertEquals(CreativeMotorModel.Texture.CASING, rim.sideTexture());
        assertEquals(CreativeMotorModel.Texture.MOTOR, rim.capTexture());
        assertEquals(CreativeMotorModel.Texture.AXIS, rail.sideTexture());
        assertEquals(CreativeMotorModel.Texture.MOTOR, rail.capTexture());
    }

    @Test
    void preservesUpstreamPerFaceUvAndRotation() {
        CreativeMotorModel.Face north = CreativeMotorModel.baseHorizontalElements().get(0)
            .faces().get(CreativeMotorModel.Direction.NORTH);
        CreativeMotorModel.Face east = CreativeMotorModel.baseHorizontalElements().get(0)
            .faces().get(CreativeMotorModel.Direction.EAST);

        assertEquals(CreativeMotorModel.Texture.MOTOR, north.texture());
        assertEquals(new CreativeMotorModel.Uv(0, 0, 10, 10), north.uv());
        assertEquals(CreativeMotorModel.Texture.CASING, east.texture());
        assertEquals(new CreativeMotorModel.Uv(3, 0, 13, 2), east.uv());
        assertEquals(90, east.rotation());
    }
}
