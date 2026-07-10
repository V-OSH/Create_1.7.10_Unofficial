package create.core.machinery.drill;

import create.core.kinetic.KineticTileEntity;
import create.shim.MyBlockPos;
import create.shim.MyDirection;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Drill tile entity — breaks blocks in front using rotational power.
 * Break speed scales with RPM.
 */
public class DrillTileEntity extends KineticTileEntity {

    private ForgeDirection facing = ForgeDirection.NORTH;
    private int ticksUntilNextProgress;
    private int destroyProgress; // 0-10

    // --- Accessors ---

    public ForgeDirection getFacing() {
        return facing;
    }

    /** Called from DrillBlock to sync metadata to TE state. */
    public void updateFromMetadata(int meta) {
        if (meta >= 0 && meta < 6) {
            this.facing = ForgeDirection.getOrientation(meta);
            // Rotation axis is always along the shaft, which is the facing direction's axis
            this.rotationAxis = MyDirection.Axis.fromForge(facing);
        }
    }

    // --- Tick ---

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (worldObj == null || worldObj.isRemote) return;
        if (getSpeed() == 0) {
            destroyProgress = 0;
            return;
        }

        MyBlockPos breakingPos = getBreakingPos();
        if (breakingPos == null) return;

        if (ticksUntilNextProgress > 0) {
            ticksUntilNextProgress--;
        }

        if (ticksUntilNextProgress <= 0) {
            Block block = worldObj.getBlock(breakingPos.getX(), breakingPos.getY(), breakingPos.getZ());
            int meta = worldObj.getBlockMetadata(breakingPos.getX(), breakingPos.getY(), breakingPos.getZ());

            if (!canBreak(block, breakingPos)) {
                destroyProgress = 0;
                ticksUntilNextProgress = 10; // re-check delay
                return;
            }

            float breakSpeed = getBreakSpeed();
            float hardness = block.getBlockHardness(worldObj,
                    breakingPos.getX(), breakingPos.getY(), breakingPos.getZ());

            int progress = Math.max(1, (int) (breakSpeed / hardness));
            destroyProgress += progress;
            destroyProgress = Math.min(destroyProgress, 10);

            if (destroyProgress >= 10) {
                onBlockBroken(breakingPos, block, meta);
                destroyProgress = 0;
            }

            ticksUntilNextProgress = Math.max(1, (int) (hardness / breakSpeed));
        }
    }

    private MyBlockPos getBreakingPos() {
        return new MyBlockPos(
                xCoord + facing.offsetX,
                yCoord + facing.offsetY,
                zCoord + facing.offsetZ);
    }

    private boolean canBreak(Block block, MyBlockPos pos) {
        if (block == null || block == Blocks.air) return false;
        if (block.getMaterial().isLiquid()) return false;
        float hardness = block.getBlockHardness(worldObj, pos.getX(), pos.getY(), pos.getZ());
        return hardness >= 0 && hardness < Float.MAX_VALUE;
    }

    private float getBreakSpeed() {
        return Math.abs(getSpeed()) / 100f;
    }

    private void onBlockBroken(MyBlockPos pos, Block block, int meta) {
        // Collect drops before destroying the block
        List<ItemStack> drops = new ArrayList<>();
        int fortune = 0; // drills don't have fortune
        drops.addAll(block.getDrops(worldObj, pos.getX(), pos.getY(), pos.getZ(), meta, fortune));

        // Destroy the block
        worldObj.setBlockToAir(pos.getX(), pos.getY(), pos.getZ());
        worldObj.playAuxSFX(2001, pos.getX(), pos.getY(), pos.getZ(),
                Block.getIdFromBlock(block) + (meta << 12));

        // Spawn drops at the break position
        for (ItemStack drop : drops) {
            if (drop == null) continue;
            float f = 0.7F;
            double dx = worldObj.rand.nextFloat() * f + (1.0F - f) * 0.5D;
            double dy = worldObj.rand.nextFloat() * f + (1.0F - f) * 0.5D;
            double dz = worldObj.rand.nextFloat() * f + (1.0F - f) * 0.5D;
            EntityItem entityItem = new EntityItem(worldObj,
                    pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz, drop.copy());
            entityItem.delayBeforeCanPickup = 10;
            worldObj.spawnEntityInWorld(entityItem);
        }
    }

    // --- NBT ---

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("facing")) {
            facing = ForgeDirection.getOrientation(tag.getInteger("facing"));
            rotationAxis = MyDirection.Axis.fromForge(facing);
        }
        ticksUntilNextProgress = tag.getInteger("ticksUntilNext");
        destroyProgress = tag.getInteger("destroyProgress");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("facing", facing.ordinal());
        tag.setInteger("ticksUntilNext", ticksUntilNextProgress);
        tag.setInteger("destroyProgress", destroyProgress);
    }
}
