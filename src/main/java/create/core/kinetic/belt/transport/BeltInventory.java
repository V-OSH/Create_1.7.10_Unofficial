package create.core.kinetic.belt.transport;

import create.core.kinetic.belt.BeltHelper;
import create.core.kinetic.belt.BeltTileEntity;
import create.shim.MyVec3;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Holds all items riding on a belt chain.
 *
 * <p>Only the controller (START segment) owns an instance. Items are
 * stored in a sorted LinkedList (largest beltPosition first). Each
 * tick, items advance by the belt's movement speed. Items that reach
 * the end are either ejected, inserted into an adjacent inventory,
 * or blocked by a solid block.</p>
 */
public class BeltInventory {

    private final BeltTileEntity belt;
    private final LinkedList<TransportedItemStack> items = new LinkedList<>();
    private final List<TransportedItemStack> toInsert = new ArrayList<>();
    private final List<TransportedItemStack> toRemove = new ArrayList<>();

    private boolean beltMovementPositive;

    /** Margin values for ending resolution. Items visually pull up to
     *  the boundary before resolving. */
    private static final float MARGIN_EJECT = 0f;
    private static final float MARGIN_INSERT = 0.25f;
    private static final float MARGIN_BLOCKED = 0.45f;

    // --- Ending enum ---

    public enum Ending {
        UNRESOLVED(MARGIN_EJECT),
        EJECT(MARGIN_EJECT),
        INSERT(MARGIN_INSERT),
        BLOCKED(MARGIN_BLOCKED);

        final float margin;
        Ending(float margin) { this.margin = margin; }
    }

    public BeltInventory(BeltTileEntity belt) {
        this.belt = belt;
    }

    // --- Tick ---

    public void tick() {
        // Process queued inserts/removals from previous tick
        if (!toInsert.isEmpty()) {
            for (TransportedItemStack tis : toInsert) {
                insertIntoItems(tis);
            }
            toInsert.clear();
        }
        if (!toRemove.isEmpty()) {
            items.removeAll(toRemove);
            toRemove.clear();
        }

        World world = belt.getWorldObj();
        if (world == null || world.isRemote) return;

        float movement = belt.getDirectionAwareBeltMovementSpeed();
        if (movement == 0) return;

        // Detect direction reversal
        boolean currentPositive = movement > 0;
        if (beltMovementPositive != currentPositive) {
            beltMovementPositive = currentPositive;
            Collections.reverse(items);
        }

        // Iterate from last to first (highest position first = closest to END)
        // Use index-based iteration since we may modify the list
        for (int i = 0; i < items.size(); i++) {
            TransportedItemStack current = items.get(i);
            if (current.stack == null || current.stack.stackSize <= 0) {
                toRemove.add(current);
                continue;
            }

            current.prevBeltPosition = current.beltPosition;
            current.prevSideOffset = current.sideOffset;

            float desiredMovement = movement;

            // Collision: clamp by item in front (spacing = 1.0)
            if (i > 0) {
                TransportedItemStack front = items.get(i - 1);
                float gap = movement > 0
                        ? front.beltPosition - current.beltPosition
                        : current.beltPosition - front.beltPosition;
                float minSpacing = 1.0f; // minimum 1 block between items
                if (gap < minSpacing + Math.abs(movement)) {
                    float maxMove = Math.max(0, gap - minSpacing);
                    if (Math.abs(desiredMovement) > maxMove) {
                        desiredMovement = movement > 0 ? maxMove : -maxMove;
                    }
                }
            }

            // End-of-belt clamping
            float beltLength = belt.getBeltLength();
            float newPosition = current.beltPosition + desiredMovement;
            Ending ending = Ending.UNRESOLVED;

            if (movement > 0 && newPosition >= beltLength - 0.01f) {
                ending = resolveEnding(world);
                float margin = ending.margin;
                newPosition = Math.min(newPosition, beltLength - margin);
                if (newPosition >= beltLength - margin) {
                    handleEnding(current, ending, world);
                    continue; // item was removed or stopped
                }
            } else if (movement < 0 && newPosition <= 0.01f) {
                // Moving backward past START: eject at start
                ending = Ending.EJECT;
                newPosition = Math.max(newPosition, 0);
                if (newPosition <= 0) {
                    handleEnding(current, Ending.EJECT, world);
                    continue;
                }
            } else {
                newPosition = Math.max(0, Math.min(newPosition, beltLength));
            }

            current.beltPosition = newPosition;
            // Tween sideOffset toward 0
            current.sideOffset *= 0.7f;
        }

        // Process removals
        if (!toRemove.isEmpty()) {
            items.removeAll(toRemove);
            toRemove.clear();
        }
    }

    // --- Ending resolution ---

    private Ending resolveEnding(World world) {
        int beltLen = belt.getBeltLength();
        ForgeDirection facing = belt.getBeltFacing();
        // Position one block past the END of the belt chain
        int endX = belt.xCoord + facing.offsetX * beltLen;
        int endY = belt.yCoord;
        int endZ = belt.zCoord + facing.offsetZ * beltLen;

        // Check for IInventory neighbor
        TileEntity neighbor = world.getTileEntity(endX, endY, endZ);
        if (neighbor instanceof IInventory) {
            return Ending.INSERT;
        }

        // Check for solid block blocking the path
        net.minecraft.block.Block endBlock = world.getBlock(endX, endY, endZ);
        if (endBlock != null && endBlock.getMaterial().isSolid()) {
            return Ending.BLOCKED;
        }

        return Ending.EJECT;
    }

    private void handleEnding(TransportedItemStack current, Ending ending, World world) {
        switch (ending) {
            case EJECT:
                eject(current, world);
                toRemove.add(current);
                break;
            case INSERT:
                if (tryInsertIntoInventory(current, world)) {
                    toRemove.add(current);
                } else {
                    // Can't insert: stop at boundary
                    current.beltPosition = belt.getBeltLength() - MARGIN_BLOCKED;
                }
                break;
            case BLOCKED:
                current.beltPosition = belt.getBeltLength() - MARGIN_BLOCKED;
                break;
        }
    }

    // --- Insert into adjacent inventory ---

    private boolean tryInsertIntoInventory(TransportedItemStack current, World world) {
        int beltLen = belt.getBeltLength();
        ForgeDirection facing = belt.getBeltFacing();
        int endX = belt.xCoord + facing.offsetX * beltLen;
        int endY = belt.yCoord;
        int endZ = belt.zCoord + facing.offsetZ * beltLen;

        TileEntity te = world.getTileEntity(endX, endY, endZ);
        if (!(te instanceof IInventory)) return false;

        IInventory inv = (IInventory) te;
        ItemStack remaining = insertStack(inv, current.stack.copy(), facing.getOpposite());
        if (remaining == null || remaining.stackSize == 0) {
            current.stack.stackSize = 0;
            return true;
        }
        // Partial insert: reduce stack
        current.stack.stackSize = remaining.stackSize;
        return remaining.stackSize < current.stack.stackSize;
    }

    /**
     * Insert a stack into an IInventory, trying all slots.
     */
    public static ItemStack insertStack(IInventory inv, ItemStack stack,
                                         ForgeDirection side) {
        if (stack == null || stack.stackSize <= 0) return null;

        int size = inv.getSizeInventory();
        // First pass: merge into existing stacks
        for (int i = 0; i < size && stack.stackSize > 0; i++) {
            if (!inv.isItemValidForSlot(i, stack)) continue;
            ItemStack existing = inv.getStackInSlot(i);
            if (existing == null) continue;
            if (existing.isItemEqual(stack)
                    && ItemStack.areItemStackTagsEqual(existing, stack)) {
                int maxSize = Math.min(inv.getInventoryStackLimit(),
                        existing.getMaxStackSize());
                int space = maxSize - existing.stackSize;
                if (space > 0) {
                    int toMove = Math.min(space, stack.stackSize);
                    existing.stackSize += toMove;
                    stack.stackSize -= toMove;
                    inv.setInventorySlotContents(i, existing);
                }
            }
        }
        // Second pass: place into empty slots
        for (int i = 0; i < size && stack.stackSize > 0; i++) {
            if (!inv.isItemValidForSlot(i, stack)) continue;
            ItemStack existing = inv.getStackInSlot(i);
            if (existing == null) {
                int maxSize = Math.min(inv.getInventoryStackLimit(),
                        stack.getMaxStackSize());
                int toPlace = Math.min(maxSize, stack.stackSize);
                ItemStack placed = stack.copy();
                placed.stackSize = toPlace;
                inv.setInventorySlotContents(i, placed);
                stack.stackSize -= toPlace;
            }
        }

        return stack.stackSize <= 0 ? null : stack;
    }

    // --- Eject ---

    private void eject(TransportedItemStack current, World world) {
        if (current.stack == null || current.stack.stackSize <= 0) return;

        // Compute world position at end of belt
        float partialTicks = 0;
        MyVec3 pos = BeltHelper.getVectorForOffset(
                belt.getControllerBE(), current.beltPosition, partialTicks);

        float speed = belt.getBeltMovementSpeed();
        ForgeDirection facing = belt.getBeltFacing();
        double vx = facing.offsetX * Math.max(Math.abs(speed), 1.0f / 8.0f);
        double vy = 1.0 / 8.0;
        double vz = facing.offsetZ * Math.max(Math.abs(speed), 1.0f / 8.0f);

        EntityItem entity = new EntityItem(world, pos.x, pos.y, pos.z,
                current.stack.copy());
        entity.motionX = vx;
        entity.motionY = vy;
        entity.motionZ = vz;
        entity.delayBeforeCanPickup = 10;
        world.spawnEntityInWorld(entity);

        current.stack.stackSize = 0;
    }

    // --- Insert / Remove ---

    /** Queue an item for insertion in the next tick. */
    public void addItem(TransportedItemStack stack) {
        toInsert.add(stack);
    }

    /** Internal: insert into sorted position (largest position first). */
    private void insertIntoItems(TransportedItemStack stack) {
        int idx = 0;
        for (TransportedItemStack existing : items) {
            if (stack.beltPosition >= existing.beltPosition) break;
            idx++;
        }
        items.add(idx, stack);
    }

    // --- Bulk operations ---

    /** Eject all items (called on chain destruction). */
    public void ejectAll() {
        World world = belt.getWorldObj();
        if (world == null || world.isRemote) return;
        for (TransportedItemStack tis : items) {
            if (tis.stack != null && tis.stack.stackSize > 0) {
                tis.beltPosition = Math.max(0, Math.min(tis.beltPosition,
                        belt.getBeltLength()));
                eject(tis, world);
            }
        }
        items.clear();
        toInsert.clear();
        toRemove.clear();
    }

    /** Get the transported items (for queries like canInsertAt). */
    public List<TransportedItemStack> getTransportedItems() {
        return items;
    }

    /** Check if there's already an item at the given offset. */
    public boolean canInsertAt(float offset) {
        if (hasStackNear(items, offset)) {
            return false;
        }
        return !hasStackNear(toInsert, offset);
    }

    private static boolean hasStackNear(List<TransportedItemStack> stacks, float offset) {
        for (TransportedItemStack tis : stacks) {
            if (tis.stack != null && tis.stack.stackSize > 0
                    && Math.abs(tis.beltPosition - offset) < 0.5f) {
                return true;
            }
        }
        return false;
    }

    // --- NBT ---

    public NBTTagCompound write() {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        for (TransportedItemStack tis : items) {
            list.appendTag(tis.write());
        }
        nbt.setTag("Items", list);
        nbt.setBoolean("PositiveOrder", beltMovementPositive);
        return nbt;
    }

    public void read(NBTTagCompound nbt) {
        items.clear();
        NBTTagList list = nbt.getTagList("Items", 10); // 10 = NBTTagCompound
        for (int i = 0; i < list.tagCount(); i++) {
            items.add(TransportedItemStack.read(list.getCompoundTagAt(i)));
        }
        beltMovementPositive = nbt.getBoolean("PositiveOrder");
    }
}
