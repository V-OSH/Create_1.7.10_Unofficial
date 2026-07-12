package create.core.machinery.drill;

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
 * Mechanical Drill — breaks the block in front of it using rotational power.
 * The shaft connection is on the back face.
 */
public class DrillBlock extends KineticBlock {

    public DrillBlock() {
        setBlockName("create:drill");
        setBlockTextureName("create:drill");
        setHarvestLevel("axe", 0);
        setHarvestLevel("pickaxe", 1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        super.registerBlockIcons(reg);
        registerTexture(reg, "drill");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return iconMap.get("drill");
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public Class<? extends TileEntity> getTileEntityClass() {
        return DrillTileEntity.class;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        setDirectionalMachineBounds(world.getBlockMetadata(x, y, z));
    }

    @Override
    public void setBlockBoundsForItemRender() {
        setBlockBounds(2 / 16f, 2 / 16f, 2 / 16f, 14 / 16f, 14 / 16f, 14 / 16f);
    }

    private void setDirectionalMachineBounds(int metadata) {
        ForgeDirection facing = ForgeDirection.getOrientation(metadata & 7);
        switch (facing) {
            case DOWN: setBlockBounds(2 / 16f, 0, 2 / 16f, 14 / 16f, 14 / 16f, 14 / 16f); break;
            case UP: setBlockBounds(2 / 16f, 2 / 16f, 2 / 16f, 14 / 16f, 1, 14 / 16f); break;
            case NORTH: setBlockBounds(2 / 16f, 2 / 16f, 0, 14 / 16f, 14 / 16f, 14 / 16f); break;
            case SOUTH: setBlockBounds(2 / 16f, 2 / 16f, 2 / 16f, 14 / 16f, 14 / 16f, 1); break;
            case WEST: setBlockBounds(0, 2 / 16f, 2 / 16f, 14 / 16f, 14 / 16f, 14 / 16f); break;
            case EAST: setBlockBounds(2 / 16f, 2 / 16f, 2 / 16f, 1, 14 / 16f, 14 / 16f); break;
            default: setBlockBounds(2 / 16f, 2 / 16f, 2 / 16f, 14 / 16f, 14 / 16f, 14 / 16f);
        }
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        DrillTileEntity te = new DrillTileEntity();
        te.setKineticType(KineticBlockType.SHAFT);
        // Axis is set from facing in the TE's first updateEntity
        te.setRotationAxis(MyDirection.Axis.Y);
        return te;
    }

    // --- Directional placement ---

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z,
                                EntityLivingBase placer, ItemStack stack) {
        int facing = determineFacing(placer);
        world.setBlockMetadataWithNotify(x, y, z, facing, 2);
        // Update the TE's rotation axis
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof DrillTileEntity) {
            ((DrillTileEntity) te).updateFromMetadata(facing);
        }
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        // Update TE from current metadata before super runs network logic
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof DrillTileEntity) {
            int meta = world.getBlockMetadata(x, y, z);
            ((DrillTileEntity) te).updateFromMetadata(meta);
        }
        super.onBlockAdded(world, x, y, z);
    }

    static int determineFacing(EntityLivingBase placer) {
        int pitch = MathHelper.floor_double((double) (placer.rotationPitch * 4.0F / 360.0F) + 0.5D) & 3;
        if (pitch == 0) {
            // Looking up → place facing down
            return ForgeDirection.DOWN.ordinal();
        } else if (pitch == 2) {
            // Looking down → place facing up
            return ForgeDirection.UP.ordinal();
        } else {
            // Horizontal → facing opposite of player look
            return ForgeDirection.getOrientation(
                    MathHelper.floor_double((double) (placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3
            ).getOpposite().ordinal();
        }
    }

    // --- Entity damage ---

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z,
                                          net.minecraft.entity.Entity entity) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof DrillTileEntity)) return;

        DrillTileEntity drill = (DrillTileEntity) te;
        float speed = drill.getSpeed();
        if (speed == 0) return;

        float damage = getDamage(speed);
        // Only damage if entity is in front of the drill head
        ForgeDirection facing = drill.getFacing();
        if (facing == null) return;

        double dx = entity.posX - (x + 0.5 + facing.offsetX * 0.6);
        double dy = entity.posY - (y + 0.5 + facing.offsetY * 0.6);
        double dz = entity.posZ - (z + 0.5 + facing.offsetZ * 0.6);
        double dot = dx * facing.offsetX + dy * facing.offsetY + dz * facing.offsetZ;

        if (dot > 0) {
            entity.attackEntityFrom(
                    net.minecraft.util.DamageSource.generic, damage);
        }
    }

    static float getDamage(float speed) {
        float s = Math.abs(speed);
        return MathHelper.clamp_float(
                (float) Math.min(s / 16f, 2)
                        + (float) Math.min(s / 32f, 4)
                        + (float) Math.min(s / 64f, 4),
                1f, 10f);
    }
}
