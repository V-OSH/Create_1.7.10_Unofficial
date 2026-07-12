package create.foundation.render;

import create.core.kinetic.KineticBlockType;
import create.core.kinetic.KineticTileEntity;
import create.shim.MyDirection;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

/**
 * ISBRH for all kinetic blocks.
 *
 * <p>Renders static (non-rotating) block geometry per {@link KineticBlockType}.
 * When {@code speed != 0} the TESR handles the rotating parts and the ISBRH
 * draws only the non-rotating housing (or nothing if the whole block rotates).</p>
 */
public class KineticRenderer implements ISimpleBlockRenderingHandler {

    private static final float PX = 1f / 16f;
    public static int RENDER_ID;

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId,
                                     RenderBlocks renderer) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.setNormal(0, -1, 0);
        renderer.renderFaceYNeg(block, 0, 0, 0, block.getIcon(0, metadata));
        t.setNormal(0, 1, 0);
        renderer.renderFaceYPos(block, 0, 0, 0, block.getIcon(1, metadata));
        t.setNormal(0, 0, -1);
        renderer.renderFaceZNeg(block, 0, 0, 0, block.getIcon(2, metadata));
        t.setNormal(0, 0, 1);
        renderer.renderFaceZPos(block, 0, 0, 0, block.getIcon(3, metadata));
        t.setNormal(-1, 0, 0);
        renderer.renderFaceXNeg(block, 0, 0, 0, block.getIcon(4, metadata));
        t.setNormal(1, 0, 0);
        renderer.renderFaceXPos(block, 0, 0, 0, block.getIcon(5, metadata));
        t.draw();
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
                                     Block block, int modelId, RenderBlocks renderer) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof KineticTileEntity)) {
            renderer.renderStandardBlock(block, x, y, z);
            return true;
        }

        KineticTileEntity kte = (KineticTileEntity) te;
        KineticBlockType type = kte.getKineticType();
        MyDirection.Axis axis = kte.getRotationAxis();
        float speed = kte.getSpeed();
        boolean rotating = Math.abs(speed) >= 0.01f;

        Tessellator t = Tessellator.instance;
        t.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
        int color = block.colorMultiplier(world, x, y, z);
        float r = (color >> 16 & 255) / 255f;
        float g = (color >> 8 & 255) / 255f;
        float b = (color & 255) / 255f;
        t.setColorOpaque_F(r, g, b);

        switch (type) {
            case SHAFT:
                if (!rotating) renderShaft(t, block, x, y, z, axis);
                break;
            case SMALL_COGWHEEL:
                renderCogwheel(t, block, x, y, z, axis, false, rotating);
                break;
            case LARGE_COGWHEEL:
                renderCogwheel(t, block, x, y, z, axis, true, rotating);
                break;
            case SOURCE:
                renderSource(t, block, x, y, z, axis, rotating);
                break;
            case BELT:
                renderBeltSlab(t, block, x, y, z);
                break;
            default:
                renderer.renderStandardBlock(block, x, y, z);
                break;
        }
        return true;
    }

    @Override public boolean shouldRender3DInInventory(int modelId) { return true; }
    @Override public int getRenderId() { return RENDER_ID; }

    // ========================================================================
    //  Per-type renderers
    // ========================================================================

    private static void renderShaft(Tessellator t, Block block, int x, int y, int z,
                                     MyDirection.Axis axis) {
        switch (axis) {
            case X: box(t, block, x, y, z, 0f, 5*PX, 5*PX, 1f, 11*PX, 11*PX); break;
            case Z: box(t, block, x, y, z, 5*PX, 5*PX, 0f, 11*PX, 11*PX, 1f); break;
            case Y: box(t, block, x, y, z, 5*PX, 0f, 5*PX, 11*PX, 1f, 11*PX); break;
        }
    }

    private static void renderCogwheel(Tessellator t, Block block, int x, int y, int z,
                                        MyDirection.Axis axis, boolean large, boolean rotating) {
        float d0 = large ? 0f : 2 * PX;
        float d1 = large ? 1f : 14 * PX;
        float discBot = 6 * PX;
        float discTop = 10 * PX;
        float sMin = 5 * PX;
        float sMax = 11 * PX;

        if (!rotating) {
            // Disc
            switch (axis) {
                case X: box(t, block, x, y, z, discBot, d0, d0, discTop, d1, d1); break;
                case Z: box(t, block, x, y, z, d0, d0, discBot, d1, d1, discTop); break;
                case Y: box(t, block, x, y, z, d0, discBot, d0, d1, discTop, d1); break;
            }
        }
        // Center shaft (always static in ISBRH)
        switch (axis) {
            case X: box(t, block, x, y, z, 0f, sMin, sMin, 1f, sMax, sMax); break;
            case Z: box(t, block, x, y, z, sMin, sMin, 0f, sMax, sMax, 1f); break;
            case Y: box(t, block, x, y, z, sMin, 0f, sMin, sMax, 1f, sMax); break;
        }
    }

    private static void renderSource(Tessellator t, Block block, int x, int y, int z,
                                      MyDirection.Axis axis, boolean rotating) {
        // Casing
        box(t, block, x, y, z, 1*PX, 1*PX, 1*PX, 15*PX, 15*PX, 15*PX);
        // Shaft stub (static if not rotating)
        if (!rotating) renderShaft(t, block, x, y, z, axis);
    }

    private static void renderBeltSlab(Tessellator t, Block block, int x, int y, int z) {
        box(t, block, x, y, z, 0f, 6*PX, 0f, 1f, 10*PX, 1f);
    }

    // ========================================================================
    //  Box with correct UV mapping
    // ========================================================================

    /** Draw a textured box with the full texture mapped to each face. */
    private static void box(Tessellator t, Block block, int x, int y, int z,
                             float minX, float minY, float minZ,
                             float maxX, float maxY, float maxZ) {
        double bx = x + minX, by = y + minY, bz = z + minZ;
        double tx = x + maxX, ty = y + maxY, tz = z + maxZ;

        IIcon top = icon(block, 1);
        IIcon bot = icon(block, 0);
        IIcon north = icon(block, 2);
        IIcon south = icon(block, 3);
        IIcon west = icon(block, 4);
        IIcon east = icon(block, 5);

        // Y- (bottom) — X spans U, Z spans V
        t.setNormal(0, -1, 0);
        v(t, bx, by, bz, bot, 0, 16);
        v(t, tx, by, bz, bot, 16, 16);
        v(t, tx, by, tz, bot, 16, 0);
        v(t, bx, by, tz, bot, 0, 0);

        // Y+ (top) — X spans U, Z spans V
        t.setNormal(0, 1, 0);
        v(t, bx, ty, bz, top, 0, 0);
        v(t, bx, ty, tz, top, 0, 16);
        v(t, tx, ty, tz, top, 16, 16);
        v(t, tx, ty, bz, top, 16, 0);

        // Z- (north) — X spans U, Y spans V
        t.setNormal(0, 0, -1);
        v(t, bx, by, bz, north, 0, 16);
        v(t, tx, by, bz, north, 16, 16);
        v(t, tx, ty, bz, north, 16, 0);
        v(t, bx, ty, bz, north, 0, 0);

        // Z+ (south) — X spans U, Y spans V
        t.setNormal(0, 0, 1);
        v(t, tx, by, tz, south, 0, 16);
        v(t, bx, by, tz, south, 16, 16);
        v(t, bx, ty, tz, south, 16, 0);
        v(t, tx, ty, tz, south, 0, 0);

        // X- (west) — Z spans U, Y spans V
        t.setNormal(-1, 0, 0);
        v(t, bx, by, tz, west, 0, 16);
        v(t, bx, by, bz, west, 16, 16);
        v(t, bx, ty, bz, west, 16, 0);
        v(t, bx, ty, tz, west, 0, 0);

        // X+ (east) — Z spans U, Y spans V
        t.setNormal(1, 0, 0);
        v(t, tx, by, bz, east, 0, 16);
        v(t, tx, by, tz, east, 16, 16);
        v(t, tx, ty, tz, east, 16, 0);
        v(t, tx, ty, bz, east, 0, 0);
    }

    private static IIcon icon(Block block, int side) {
        // Use getIcon(side, meta) — this method IS overridable (unlike the final getBlockTextureFromSide)
        IIcon ic = block.getIcon(side, 0);
        if (ic != null) return ic;
        // Fallback to stone so the face is visible rather than transparent
        return net.minecraft.init.Blocks.stone.getIcon(side, 0);
    }

    private static void v(Tessellator t, double x, double y, double z,
                           IIcon icon, float u, float v) {
        t.addVertexWithUV(x, y, z, icon.getInterpolatedU(u), icon.getInterpolatedV(v));
    }
}
