package com.simibubi.create.foundation.utility.legacy.render;

import com.simibubi.create.content.kinetics.motor.CreativeMotorBlock;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

public final class CreativeMotorRenderer extends TileEntitySpecialRenderer {

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTicks) {
        if (!(tileEntity instanceof CreativeMotorBlockEntity motor) || motor.getWorldObj() == null) {
            return;
        }
        Block block = motor.getBlockType();
        if (!(block instanceof CreativeMotorBlock motorBlock)) {
            return;
        }

        ForgeDirection facing = motorBlock.getFacing(motor.getBlockMetadata());
        float angle = KineticRenderMath.angleDegrees(motor.getWorldObj().getTotalWorldTime(), partialTicks,
            motor.getGeneratedSpeed());

        bindTexture(TextureMap.locationBlocksTexture);
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        GL11.glTranslatef(0.5f, 0.5f, 0.5f);
        orientSouthAxisTo(facing);
        GL11.glRotatef(angle, 0, 0, 1);
        GL11.glTranslatef(-0.5f, -0.5f, -0.5f);
        GL11.glColor4f(1, 1, 1, 1);
        renderShaft(motor, motorBlock);
        GL11.glPopMatrix();
    }

    private static void orientSouthAxisTo(ForgeDirection facing) {
        switch (facing) {
            case NORTH -> GL11.glRotatef(180, 0, 1, 0);
            case EAST -> GL11.glRotatef(90, 0, 1, 0);
            case WEST -> GL11.glRotatef(-90, 0, 1, 0);
            case UP -> GL11.glRotatef(-90, 1, 0, 0);
            case DOWN -> GL11.glRotatef(90, 1, 0, 0);
            default -> {
            }
        }
    }

    private static void renderShaft(CreativeMotorBlockEntity motor, CreativeMotorBlock block) {
        int brightness = block.getMixedBrightnessForBlock(motor.getWorldObj(), motor.xCoord, motor.yCoord,
            motor.zCoord);
        IIcon side = block.getAxisIcon();
        IIcon end = block.getAxisTopIcon();
        double min = ShaftRenderGeometry.MIN;
        double max = ShaftRenderGeometry.MAX;
        double start = ShaftRenderGeometry.HALF_START;
        double finish = ShaftRenderGeometry.HALF_END;
        drawFace(brightness, 0, 0, -1, end, 6, 6, 10, 10, 180,
            new double[][] {{max, min, start}, {min, min, start}, {min, max, start}, {max, max, start}});
        drawFace(brightness, 0, 0, 1, end, 6, 6, 10, 10, 0,
            new double[][] {{min, min, finish}, {max, min, finish}, {max, max, finish}, {min, max, finish}});
        drawFace(brightness, 1, 0, 0, side, 6, 0, 10, 8, 270,
            new double[][] {{max, min, finish}, {max, min, start}, {max, max, start}, {max, max, finish}});
        drawFace(brightness, -1, 0, 0, side, 6, 0, 10, 8, 90,
            new double[][] {{min, min, start}, {min, min, finish}, {min, max, finish}, {min, max, start}});
        drawFace(brightness, 0, 1, 0, side, 6, 0, 10, 8, 180,
            new double[][] {{min, max, start}, {min, max, finish}, {max, max, finish}, {max, max, start}});
        drawFace(brightness, 0, -1, 0, side, 6, 0, 10, 8, 0,
            new double[][] {{min, min, finish}, {min, min, start}, {max, min, start}, {max, min, finish}});
    }

    private static void drawFace(int brightness, float x, float y, float z, IIcon icon, double minU, double minV,
        double maxU, double maxV, int rotation, double[][] vertices) {
        Tessellator tessellator = Tessellator.instance;
        double[][] uv = {{minU, maxV}, {maxU, maxV}, {maxU, minV}, {minU, minV}};
        int shift = Math.floorMod(rotation / 90, 4);
        tessellator.startDrawingQuads();
        tessellator.setBrightness(brightness);
        tessellator.setColorOpaque_F(1, 1, 1);
        tessellator.setNormal(x, y, z);
        for (int index = 0; index < 4; index++) {
            double[] vertex = vertices[index];
            double[] texture = uv[(index + shift) % 4];
            tessellator.addVertexWithUV(vertex[0], vertex[1], vertex[2], icon.getInterpolatedU(texture[0]),
                icon.getInterpolatedV(texture[1]));
        }
        tessellator.draw();
    }
}
