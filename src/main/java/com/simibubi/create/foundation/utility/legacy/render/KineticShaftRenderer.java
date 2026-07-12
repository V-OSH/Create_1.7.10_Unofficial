package com.simibubi.create.foundation.utility.legacy.render;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.foundation.utility.legacy.LegacyAxis;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import org.lwjgl.opengl.GL11;

public final class KineticShaftRenderer extends TileEntitySpecialRenderer {

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTicks) {
        if (!(tileEntity instanceof KineticBlockEntity kinetic) || kinetic.getWorldObj() == null) {
            return;
        }
        Block block = kinetic.getBlockType();
        if (!(block instanceof ShaftBlock) && !(block instanceof CogWheelBlock)) {
            return;
        }
        LegacyAxis axis = block instanceof ShaftBlock shaft ? shaft.getRotationAxis(kinetic.getBlockMetadata())
            : ((CogWheelBlock) block).getRotationAxis(kinetic.getBlockMetadata());
        float offset = block instanceof CogWheelBlock
            ? KineticRenderMath.cogwheelOffsetDegrees(axis, kinetic.xCoord, kinetic.yCoord, kinetic.zCoord) : 0;
        float angle = KineticRenderMath.angleDegrees(kinetic.getWorldObj().getTotalWorldTime(), partialTicks,
            kinetic.getSpeed(), offset);

        bindTexture(TextureMap.locationBlocksTexture);
        GL11.glPushMatrix();
        GL11.glTranslated(x + .5, y + .5, z + .5);
        orientYAxisTo(axis);
        GL11.glRotatef(angle, 0, 1, 0);
        GL11.glTranslatef(-.5f, -.5f, -.5f);
        GL11.glColor4f(1, 1, 1, 1);
        if (block instanceof ShaftBlock shaft) {
            renderShaft(kinetic, shaft);
        } else {
            int brightness = block.getMixedBrightnessForBlock(kinetic.getWorldObj(), kinetic.xCoord, kinetic.yCoord,
                kinetic.zCoord);
            CogWheelModelRenderer.render((CogWheelBlock) block, brightness);
        }
        GL11.glPopMatrix();
    }

    private static void orientYAxisTo(LegacyAxis axis) {
        switch (axis) {
            case X -> GL11.glRotatef(-90, 0, 0, 1);
            case Z -> GL11.glRotatef(90, 1, 0, 0);
            default -> {
            }
        }
    }

    private static void renderShaft(KineticBlockEntity kinetic, ShaftBlock block) {
        RenderBlocks renderer = new RenderBlocks();
        renderer.setRenderBounds(ShaftRenderGeometry.MIN, 0, ShaftRenderGeometry.MIN, ShaftRenderGeometry.MAX, 1,
            ShaftRenderGeometry.MAX);
        Tessellator tessellator = Tessellator.instance;
        int brightness = block.getMixedBrightnessForBlock(kinetic.getWorldObj(), kinetic.xCoord, kinetic.yCoord,
            kinetic.zCoord);
        IIcon side = block.getAxisIcon();
        IIcon cap = block.getAxisTopIcon();
        face(tessellator, brightness, 0, -1, 0, () -> renderer.renderFaceYNeg(block, 0, 0, 0, cap));
        face(tessellator, brightness, 0, 1, 0, () -> renderer.renderFaceYPos(block, 0, 0, 0, cap));
        face(tessellator, brightness, 0, 0, -1, () -> renderer.renderFaceZNeg(block, 0, 0, 0, side));
        face(tessellator, brightness, 0, 0, 1, () -> renderer.renderFaceZPos(block, 0, 0, 0, side));
        face(tessellator, brightness, -1, 0, 0, () -> renderer.renderFaceXNeg(block, 0, 0, 0, side));
        face(tessellator, brightness, 1, 0, 0, () -> renderer.renderFaceXPos(block, 0, 0, 0, side));
    }

    private static void face(Tessellator tessellator, int brightness, float x, float y, float z, Runnable render) {
        tessellator.startDrawingQuads();
        tessellator.setBrightness(brightness);
        tessellator.setColorOpaque_F(1, 1, 1);
        tessellator.setNormal(x, y, z);
        render.run();
        tessellator.draw();
    }
}
