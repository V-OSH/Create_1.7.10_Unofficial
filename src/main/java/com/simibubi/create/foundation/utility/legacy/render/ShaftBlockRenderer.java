package com.simibubi.create.foundation.utility.legacy.render;

import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

public final class ShaftBlockRenderer implements ISimpleBlockRenderingHandler {

    private final int renderId;

    public ShaftBlockRenderer(int renderId) {
        this.renderId = renderId;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        ShaftBlock shaft = (ShaftBlock) block;
        renderer.setRenderBounds(ShaftRenderGeometry.MIN, 0, ShaftRenderGeometry.MIN, ShaftRenderGeometry.MAX, 1,
            ShaftRenderGeometry.MAX);
        GL11.glTranslatef(-0.5f, -0.5f, -0.5f);
        draw(renderer, shaft, shaft.getAxisIcon(), shaft.getAxisTopIcon());
        GL11.glTranslatef(0.5f, 0.5f, 0.5f);
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
        RenderBlocks renderer) {
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

    private static void draw(RenderBlocks renderer, Block block, IIcon side, IIcon cap) {
        Tessellator tessellator = Tessellator.instance;
        face(tessellator, 0, -1, 0, () -> renderer.renderFaceYNeg(block, 0, 0, 0, cap));
        face(tessellator, 0, 1, 0, () -> renderer.renderFaceYPos(block, 0, 0, 0, cap));
        face(tessellator, 0, 0, -1, () -> renderer.renderFaceZNeg(block, 0, 0, 0, side));
        face(tessellator, 0, 0, 1, () -> renderer.renderFaceZPos(block, 0, 0, 0, side));
        face(tessellator, -1, 0, 0, () -> renderer.renderFaceXNeg(block, 0, 0, 0, side));
        face(tessellator, 1, 0, 0, () -> renderer.renderFaceXPos(block, 0, 0, 0, side));
    }

    private static void face(Tessellator tessellator, float x, float y, float z, Runnable render) {
        tessellator.startDrawingQuads();
        tessellator.setNormal(x, y, z);
        render.run();
        tessellator.draw();
    }
}
