package create.core.kinetic.belt;

import create.core.kinetic.belt.transport.TransportedItemStack;
import create.shim.MyBlockPos;
import create.shim.MyVec3;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Static utilities for belt positions and lookups.
 */
public final class BeltHelper {

    private BeltHelper() {}

    /**
     * Get the BeltTileEntity at a position, or null.
     */
    public static BeltTileEntity getSegmentBE(net.minecraft.world.World world,
                                               int x, int y, int z) {
        if (world == null) return null;
        net.minecraft.tileentity.TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof BeltTileEntity) return (BeltTileEntity) te;
        return null;
    }

    /**
     * Get the controller BeltTileEntity for a belt segment.
     */
    public static BeltTileEntity getControllerBE(net.minecraft.world.World world,
                                                   MyBlockPos segmentPos) {
        BeltTileEntity segment = getSegmentBE(world, segmentPos.getX(),
                segmentPos.getY(), segmentPos.getZ());
        if (segment == null) return null;
        return segment.getControllerBE();
    }

    /**
     * Get the world BlockPos at a given belt offset (integer index).
     * For HORIZONTAL belts: controller position + facing * offset.
     */
    public static MyBlockPos getPositionForOffset(BeltTileEntity controller, int offset) {
        ForgeDirection facing = controller.getBeltFacing();
        MyBlockPos base = controller.getKineticPos();
        return base.add(facing.offsetX * offset, facing.offsetY * offset, facing.offsetZ * offset);
    }

    /**
     * Get the world-space Vec3 position for a given belt offset (float).
     */
    public static MyVec3 getVectorForOffset(BeltTileEntity controller, float offset,
                                             float partialTicks) {
        ForgeDirection facing = controller.getBeltFacing();
        MyBlockPos base = controller.getControllerBE().getKineticPos();

        double x = base.getX() + 0.5 + facing.offsetX * offset;
        double y = base.getY() + 10.0 / 16.0; // belt surface height
        double z = base.getZ() + 0.5 + facing.offsetZ * offset;
        return new MyVec3(x, y, z);
    }

    /**
     * Convert an entity's local position inside a belt segment into an offset
     * along the whole belt chain.
     */
    public static float getBeltPositionForSegment(int segmentIndex, ForgeDirection facing,
                                                   double localX, double localZ) {
        double along = facing.offsetX != 0 ? localX : localZ;
        if (facing.offsetX < 0 || facing.offsetZ < 0) {
            along = 1.0 - along;
        }
        return segmentIndex + (float) Math.max(0.0, Math.min(1.0, along));
    }
}
