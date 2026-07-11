package create.core.kinetic.belt;

import create.foundation.AllBlocks;
import create.shim.MyBlockPos;
import create.shim.MyDirection;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Player-facing item used to create belt chains between two parallel shafts.
 */
public class BeltConnectorItem extends Item {

    private static final int MAX_BELT_LENGTH = 20;
    private static final String TAG_START = "BeltStart";
    private static final String TAG_X = "X";
    private static final String TAG_Y = "Y";
    private static final String TAG_Z = "Z";
    private static final String TAG_DIM = "Dim";

    public BeltConnectorItem() {
        setUnlocalizedName("create.belt_connector");
        setTextureName("create:andesite_alloy");
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
                             int x, int y, int z, int side,
                             float hitX, float hitY, float hitZ) {
        if (stack == null || !isShaft(world, x, y, z)) {
            clearSelection(stack);
            return false;
        }

        MyBlockPos clicked = new MyBlockPos(x, y, z);
        if (!hasSelection(stack)) {
            if (!world.isRemote) {
                storeSelection(stack, clicked, world.provider.dimensionId);
            }
            return true;
        }

        MyBlockPos start = readSelection(stack);
        int dimensionId = stack.getTagCompound().getCompoundTag(TAG_START).getInteger(TAG_DIM);
        if (dimensionId != world.provider.dimensionId || start.equals(clicked)) {
            clearSelection(stack);
            return false;
        }

        if (world.isRemote) {
            return true;
        }

        boolean created = createBeltBetween(stack, player, world, start, clicked);
        clearSelection(stack);
        return created;
    }

    public static ForgeDirection getFacingBetween(MyBlockPos start, MyBlockPos end) {
        if (start.getY() != end.getY()) return ForgeDirection.UNKNOWN;
        if (start.getX() == end.getX() && start.getZ() != end.getZ()) {
            return end.getZ() > start.getZ() ? ForgeDirection.SOUTH : ForgeDirection.NORTH;
        }
        if (start.getZ() == end.getZ() && start.getX() != end.getX()) {
            return end.getX() > start.getX() ? ForgeDirection.EAST : ForgeDirection.WEST;
        }
        return ForgeDirection.UNKNOWN;
    }

    public static int getBeltLength(MyBlockPos start, MyBlockPos end) {
        return Math.abs(end.getX() - start.getX()) + Math.abs(end.getZ() - start.getZ()) + 1;
    }

    public static boolean isCompatibleShaftAxis(MyDirection.Axis shaftAxis,
                                                ForgeDirection beltFacing) {
        if (beltFacing == ForgeDirection.UNKNOWN) return false;
        return shaftAxis == BeltBlock.beltFacingToAxis(beltFacing);
    }

    private static boolean createBeltBetween(ItemStack stack, EntityPlayer player,
                                             World world, MyBlockPos start,
                                             MyBlockPos end) {
        ForgeDirection facing = getFacingBetween(start, end);
        if (facing == ForgeDirection.UNKNOWN) return false;

        int length = getBeltLength(start, end);
        if (length < 2 || length > MAX_BELT_LENGTH) return false;
        if (!player.capabilities.isCreativeMode && stack.stackSize < length) return false;

        MyDirection.Axis requiredAxis = BeltBlock.beltFacingToAxis(facing);
        if (!isCompatibleShaft(world, start, requiredAxis)
                || !isCompatibleShaft(world, end, requiredAxis)) {
            return false;
        }

        MyBlockPos current = start;
        for (int i = 0; i < length; i++) {
            boolean endpoint = i == 0 || i == length - 1;
            if (!endpoint && !world.isAirBlock(current.getX(), current.getY(), current.getZ())) {
                return false;
            }
            current = current.add(facing.offsetX, 0, facing.offsetZ);
        }

        current = start;
        for (int i = 0; i < length; i++) {
            BeltPart part = i == 0 ? BeltPart.START
                    : (i == length - 1 ? BeltPart.END : BeltPart.MIDDLE);
            int meta = BeltBlock.partToMeta(part, BeltBlock.facingToMeta(facing));
            world.setBlock(current.getX(), current.getY(), current.getZ(),
                    AllBlocks.BELT, meta, 3);

            TileEntity te = world.getTileEntity(current.getX(), current.getY(), current.getZ());
            if (te instanceof BeltTileEntity) {
                BeltTileEntity belt = (BeltTileEntity) te;
                belt.setBeltFacing(facing);
                belt.setRotationAxis(requiredAxis);
                belt.setBeltPart(part);
                belt.setController(i == 0 ? null : start);
                belt.setIndex(i);
                belt.setBeltLength(length);
                belt.chainInvalid = false;
                belt.markDirty();
            }
            current = current.add(facing.offsetX, 0, facing.offsetZ);
        }

        if (!player.capabilities.isCreativeMode) {
            stack.stackSize -= length;
        }
        return true;
    }

    private static boolean isCompatibleShaft(World world, MyBlockPos pos,
                                             MyDirection.Axis requiredAxis) {
        if (!isShaft(world, pos.getX(), pos.getY(), pos.getZ())) return false;
        int meta = world.getBlockMetadata(pos.getX(), pos.getY(), pos.getZ());
        return create.core.kinetic.ShaftBlock.axisFromMeta(meta) == requiredAxis;
    }

    private static boolean isShaft(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        return block == AllBlocks.SHAFT;
    }

    private static boolean hasSelection(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey(TAG_START);
    }

    private static void storeSelection(ItemStack stack, MyBlockPos pos, int dimensionId) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        NBTTagCompound start = new NBTTagCompound();
        start.setInteger(TAG_X, pos.getX());
        start.setInteger(TAG_Y, pos.getY());
        start.setInteger(TAG_Z, pos.getZ());
        start.setInteger(TAG_DIM, dimensionId);
        tag.setTag(TAG_START, start);
    }

    private static MyBlockPos readSelection(ItemStack stack) {
        NBTTagCompound start = stack.getTagCompound().getCompoundTag(TAG_START);
        return new MyBlockPos(start.getInteger(TAG_X),
                start.getInteger(TAG_Y), start.getInteger(TAG_Z));
    }

    private static void clearSelection(ItemStack stack) {
        if (stack != null && stack.hasTagCompound()) {
            stack.getTagCompound().removeTag(TAG_START);
            if (stack.getTagCompound().hasNoTags()) {
                stack.setTagCompound(null);
            }
        }
    }
}
