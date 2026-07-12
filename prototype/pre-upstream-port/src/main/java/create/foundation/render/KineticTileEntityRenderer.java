package create.foundation.render;

import create.core.kinetic.KineticBlockType;
import create.core.kinetic.KineticTileEntity;
import create.shim.MyDirection;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import org.lwjgl.opengl.GL11;

/**
 * TESR for dynamically rotating kinetic blocks.
 *
 * <p>Receives the interpolated angle from {@link KineticTileEntity}
 * and renders the rotating portion via GL rotate transforms.</p>
 */
public class KineticTileEntityRenderer extends TileEntitySpecialRenderer {

    private static final float PX = 1f / 16f;

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z,
                                    float partialTicks) {
        if (!(te instanceof KineticTileEntity)) return;
        KineticTileEntity kte = (KineticTileEntity) te;
        float speed = kte.getSpeed();
        if (Math.abs(speed) < 0.01f) return;

        Block block = kte.getBlockType();
        KineticBlockType type = kte.getKineticType();
        MyDirection.Axis axis = kte.getRotationAxis();

        float renderAngle = kte.getRenderAngle(partialTicks);

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
        double degAngle = Math.toDegrees(renderAngle);
        switch (axis) {
            case X: GL11.glRotated(degAngle, 1, 0, 0); break;
            case Y: GL11.glRotated(degAngle, 0, 1, 0); break;
            case Z: GL11.glRotated(degAngle, 0, 0, 1); break;
        }
        GL11.glTranslated(-0.5, -0.5, -0.5);

        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.setBrightness(block.getMixedBrightnessForBlock(kte.getWorldObj(),
                (int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z)));

        switch (type) {
            case SHAFT:
            case SMALL_COGWHEEL:
            case LARGE_COGWHEEL:
                renderRotated(t, block, type, axis);
                break;
            case SOURCE:
                renderRotatedSourcePart(t, block, axis);
                break;
            default:
                break;
        }

        t.draw();
        GL11.glPopMatrix();
    }

    private static void renderRotated(Tessellator t, Block block,
                                       KineticBlockType type, MyDirection.Axis axis) {
        boolean large = type == KineticBlockType.LARGE_COGWHEEL;
        float d0 = large ? 0f : 2 * PX;
        float d1 = large ? 1f : 14 * PX;
        float discBot = 6 * PX;
        float discTop = 10 * PX;

        if (type == KineticBlockType.SHAFT) {
            switch (axis) {
                case X: box(t, block, 0f, 5*PX, 5*PX, 1f, 11*PX, 11*PX); break;
                case Z: box(t, block, 5*PX, 5*PX, 0f, 11*PX, 11*PX, 1f); break;
                case Y: box(t, block, 5*PX, 0f, 5*PX, 11*PX, 1f, 11*PX); break;
            }
        } else {
            switch (axis) {
                case X: box(t, block, discBot, d0, d0, discTop, d1, d1); break;
                case Z: box(t, block, d0, d0, discBot, d1, d1, discTop); break;
                case Y: box(t, block, d0, discBot, d0, d1, discTop, d1); break;
            }
        }
    }

    private static void renderRotatedSourcePart(Tessellator t, Block block,
                                                  MyDirection.Axis axis) {
        // Draw the rotating wheel-like element
        switch (axis) {
            case X: box(t, block, 4*PX, 0f, 0f, 6*PX, 1f, 1f); break;
            case Z: box(t, block, 0f, 0f, 4*PX, 1f, 1f, 6*PX); break;
            case Y: box(t, block, 0f, 4*PX, 0f, 1f, 6*PX, 1f); break;
        }
    }

    // ========================================================================
    //  Box with full-texture UV mapping (same as ISBRH)
    // ========================================================================

    private static void box(Tessellator t, Block block,
                             float minX, float minY, float minZ,
                             float maxX, float maxY, float maxZ) {
        IIcon top = icon(block, 1), bot = icon(block, 0);
        IIcon north = icon(block, 2), south = icon(block, 3);
        IIcon west = icon(block, 4), east = icon(block, 5);

        // Y-
        t.setNormal(0, -1, 0);
        v(t, minX, minY, minZ, bot, 0, 16);
        v(t, maxX, minY, minZ, bot, 16, 16);
        v(t, maxX, minY, maxZ, bot, 16, 0);
        v(t, minX, minY, maxZ, bot, 0, 0);
        // Y+
        t.setNormal(0, 1, 0);
        v(t, minX, maxY, minZ, top, 0, 0);
        v(t, minX, maxY, maxZ, top, 0, 16);
        v(t, maxX, maxY, maxZ, top, 16, 16);
        v(t, maxX, maxY, minZ, top, 16, 0);
        // Z-
        t.setNormal(0, 0, -1);
        v(t, minX, minY, minZ, north, 0, 16);
        v(t, maxX, minY, minZ, north, 16, 16);
        v(t, maxX, maxY, minZ, north, 16, 0);
        v(t, minX, maxY, minZ, north, 0, 0);
        // Z+
        t.setNormal(0, 0, 1);
        v(t, maxX, minY, maxZ, south, 0, 16);
        v(t, minX, minY, maxZ, south, 16, 16);
        v(t, minX, maxY, maxZ, south, 16, 0);
        v(t, maxX, maxY, maxZ, south, 0, 0);
        // X-
        t.setNormal(-1, 0, 0);
        v(t, minX, minY, maxZ, west, 0, 16);
        v(t, minX, minY, minZ, west, 16, 16);
        v(t, minX, maxY, minZ, west, 16, 0);
        v(t, minX, maxY, maxZ, west, 0, 0);
        // X+
        t.setNormal(1, 0, 0);
        v(t, maxX, minY, minZ, east, 0, 16);
        v(t, maxX, minY, maxZ, east, 16, 16);
        v(t, maxX, maxY, maxZ, east, 16, 0);
        v(t, maxX, maxY, minZ, east, 0, 0);
    }

    private static IIcon icon(Block block, int side) {
        IIcon ic = block.getIcon(side, 0);
        if (ic != null) return ic;
        return net.minecraft.init.Blocks.stone.getIcon(side, 0);
    }

    private static void v(Tessellator t, double x, double y, double z,
                           IIcon icon, float u, float v) {
        t.addVertexWithUV(x, y, z, icon.getInterpolatedU(u), icon.getInterpolatedV(v));
    }
}
