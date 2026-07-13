package com.simibubi.create.foundation.utility.legacy.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.Test;

class CogWheelModelTest {

    @Test
    void preservesTheUpstreamSevenElementGearModel() {
        assertEquals(7, CogWheelModel.elements().size());
        assertEquals(45, CogWheelModel.elements().get(2).rotation().angle(), 0.0001);
        assertEquals(-45, CogWheelModel.elements().get(3).rotation().angle(), 0.0001);
    }

    @Test
    void preservesTheUpstreamLargeCogwheelModel() {
        assertEquals(16, CogWheelModel.elements(true).size());
        assertEquals(45, CogWheelModel.elements(true).get(0).rotation().angle(), 0.0001);
        assertEquals(22.5, CogWheelModel.elements(true).get(2).rotation().angle(), 0.0001);
        assertEquals(-7.0 / 16, CogWheelModel.elements(true).get(2).bounds().minX(), 0.0001);
        assertEquals(23.0 / 16, CogWheelModel.elements(true).get(2).bounds().maxX(), 0.0001);
    }

    @Test
    void bundlesTheExactUpstreamCogwheelTextures() throws IOException, NoSuchAlgorithmException {
        assertEquals("011591a34282037145fa38a5970b5b38e1c31d6b9f8a823b4598daa7e79c5b08",
            sha256("/assets/create/textures/blocks/cogwheel.png"));
        assertEquals("fdea9c2c9cb758ad27170dce94ca4adb73b187dbe89098ec81264b4c0cdd48ac",
            sha256("/assets/create/textures/blocks/large_cogwheel.png"));
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
        double[][] corners = CogWheelModel.uvCorners(face);

        assertCorner(corners[0], 7.5625, 8.09375);
        assertCorner(corners[1], 7.5625, 9.40625);
        assertCorner(corners[2], 15.4375, 9.40625);
        assertCorner(corners[3], 15.4375, 8.09375);
    }

    @Test
    void keepsSideFaceUvsIdenticalToUpstreamFaceBakery() {
        CogWheelModel.Face face = new CogWheelModel.Face(CogWheelModel.Texture.COGWHEEL,
            new CogWheelModel.Uv(7, 8, 16, 9.5), 0);
        double[][] side = CogWheelModel.uvCorners(face);
        double[][] top = CogWheelModel.uvCorners(face);
        CogWheelModel.Face axisFace = new CogWheelModel.Face(CogWheelModel.Texture.AXIS,
            new CogWheelModel.Uv(6, 0, 10, 16), 0);
        double[][] axisSide = CogWheelModel.uvCorners(axisFace);

        assertCorner(side[0], 7.5625, 8.09375);
        assertCorner(side[2], 15.4375, 9.40625);
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

    private static String sha256(String resource) throws IOException, NoSuchAlgorithmException {
        try (InputStream stream = CogWheelModelTest.class.getResourceAsStream(resource)) {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(stream.readAllBytes()));
        }
    }
}
