package com.simibubi.create.foundation.utility.legacy.render;

import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

public final class CogWheelBlockRenderer implements ISimpleBlockRenderingHandler {

    private final int renderId;

    public CogWheelBlockRenderer(int renderId) {
        this.renderId = renderId;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        GL11.glTranslatef(-.5f, -.5f, -.5f);
        CogWheelModelRenderer.render((CogWheelBlock) block, 15728880);
        GL11.glTranslatef(.5f, .5f, .5f);
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
}
