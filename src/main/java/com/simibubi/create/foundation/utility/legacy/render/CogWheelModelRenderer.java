package com.simibubi.create.foundation.utility.legacy.render;

import java.util.Map;

import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.foundation.utility.legacy.render.CogWheelModel.Cuboid;
import com.simibubi.create.foundation.utility.legacy.render.CogWheelModel.Direction;
import com.simibubi.create.foundation.utility.legacy.render.CogWheelModel.Element;
import com.simibubi.create.foundation.utility.legacy.render.CogWheelModel.Face;
import com.simibubi.create.foundation.utility.legacy.render.CogWheelModel.Rotation;
import com.simibubi.create.foundation.utility.legacy.render.CogWheelModel.Uv;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;

public final class CogWheelModelRenderer {

    private CogWheelModelRenderer() {}

    public static void render(CogWheelBlock block, int brightness) {
        for (Element element : CogWheelModel.elements()) {
            for (Map.Entry<Direction, Face> entry : element.faces().entrySet()) {
                drawFace(block, brightness, rotate(vertices(element.bounds(), entry.getKey()), element.rotation()),
                    entry.getValue());
            }
        }
    }

    private static void drawFace(CogWheelBlock block, int brightness, double[][] vertices, Face face) {
        Tessellator tessellator = Tessellator.instance;
        double scale = CogWheelModel.textureCoordinateScale(face.texture());
        Uv faceUv = face.uv();
        double[][] uv = {{faceUv.minU() * scale, faceUv.maxV() * scale},
            {faceUv.maxU() * scale, faceUv.maxV() * scale}, {faceUv.maxU() * scale, faceUv.minV() * scale},
            {faceUv.minU() * scale, faceUv.minV() * scale}};
        int shift = Math.floorMod(face.rotation() / 90, 4);
        double[] normal = normal(vertices);
        IIcon icon = icon(block, face.texture());
        tessellator.startDrawingQuads();
        tessellator.setBrightness(brightness);
        tessellator.setColorOpaque_F(1, 1, 1);
        tessellator.setNormal((float) normal[0], (float) normal[1], (float) normal[2]);
        for (int index = 0; index < 4; index++) {
            double[] vertex = vertices[index];
            double[] texture = uv[(index + shift) % 4];
            tessellator.addVertexWithUV(vertex[0], vertex[1], vertex[2], icon.getInterpolatedU(texture[0]),
                icon.getInterpolatedV(texture[1]));
        }
        tessellator.draw();
    }

    private static IIcon icon(CogWheelBlock block, CogWheelModel.Texture texture) {
        return switch (texture) {
            case AXIS -> block.getAxisIcon();
            case AXIS_TOP -> block.getAxisTopIcon();
            case COGWHEEL -> block.getCogwheelIcon();
        };
    }

    private static double[][] rotate(double[][] vertices, Rotation rotation) {
        if (rotation.angle() == 0) {
            return vertices;
        }
        if (rotation.axis() != 'y') {
            throw new IllegalArgumentException("Unsupported cogwheel element rotation axis: " + rotation.axis());
        }
        double radians = Math.toRadians(rotation.angle());
        double cosine = Math.cos(radians);
        double sine = Math.sin(radians);
        double[][] rotated = new double[vertices.length][3];
        for (int index = 0; index < vertices.length; index++) {
            double x = vertices[index][0] - rotation.originX();
            double z = vertices[index][2] - rotation.originZ();
            rotated[index][0] = rotation.originX() + x * cosine + z * sine;
            rotated[index][1] = vertices[index][1];
            rotated[index][2] = rotation.originZ() - x * sine + z * cosine;
        }
        return rotated;
    }

    private static double[] normal(double[][] vertices) {
        double ax = vertices[1][0] - vertices[0][0];
        double ay = vertices[1][1] - vertices[0][1];
        double az = vertices[1][2] - vertices[0][2];
        double bx = vertices[2][0] - vertices[1][0];
        double by = vertices[2][1] - vertices[1][1];
        double bz = vertices[2][2] - vertices[1][2];
        double x = ay * bz - az * by;
        double y = az * bx - ax * bz;
        double z = ax * by - ay * bx;
        double length = Math.sqrt(x * x + y * y + z * z);
        return new double[] {x / length, y / length, z / length};
    }

    private static double[][] vertices(Cuboid box, Direction direction) {
        return switch (direction) {
            case DOWN -> new double[][] {{box.minX(), box.minY(), box.maxZ()}, {box.minX(), box.minY(), box.minZ()},
                {box.maxX(), box.minY(), box.minZ()}, {box.maxX(), box.minY(), box.maxZ()}};
            case UP -> new double[][] {{box.minX(), box.maxY(), box.minZ()}, {box.minX(), box.maxY(), box.maxZ()},
                {box.maxX(), box.maxY(), box.maxZ()}, {box.maxX(), box.maxY(), box.minZ()}};
            case NORTH -> new double[][] {{box.maxX(), box.minY(), box.minZ()},
                {box.minX(), box.minY(), box.minZ()}, {box.minX(), box.maxY(), box.minZ()},
                {box.maxX(), box.maxY(), box.minZ()}};
            case SOUTH -> new double[][] {{box.minX(), box.minY(), box.maxZ()},
                {box.maxX(), box.minY(), box.maxZ()}, {box.maxX(), box.maxY(), box.maxZ()},
                {box.minX(), box.maxY(), box.maxZ()}};
            case WEST -> new double[][] {{box.minX(), box.minY(), box.minZ()},
                {box.minX(), box.minY(), box.maxZ()}, {box.minX(), box.maxY(), box.maxZ()},
                {box.minX(), box.maxY(), box.minZ()}};
            case EAST -> new double[][] {{box.maxX(), box.minY(), box.maxZ()},
                {box.maxX(), box.minY(), box.minZ()}, {box.maxX(), box.maxY(), box.minZ()},
                {box.maxX(), box.maxY(), box.maxZ()}};
        };
    }
}
