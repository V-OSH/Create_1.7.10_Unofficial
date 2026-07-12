package create.core.kinetic.source;

import create.core.kinetic.KineticBlock;
import create.shim.MyDirection;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Small water wheel — generates rotational power when adjacent to
 * moving water (or lava).
 *
 * <p>Placed with a horizontal axis (X or Z). Has shaft connections on
 * BOTH ends along the axis, allowing power extraction from either side.</p>
 *
 * <p>Metadata: 0 = X axis, 1 = Z axis.</p>
 */
public class WaterWheelBlock extends KineticBlock {

    public WaterWheelBlock() {
        setBlockName("create:water_wheel");
        setBlockTextureName("create:water_wheel");
        setHarvestLevel("axe", 0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        super.registerBlockIcons(reg);
        registerTexture(reg, "water_wheel");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return iconMap.get("water_wheel");
    }

    @Override
    public Class<? extends TileEntity> getTileEntityClass() {
        return WaterWheelTileEntity.class;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        setWaterWheelBounds(world.getBlockMetadata(x, y, z));
    }

    @Override
    public void setBlockBoundsForItemRender() {
        setWaterWheelBounds(0);
    }

    private void setWaterWheelBounds(int metadata) {
        if ((metadata & 1) == 0) {
            setBlockBounds(0, 1 / 16f, 1 / 16f, 1, 15 / 16f, 15 / 16f);
        } else {
            setBlockBounds(1 / 16f, 1 / 16f, 0, 15 / 16f, 15 / 16f, 1);
        }
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        WaterWheelTileEntity te = new WaterWheelTileEntity();
        MyDirection.Axis axis = (metadata & 1) == 0 ? MyDirection.Axis.X : MyDirection.Axis.Z;
        te.setRotationAxis(axis);
        return te;
    }

    // --- Placement ---

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z,
                                 EntityLivingBase placer, ItemStack stack) {
        // Determine axis from player's facing direction (horizontal only)
        int facing = net.minecraft.util.MathHelper
                .floor_double((double) (placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        // facing: 0=south, 1=west, 2=north, 3=east
        // Map to wheel axis: player facing south/north → wheel on Z axis
        //                       player facing east/west → wheel on X axis
        int meta = (facing == 1 || facing == 3) ? 1 : 0; // 1=Z axis, 0=X axis
        world.setBlockMetadataWithNotify(x, y, z, meta, 2);

        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof WaterWheelTileEntity) {
            MyDirection.Axis axis = (meta == 0) ? MyDirection.Axis.X : MyDirection.Axis.Z;
            ((WaterWheelTileEntity) te).setRotationAxis(axis);
        }
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        // Recalculate initial flow score after TE is ready
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof WaterWheelTileEntity) {
                int meta = world.getBlockMetadata(x, y, z);
                MyDirection.Axis axis = (meta & 1) == 0 ? MyDirection.Axis.X : MyDirection.Axis.Z;
                ((WaterWheelTileEntity) te).setRotationAxis(axis);
                ((WaterWheelTileEntity) te).determineAndApplyFlowScore();
            }
        }
        super.onBlockAdded(world, x, y, z);
    }

    // --- Neighbor change: re-check water/lava ---

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        super.onNeighborBlockChange(world, x, y, z, neighbor);

        // When any adjacent block changes, re-check flow
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof WaterWheelTileEntity) {
                // Schedule next tick so the TE is ready after connection re-validation
                world.scheduleBlockUpdate(x, y, z, this, 1);
            }
        }
    }

    @Override
    public void updateTick(World world, int x, int y, int z, java.util.Random random) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof WaterWheelTileEntity) {
                ((WaterWheelTileEntity) te).determineAndApplyFlowScore();
            }
        }
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }
}
