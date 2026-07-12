package create.core.machinery.millstone;

import create.core.kinetic.KineticBlock;
import create.core.kinetic.KineticBlockType;
import create.core.kinetic.KineticTileEntity;
import create.shim.MyDirection;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * The Millstone — a vertical millstone that grinds items dropped on top.
 * Powered from below via shaft. Acts as a large cogwheel for side connections.
 */
public class MillstoneBlock extends KineticBlock {

    public MillstoneBlock() {
        setBlockName("create:millstone");
        setBlockTextureName("create:millstone");
        setHarvestLevel("pickaxe", 1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        super.registerBlockIcons(reg);
        registerTexture(reg, "millstone");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return iconMap.get("millstone");
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public Class<? extends TileEntity> getTileEntityClass() {
        return MillstoneTileEntity.class;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        MillstoneTileEntity te = new MillstoneTileEntity();
        te.setKineticType(KineticBlockType.LARGE_COGWHEEL);
        te.setRotationAxis(MyDirection.Axis.Y);
        return te;
    }

    // --- Item pickup from top ---

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z,
                                          net.minecraft.entity.Entity entity) {
        if (world.isRemote) return;
        if (!(entity instanceof EntityItem)) return;

        EntityItem itemEntity = (EntityItem) entity;
        if (itemEntity.isDead) return;

        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof MillstoneTileEntity)) return;

        MillstoneTileEntity millstone = (MillstoneTileEntity) te;
        ItemStack remaining = millstone.tryInsert(itemEntity.getEntityItem());
        if (remaining == null || remaining.stackSize == 0) {
            itemEntity.setDead();
        } else {
            itemEntity.setEntityItemStack(remaining);
        }
    }

    // --- Right-click extraction ---

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
                                    EntityPlayer player, int side,
                                    float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;

        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof MillstoneTileEntity)) return false;

        MillstoneTileEntity millstone = (MillstoneTileEntity) te;

        if (player.getHeldItem() != null) return false;

        // First empty output slots, then input slot
        for (int i = 1; i <= 9; i++) {
            ItemStack stack = millstone.getStackInSlot(i);
            if (stack != null) {
                player.inventory.addItemStackToInventory(stack);
                millstone.setInventorySlotContents(i, null);
                return true;
            }
        }
        ItemStack input = millstone.getStackInSlot(0);
        if (input != null) {
            player.inventory.addItemStackToInventory(input);
            millstone.setInventorySlotContents(0, null);
            return true;
        }
        return false;
    }
}
