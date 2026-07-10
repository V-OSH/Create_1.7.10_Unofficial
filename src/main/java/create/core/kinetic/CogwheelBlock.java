package create.core.kinetic;

import create.shim.MyDirection;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * A cogwheel transmits rotational power on a perpendicular axis to its shaft
 * connection point, allowing speed/rotation direction changes between adjacent
 * cogwheels.
 */
public class CogwheelBlock extends KineticBlock {

    private final boolean large;

    public CogwheelBlock(boolean large) {
        this.large = large;
        setBlockName(large ? "create:large_cogwheel" : "create:cogwheel");
        setBlockTextureName(large ? "create:large_cogwheel" : "create:cogwheel");
    }

    public boolean isLarge() {
        return large;
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public Class<? extends TileEntity> getTileEntityClass() {
        return KineticTileEntity.class;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        KineticTileEntity te = new KineticTileEntity();
        te.setKineticType(large ? KineticBlockType.LARGE_COGWHEEL : KineticBlockType.SMALL_COGWHEEL);
        te.setRotationAxis(MyDirection.Axis.Y);
        return te;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        super.breakBlock(world, x, y, z, block, meta);
    }
}
