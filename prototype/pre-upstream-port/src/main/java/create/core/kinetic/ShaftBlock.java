package create.core.kinetic;

import create.shim.MyDirection;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * A shaft transmits rotational power along its axis.
 * Extends KineticBlock; the BFS network in Phase 2 will handle propagation.
 */
public class ShaftBlock extends KineticBlock {

    public ShaftBlock() {
        setBlockName("create:shaft");
        setBlockTextureName("create:shaft");
        setHarvestLevel("pickaxe", 0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        super.registerBlockIcons(reg);
        registerTexture(reg, "shaft");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return iconMap.get("shaft");
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
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        setShaftBounds(axisFromMeta(world.getBlockMetadata(x, y, z)));
    }

    @Override
    public void setBlockBoundsForItemRender() {
        setShaftBounds(MyDirection.Axis.Y);
    }

    private void setShaftBounds(MyDirection.Axis axis) {
        switch (axis) {
            case X:
                setBlockBounds(0, 5 / 16f, 5 / 16f, 1, 11 / 16f, 11 / 16f);
                break;
            case Z:
                setBlockBounds(5 / 16f, 5 / 16f, 0, 11 / 16f, 11 / 16f, 1);
                break;
            case Y:
            default:
                setBlockBounds(5 / 16f, 0, 5 / 16f, 11 / 16f, 1, 11 / 16f);
                break;
        }
    }

    public static int metaFromAxis(MyDirection.Axis axis) {
        switch (axis) {
            case X: return 0;
            case Z: return 2;
            case Y:
            default: return 1;
        }
    }

    public static MyDirection.Axis axisFromMeta(int metadata) {
        switch (metadata & 3) {
            case 0: return MyDirection.Axis.X;
            case 2: return MyDirection.Axis.Z;
            case 1:
            default: return MyDirection.Axis.Y;
        }
    }

    @Override
    public int onBlockPlaced(World world, int x, int y, int z, int side,
                             float hitX, float hitY, float hitZ, int metadata) {
        return metaFromAxis(MyDirection.Axis.fromForge(ForgeDirection.getOrientation(side)));
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z,
                                EntityLivingBase placer, ItemStack stack) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof KineticTileEntity) {
            ((KineticTileEntity) te).setRotationAxis(
                    axisFromMeta(world.getBlockMetadata(x, y, z)));
        }
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        KineticTileEntity te = new KineticTileEntity();
        te.setKineticType(KineticBlockType.SHAFT);
        te.setRotationAxis(axisFromMeta(metadata));
        return te;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        // Phase 2: trigger network re-validation on break
        super.breakBlock(world, x, y, z, block, meta);
    }
}
