package create.core.machinery.millstone;

import create.core.kinetic.KineticTileEntity;
import create.core.machinery.recipe.MillingRecipe;
import create.core.machinery.recipe.ProcessingRecipe;
import create.core.machinery.recipe.ProcessingRecipeRegistry;
import create.core.machinery.recipe.RecipeType;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/**
 * Millstone tile entity — grinds input items into output products using
 * rotational power. Speed scales processing rate.
 */
public class MillstoneTileEntity extends KineticTileEntity implements IInventory {

    private ItemStack inputInv;
    private final ItemStack[] outputInv = new ItemStack[9];
    private int timer;
    private MillingRecipe lastRecipe;

    public MillstoneTileEntity() {
        inputInv = null;
    }

    // --- Tick ---

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (worldObj == null || worldObj.isRemote) return;
        if (getSpeed() == 0) return;

        // Check if output is full
        if (isOutputFull()) return;

        int processingSpeed = getProcessingSpeed();
        timer -= processingSpeed;

        if (timer <= 0) {
            if (inputInv != null && lastRecipe != null) {
                process();
            }
            // Look up next recipe
            if (inputInv == null) {
                timer = 100; // idle cooldown
                lastRecipe = null;
                return;
            }
            ProcessingRecipe found = ProcessingRecipeRegistry.find(RecipeType.MILLING, this);
            if (found instanceof MillingRecipe) {
                lastRecipe = (MillingRecipe) found;
                timer = lastRecipe.getProcessingDuration();
            } else {
                timer = 100; // no valid recipe, idle cooldown
                lastRecipe = null;
            }
        }
    }

    private void process() {
        if (inputInv == null || lastRecipe == null) return;

        // Consume input
        inputInv.stackSize--;
        if (inputInv.stackSize <= 0) {
            inputInv = null;
        }

        // Roll and insert results
        List<ItemStack> results = lastRecipe.rollResults();
        for (ItemStack result : results) {
            insertToOutput(result);
        }

        markDirty();
    }

    private void insertToOutput(ItemStack stack) {
        if (stack == null) return;
        // Try to stack first
        for (int i = 0; i < 9; i++) {
            if (outputInv[i] != null
                    && outputInv[i].getItem() == stack.getItem()
                    && outputInv[i].getItemDamage() == stack.getItemDamage()
                    && outputInv[i].stackSize < outputInv[i].getMaxStackSize()) {
                int space = outputInv[i].getMaxStackSize() - outputInv[i].stackSize;
                int toAdd = Math.min(space, stack.stackSize);
                outputInv[i].stackSize += toAdd;
                stack.stackSize -= toAdd;
                if (stack.stackSize <= 0) return;
            }
        }
        // Place in empty slot
        for (int i = 0; i < 9; i++) {
            if (outputInv[i] == null) {
                outputInv[i] = stack.copy();
                stack.stackSize = 0;
                return;
            }
        }
    }

    private boolean isOutputFull() {
        for (int i = 0; i < 9; i++) {
            if (outputInv[i] == null) return false;
            if (outputInv[i].stackSize < outputInv[i].getMaxStackSize()) return false;
        }
        return true;
    }

    int getProcessingSpeed() {
        int speed = Math.max(1, Math.abs((int) (getSpeed() / 16f)));
        return Math.min(speed, 512);
    }

    /** Try to insert a stack into the input slot. Returns leftover or null. */
    public ItemStack tryInsert(ItemStack stack) {
        if (stack == null || stack.stackSize == 0) return null;
        if (inputInv == null) {
            int insert = Math.min(stack.stackSize, stack.getMaxStackSize());
            inputInv = stack.copy();
            inputInv.stackSize = insert;
            stack.stackSize -= insert;
            lastRecipe = null;
            markDirty();
            return stack.stackSize > 0 ? stack : null;
        }
        if (inputInv.getItem() == stack.getItem()
                && inputInv.getItemDamage() == stack.getItemDamage()
                && inputInv.stackSize < inputInv.getMaxStackSize()) {
            int space = inputInv.getMaxStackSize() - inputInv.stackSize;
            int toAdd = Math.min(space, stack.stackSize);
            inputInv.stackSize += toAdd;
            stack.stackSize -= toAdd;
            markDirty();
            return stack.stackSize > 0 ? stack : null;
        }
        return stack;
    }

    // --- IInventory ---

    @Override
    public int getSizeInventory() {
        return 10; // slot 0 = input, slots 1-9 = output
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot == 0) return inputInv;
        if (slot >= 1 && slot <= 9) return outputInv[slot - 1];
        return null;
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (slot == 0) {
            if (inputInv == null) return null;
            ItemStack stack;
            if (inputInv.stackSize <= amount) {
                stack = inputInv;
                inputInv = null;
            } else {
                stack = inputInv.splitStack(amount);
            }
            markDirty();
            return stack;
        }
        if (slot >= 1 && slot <= 9) {
            int idx = slot - 1;
            if (outputInv[idx] == null) return null;
            ItemStack stack;
            if (outputInv[idx].stackSize <= amount) {
                stack = outputInv[idx];
                outputInv[idx] = null;
            } else {
                stack = outputInv[idx].splitStack(amount);
            }
            markDirty();
            return stack;
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return getStackInSlot(slot);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot == 0) {
            inputInv = stack;
        } else if (slot >= 1 && slot <= 9) {
            outputInv[slot - 1] = stack;
        }
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return "container.create.millstone";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
                && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 64.0;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return slot == 0; // only input slot accepts items
    }

    // --- NBT ---

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        timer = tag.getInteger("timer");

        // Input slot
        if (tag.hasKey("input")) {
            inputInv = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("input"));
        } else {
            inputInv = null;
        }

        // Output slots
        NBTTagList outList = tag.getTagList("output", 10);
        for (int i = 0; i < outList.tagCount() && i < 9; i++) {
            NBTTagCompound slotTag = outList.getCompoundTagAt(i);
            int slotIdx = slotTag.getByte("slot") & 0xFF;
            if (slotIdx < 9) {
                outputInv[slotIdx] = ItemStack.loadItemStackFromNBT(slotTag);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("timer", timer);

        if (inputInv != null) {
            NBTTagCompound inputTag = new NBTTagCompound();
            inputInv.writeToNBT(inputTag);
            tag.setTag("input", inputTag);
        }

        NBTTagList outList = new NBTTagList();
        for (int i = 0; i < 9; i++) {
            if (outputInv[i] != null) {
                NBTTagCompound slotTag = new NBTTagCompound();
                slotTag.setByte("slot", (byte) i);
                outputInv[i].writeToNBT(slotTag);
                outList.appendTag(slotTag);
            }
        }
        tag.setTag("output", outList);
    }
}
