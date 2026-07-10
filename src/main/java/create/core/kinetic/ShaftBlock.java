package create.core.kinetic;

import create.shim.MyDirection;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * A shaft transmits rotational power along its axis.
 * Extends KineticBlock; the BFS network in Phase 2 will handle propagation.
 */
public class ShaftBlock extends KineticBlock {

    public ShaftBlock() {
        setBlockName("create:shaft");
        setBlockTextureName("create:shaft");
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
        te.setKineticType(KineticBlockType.SHAFT);
        te.setRotationAxis(MyDirection.Axis.Y);
        return te;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        // Phase 2: trigger network re-validation on break
        super.breakBlock(world, x, y, z, block, meta);
    }
}
