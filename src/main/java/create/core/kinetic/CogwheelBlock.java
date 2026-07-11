package create.core.kinetic;

import create.shim.MyDirection;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * A cogwheel transmits rotational power on a perpendicular axis to its shaft
 * connection point, allowing speed/rotation direction changes between adjacent
 * cogwheels.
 */
public class CogwheelBlock extends KineticBlock {

    private final boolean large;

    // Metadata encoding: bits 0-1 = axis (0=X, 1=Y, 2=Z)
    private static final int AXIS_MASK = 0x03;

    public CogwheelBlock(boolean large) {
        this.large = large;
        setBlockName(large ? "create:large_cogwheel" : "create:cogwheel");
        setBlockTextureName(large ? "create:large_cogwheel" : "create:cogwheel");
        setHarvestLevel("axe", 0);
        setHarvestLevel("pickaxe", 0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        String name = large ? "large_cogwheel" : "cogwheel";
        registerTexture(reg, name);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return iconMap.get(large ? "large_cogwheel" : "cogwheel");
    }

    // --- Multi-direction placement ---

    @Override
    public int onBlockPlaced(World world, int x, int y, int z, int side,
                              float hitX, float hitY, float hitZ, int metadata) {
        // Map clicked face to rotation axis
        // top/bottom → Y, east/west → X, north/south → Z
        MyDirection.Axis axis;
        switch (side) {
            case 0: case 1: // bottom, top
                axis = MyDirection.Axis.Y; break;
            case 4: case 5: // west, east
                axis = MyDirection.Axis.X; break;
            case 2: case 3: // north, south
                axis = MyDirection.Axis.Z; break;
            default:
                axis = MyDirection.Axis.Y;
        }
        return metaFromAxis(axis);
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

    // --- Collision box ---

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        MyDirection.Axis axis = axisFromMeta(world.getBlockMetadata(x, y, z));
        if (large) {
            // Large gear fills the block — full AABB is fine
            return AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1);
        }
        // Small gear: disc at center + shaft through axis
        switch (axis) {
            case X:
                return AxisAlignedBB.getBoundingBox(
                        x + 6/16f, y + 2/16f, z + 2/16f,
                        x + 10/16f, y + 14/16f, z + 14/16f);
            case Z:
                return AxisAlignedBB.getBoundingBox(
                        x + 2/16f, y + 6/16f, z + 6/16f,
                        x + 14/16f, y + 10/16f, z + 10/16f);
            case Y:
            default:
                return AxisAlignedBB.getBoundingBox(
                        x + 2/16f, y + 6/16f, z + 2/16f,
                        x + 14/16f, y + 10/16f, z + 14/16f);
        }
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        MyDirection.Axis axis = axisFromMeta(world.getBlockMetadata(x, y, z));
        if (large) {
            return AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1);
        }
        switch (axis) {
            case X:
                return AxisAlignedBB.getBoundingBox(
                        x + 2/16f, y + 2/16f, z + 2/16f,
                        x + 14/16f, y + 14/16f, z + 14/16f);
            case Z:
                return AxisAlignedBB.getBoundingBox(
                        x + 2/16f, y + 2/16f, z + 2/16f,
                        x + 14/16f, y + 14/16f, z + 14/16f);
            case Y:
            default:
                return AxisAlignedBB.getBoundingBox(
                        x + 2/16f, y + 2/16f, z + 2/16f,
                        x + 14/16f, y + 14/16f, z + 14/16f);
        }
    }

    // --- Meta/axis helpers ---

    public static int metaFromAxis(MyDirection.Axis axis) {
        switch (axis) {
            case X: return 0;
            case Z: return 2;
            case Y: default: return 1;
        }
    }

    public static MyDirection.Axis axisFromMeta(int metadata) {
        switch (metadata & AXIS_MASK) {
            case 0: return MyDirection.Axis.X;
            case 2: return MyDirection.Axis.Z;
            case 1: default: return MyDirection.Axis.Y;
        }
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
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        if (large) {
            setBlockBounds(0, 5 / 16f, 0, 1, 11 / 16f, 1);
        } else {
            setBlockBounds(1 / 16f, 6 / 16f, 1 / 16f, 15 / 16f, 10 / 16f, 15 / 16f);
        }
    }

    @Override
    public void setBlockBoundsForItemRender() {
        if (large) {
            setBlockBounds(0, 5 / 16f, 0, 1, 11 / 16f, 1);
        } else {
            setBlockBounds(1 / 16f, 6 / 16f, 1 / 16f, 15 / 16f, 10 / 16f, 15 / 16f);
        }
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        KineticTileEntity te = new KineticTileEntity();
        te.setKineticType(large ? KineticBlockType.LARGE_COGWHEEL : KineticBlockType.SMALL_COGWHEEL);
        te.setRotationAxis(axisFromMeta(metadata));
        return te;
    }
}
