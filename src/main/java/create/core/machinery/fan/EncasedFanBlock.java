package create.core.machinery.fan;

import create.core.kinetic.KineticBlock;
import create.core.kinetic.KineticBlockType;
import create.shim.MyDirection;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Encased Fan — produces an air current in its facing direction when powered.
 * Speed sign determines push (positive) vs pull (negative). Air current
 * processes items based on catalyst blocks.
 */
public class EncasedFanBlock extends KineticBlock {

    public EncasedFanBlock() {
        setBlockName("create:encased_fan");
        setBlockTextureName("create:encased_fan");
        setHarvestLevel("axe", 0);
        setHarvestLevel("pickaxe", 1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        registerTexture(reg, "encased_fan");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return iconMap.get("encased_fan");
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public Class<? extends TileEntity> getTileEntityClass() {
        return EncasedFanTileEntity.class;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        setFanBounds(world.getBlockMetadata(x, y, z));
    }

    @Override
    public void setBlockBoundsForItemRender() {
        setBlockBounds(1 / 16f, 1 / 16f, 1 / 16f, 15 / 16f, 15 / 16f, 15 / 16f);
    }

    private void setFanBounds(int metadata) {
        ForgeDirection facing = ForgeDirection.getOrientation(metadata & 7);
        switch (facing) {
            case DOWN: setBlockBounds(1 / 16f, 0, 1 / 16f, 15 / 16f, 15 / 16f, 15 / 16f); break;
            case UP: setBlockBounds(1 / 16f, 1 / 16f, 1 / 16f, 15 / 16f, 1, 15 / 16f); break;
            case NORTH: setBlockBounds(1 / 16f, 1 / 16f, 0, 15 / 16f, 15 / 16f, 15 / 16f); break;
            case SOUTH: setBlockBounds(1 / 16f, 1 / 16f, 1 / 16f, 15 / 16f, 15 / 16f, 1); break;
            case WEST: setBlockBounds(0, 1 / 16f, 1 / 16f, 15 / 16f, 15 / 16f, 15 / 16f); break;
            case EAST: setBlockBounds(1 / 16f, 1 / 16f, 1 / 16f, 1, 15 / 16f, 15 / 16f); break;
            default: setBlockBounds(1 / 16f, 1 / 16f, 1 / 16f, 15 / 16f, 15 / 16f, 15 / 16f);
        }
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        EncasedFanTileEntity te = new EncasedFanTileEntity();
        te.setKineticType(KineticBlockType.SHAFT);
        te.setRotationAxis(MyDirection.Axis.Y);
        return te;
    }

    // --- Directional placement ---

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z,
                                EntityLivingBase placer, ItemStack stack) {
        int facing = determineFacing(placer);
        world.setBlockMetadataWithNotify(x, y, z, facing, 2);
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof EncasedFanTileEntity) {
            ((EncasedFanTileEntity) te).updateFromMetadata(facing);
        }
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof EncasedFanTileEntity) {
            int meta = world.getBlockMetadata(x, y, z);
            ((EncasedFanTileEntity) te).updateFromMetadata(meta);
        }
        super.onBlockAdded(world, x, y, z);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z,
                                      net.minecraft.block.Block neighbor) {
        super.onNeighborBlockChange(world, x, y, z, neighbor);
        // Trigger air current rebuild when neighbors change
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof EncasedFanTileEntity) {
            ((EncasedFanTileEntity) te).markAirFlowDirty();
        }
    }

    static int determineFacing(EntityLivingBase placer) {
        int pitch = MathHelper.floor_double((double) (placer.rotationPitch * 4.0F / 360.0F) + 0.5D) & 3;
        if (pitch == 0) {
            return ForgeDirection.DOWN.ordinal();
        } else if (pitch == 2) {
            return ForgeDirection.UP.ordinal();
        } else {
            return ForgeDirection.getOrientation(
                    MathHelper.floor_double((double) (placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3
            ).getOpposite().ordinal();
        }
    }
}
