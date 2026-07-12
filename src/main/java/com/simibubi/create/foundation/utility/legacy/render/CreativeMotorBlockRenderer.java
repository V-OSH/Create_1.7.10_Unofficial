package com.simibubi.create.foundation.utility.legacy.render;

import java.util.Map;

import com.simibubi.create.content.kinetics.motor.CreativeMotorBlock;
import com.simibubi.create.foundation.utility.legacy.render.CreativeMotorModel.Cuboid;
import com.simibubi.create.foundation.utility.legacy.render.CreativeMotorModel.Direction;
import com.simibubi.create.foundation.utility.legacy.render.CreativeMotorModel.Element;
import com.simibubi.create.foundation.utility.legacy.render.CreativeMotorModel.Face;
import com.simibubi.create.foundation.utility.legacy.render.CreativeMotorModel.Uv;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

public final class CreativeMotorBlockRenderer implements ISimpleBlockRenderingHandler {

    private final int renderId;

    public CreativeMotorBlockRenderer(int renderId) {
        this.renderId = renderId;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        CreativeMotorBlock motor = (CreativeMotorBlock) block;
        GL11.glTranslatef(-0.5f, -0.5f, -0.5f);
        for (Element element : CreativeMotorModel.baseHorizontalElements()) {
            for (Map.Entry<Direction, Face> entry : element.faces().entrySet()) {
                Tessellator tessellator = Tessellator.instance;
                Direction direction = entry.getKey();
                tessellator.startDrawingQuads();
                tessellator.setNormal(directionX(direction), directionY(direction), directionZ(direction));
                renderFace(tessellator, motor, element.bounds(), direction, entry.getValue(), ForgeDirection.SOUTH,
                    ForgeDirection.SOUTH, 0, 0, 0);
                tessellator.draw();
            }
        }
        GL11.glTranslatef(0.5f, 0.5f, 0.5f);
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
        RenderBlocks renderer) {
        CreativeMotorBlock motor = (CreativeMotorBlock) block;
        ForgeDirection facing = motor.getFacing(world.getBlockMetadata(x, y, z));
        ForgeDirection baseFacing = CreativeMotorModel.baseFacingFor(facing);
        Tessellator tessellator = Tessellator.instance;
        tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
        for (Element element : CreativeMotorModel.baseElementsFor(facing)) {
            for (Map.Entry<Direction, Face> entry : element.faces().entrySet()) {
                Direction worldDirection = CreativeMotorModel.transformDirection(entry.getKey(), baseFacing, facing);
                float shade = shade(worldDirection);
                tessellator.setColorOpaque_F(shade, shade, shade);
                renderFace(tessellator, motor, element.bounds(), entry.getKey(), entry.getValue(), baseFacing, facing,
                    x, y, z);
            }
        }
        return true;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }

    @Override
    public int getRenderId() {
        return renderId;
    }

    private static void renderFace(Tessellator tessellator, CreativeMotorBlock motor, Cuboid bounds,
        Direction direction, Face face, ForgeDirection sourceFacing, ForgeDirection targetFacing, double offsetX,
        double offsetY, double offsetZ) {
        double[][] vertices = vertices(bounds, direction);
        double[][] uv = uv(face.uv(), face.rotation());
        IIcon icon = iconFor(motor, face.texture());
        for (int index = 0; index < 4; index++) {
            double[] point = CreativeMotorModel.transformPoint(vertices[index][0], vertices[index][1],
                vertices[index][2], sourceFacing, targetFacing);
            tessellator.addVertexWithUV(offsetX + point[0], offsetY + point[1], offsetZ + point[2],
                icon.getInterpolatedU(uv[index][0]), icon.getInterpolatedV(uv[index][1]));
        }
    }

    private static double[][] uv(Uv uv, int rotation) {
        double[][] corners = {
            {uv.minU(), uv.maxV()}, {uv.maxU(), uv.maxV()}, {uv.maxU(), uv.minV()}, {uv.minU(), uv.minV()}
        };
        double[][] rotated = new double[4][2];
        int shift = Math.floorMod(rotation / 90, 4);
        for (int index = 0; index < 4; index++) {
            rotated[index] = corners[(index + shift) % 4];
        }
        return rotated;
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

    private static IIcon iconFor(CreativeMotorBlock motor, CreativeMotorModel.Texture texture) {
        return switch (texture) {
            case CASING -> motor.getCasingIcon();
            case MOTOR -> motor.getMotorIcon();
            case AXIS -> motor.getAxisIcon();
            case FLAP -> motor.getFlapIcon();
        };
    }

    private static float shade(Direction direction) {
        return switch (direction) {
            case DOWN -> .5f;
            case UP -> 1;
            case NORTH, SOUTH -> .8f;
            case WEST, EAST -> .6f;
        };
    }

    private static float directionX(Direction direction) {
        return switch (direction) {
            case WEST -> -1;
            case EAST -> 1;
            default -> 0;
        };
    }

    private static float directionY(Direction direction) {
        return switch (direction) {
            case DOWN -> -1;
            case UP -> 1;
            default -> 0;
        };
    }

    private static float directionZ(Direction direction) {
        return switch (direction) {
            case NORTH -> -1;
            case SOUTH -> 1;
            default -> 0;
        };
    }
}
