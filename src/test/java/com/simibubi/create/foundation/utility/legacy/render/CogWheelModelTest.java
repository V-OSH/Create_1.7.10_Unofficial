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

    @Test
    void assignsUvCornersInVanillaBlockFaceUvOrder() {
        CogWheelModel.Face face = new CogWheelModel.Face(CogWheelModel.Texture.COGWHEEL,
            new CogWheelModel.Uv(7, 8, 16, 9.5), 0);
        double[][] corners = CogWheelModel.uvCorners(face, CogWheelModel.Direction.UP);

        assertCorner(corners[0], 7.5625, 8.09375);
        assertCorner(corners[1], 7.5625, 9.40625);
        assertCorner(corners[2], 15.4375, 9.40625);
        assertCorner(corners[3], 15.4375, 8.09375);
    }

    @Test
    void insetsOnlyBrownSideFacesByOneSourcePixel() {
        CogWheelModel.Face face = new CogWheelModel.Face(CogWheelModel.Texture.COGWHEEL,
            new CogWheelModel.Uv(7, 8, 16, 9.5), 0);
        double[][] side = CogWheelModel.uvCorners(face, CogWheelModel.Direction.NORTH);
        double[][] top = CogWheelModel.uvCorners(face, CogWheelModel.Direction.UP);
        CogWheelModel.Face axisFace = new CogWheelModel.Face(CogWheelModel.Texture.AXIS,
            new CogWheelModel.Uv(6, 0, 10, 16), 0);
        double[][] axisSide = CogWheelModel.uvCorners(axisFace, CogWheelModel.Direction.NORTH);

        assertCorner(side[0], 8.0625, 8.59375);
        assertCorner(side[2], 14.9375, 8.90625);
        assertCorner(top[0], 7.5625, 8.09375);
        assertCorner(top[2], 15.4375, 9.40625);
        assertCorner(axisSide[0], 6.5, 2);
        assertCorner(axisSide[2], 9.5, 14);
    }

    @Test
    void ordersSideVerticesLikeVanillaFaceInfo() {
        CogWheelModel.Cuboid box = new CogWheelModel.Cuboid(1, 2, 3, 4, 5, 6);

        assertVertex(CogWheelModelRenderer.vertices(box, CogWheelModel.Direction.NORTH)[0], 4, 5, 3);
        assertVertex(CogWheelModelRenderer.vertices(box, CogWheelModel.Direction.SOUTH)[0], 1, 5, 6);
        assertVertex(CogWheelModelRenderer.vertices(box, CogWheelModel.Direction.WEST)[0], 1, 5, 3);
        assertVertex(CogWheelModelRenderer.vertices(box, CogWheelModel.Direction.EAST)[0], 4, 5, 6);
    }

    private static void assertCorner(double[] corner, double expectedU, double expectedV) {
        assertEquals(expectedU, corner[0], 0.0001);
        assertEquals(expectedV, corner[1], 0.0001);
    }

    private static void assertVertex(double[] vertex, double expectedX, double expectedY, double expectedZ) {
        assertEquals(expectedX, vertex[0], 0.0001);
        assertEquals(expectedY, vertex[1], 0.0001);
        assertEquals(expectedZ, vertex[2], 0.0001);
    }
}
