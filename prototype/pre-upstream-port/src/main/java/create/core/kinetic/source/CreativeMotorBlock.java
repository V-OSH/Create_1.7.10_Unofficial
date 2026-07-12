package create.core.kinetic.source;

import create.core.kinetic.KineticBlock;
import create.shim.MyDirection;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Creative motor — a debug/sandbox kinetic source that always runs.
 *
 * <p>Placement: faces the side that was clicked (similar to pistons or
 * levers). The shaft connection is on the facing side.</p>
 *
 * <p>Right-click: cycles speed by +16 RPM. Sneak + right-click: cycles
 * by −16 RPM. Wraps at ±256, skips zero.</p>
 *
 * <p>Metadata: 0-5 for the 6 facing directions.</p>
 */
public class CreativeMotorBlock extends KineticBlock {

    public CreativeMotorBlock() {
        setBlockName("create:creative_motor");
        setBlockTextureName("create:creative_motor");
        setHarvestLevel("pickaxe", 1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        super.registerBlockIcons(reg);
        registerTexture(reg, "creative_motor");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return iconMap.get("creative_motor");
    }

    @Override
    public Class<? extends TileEntity> getTileEntityClass() {
        return CreativeMotorTileEntity.class;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        setMotorBounds(world.getBlockMetadata(x, y, z));
    }

    @Override
    public void setBlockBoundsForItemRender() {
        setBlockBounds(1 / 16f, 1 / 16f, 1 / 16f, 15 / 16f, 15 / 16f, 15 / 16f);
    }

    private void setMotorBounds(int metadata) {
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
        CreativeMotorTileEntity te = new CreativeMotorTileEntity();
        ForgeDirection facing = ForgeDirection.getOrientation(metadata & 7);
        te.setRotationAxis(MyDirection.Axis.fromForge(facing));
        return te;
    }

    // --- Placement ---

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        // KineticBlock.onBlockAdded handles network join; we just ensure
        // the TE's rotation axis is set on placement.
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof CreativeMotorTileEntity) {
                int meta = world.getBlockMetadata(x, y, z);
                ForgeDirection facing = ForgeDirection.getOrientation(meta & 7);
                ((CreativeMotorTileEntity) te).setRotationAxis(
                        MyDirection.Axis.fromForge(facing));
            }
        }
        super.onBlockAdded(world, x, y, z);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z,
                                 EntityLivingBase placer, ItemStack stack) {
        // Determine facing from the side of the block that was clicked
        // The metadata was already set by Block.onBlockPlaced before this
        // Forge calls onBlockPlacedBy. We initialize the TE axis here.
        int meta = world.getBlockMetadata(x, y, z);
        // meta should already encode facing (0-5), set by the block placement logic
        // If meta wasn't set yet, default to the placer's facing direction
        if (meta > 5) {
            int facing = net.minecraft.util.MathHelper
                    .floor_double((double) (placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
            // Convert horizontal facing (0=south, 1=west, 2=north, 3=east) to ForgeDirection
            switch (facing) {
                case 0: meta = ForgeDirection.SOUTH.ordinal(); break;  // 1
                case 1: meta = ForgeDirection.WEST.ordinal(); break;   // 5
                case 2: meta = ForgeDirection.NORTH.ordinal(); break;  // 0
                case 3: meta = ForgeDirection.EAST.ordinal(); break;   // 4
                default: meta = ForgeDirection.UP.ordinal();
            }
            world.setBlockMetadataWithNotify(x, y, z, meta, 2);
        }
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof CreativeMotorTileEntity) {
            ForgeDirection facing = ForgeDirection.getOrientation(meta & 7);
            ((CreativeMotorTileEntity) te).setRotationAxis(
                    MyDirection.Axis.fromForge(facing));
        }
    }

    // --- Interaction ---

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
                                     EntityPlayer player, int side,
                                     float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;

        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof CreativeMotorTileEntity) {
            player.openGui(create.CreateMod.instance,
                    create.foundation.gui.CreateGuiProxy.GUI_CREATIVE_MOTOR,
                    world, x, y, z);
            return true;
        }
        return false;
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }
}
