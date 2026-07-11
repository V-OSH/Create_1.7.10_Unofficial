package create.core.kinetic.belt.transport;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Represents a single item riding on a belt.
 *
 * <p>Tracks the item's position along the belt (0.0 to beltLength)
 * and its visual offset for rendering. Implements Comparable so
 * items can be sorted by position (largest first — closest to END).</p>
 */
public class TransportedItemStack implements Comparable<TransportedItemStack> {

    public ItemStack stack;
    public float beltPosition;
    public float prevBeltPosition;
    public float sideOffset;
    public float prevSideOffset;
    public int insertedAt;
    public int insertedFrom; // ForgeDirection ordinal

    // --- Factory ---

    public static TransportedItemStack read(NBTTagCompound nbt) {
        TransportedItemStack t = new TransportedItemStack();
        if (nbt.hasKey("Item")) {
            NBTTagCompound itemTag = nbt.getCompoundTag("Item");
            // In 1.7.10, ItemStack.writeToNBT writes "id" (short), "Count" (byte), "Damage" (short).
            // ItemStack.loadItemStackFromNBT wraps readFromNBT which calls getItemById on "id".
            // The method returns null if getItem() returns null (i.e. unknown id).
            // But getCompoundTag returns a NEW EMPTY NBTTagCompound if the key doesn't exist.
            // hasKey("Item") returns true because we wrote it, but the short tag may still
            // be empty. So we need to check hasKey("id") specifically.
            if (itemTag.hasKey("id")) {
                t.stack = ItemStack.loadItemStackFromNBT(itemTag);
            }
        }
        t.beltPosition = nbt.getFloat("Pos");
        t.prevBeltPosition = nbt.getFloat("PrevPos");
        t.sideOffset = nbt.getFloat("Offset");
        t.prevSideOffset = nbt.getFloat("PrevOffset");
        t.insertedAt = nbt.getInteger("InSegment");
        t.insertedFrom = nbt.getInteger("InDirection");
        return t;
    }

    // --- Serialization ---

    public NBTTagCompound write() {
        NBTTagCompound nbt = new NBTTagCompound();
        if (stack != null) {
            NBTTagCompound itemTag = new NBTTagCompound();
            stack.writeToNBT(itemTag);
            nbt.setTag("Item", itemTag);
        } else {
            // Write an empty compound so read() always gets a tag
            nbt.setTag("Item", new NBTTagCompound());
        }
        nbt.setFloat("Pos", beltPosition);
        nbt.setFloat("PrevPos", prevBeltPosition);
        nbt.setFloat("Offset", sideOffset);
        nbt.setFloat("PrevOffset", prevSideOffset);
        nbt.setInteger("InSegment", insertedAt);
        nbt.setInteger("InDirection", insertedFrom);
        return nbt;
    }

    // --- Ordering: largest beltPosition first (closest to END) ---

    @Override
    public int compareTo(TransportedItemStack o) {
        return Float.compare(o.beltPosition, this.beltPosition);
    }

    // --- Safe copy ---

    public TransportedItemStack copy() {
        TransportedItemStack t = new TransportedItemStack();
        t.stack = stack != null ? stack.copy() : null;
        t.beltPosition = beltPosition;
        t.prevBeltPosition = prevBeltPosition;
        t.sideOffset = sideOffset;
        t.prevSideOffset = prevSideOffset;
        t.insertedAt = insertedAt;
        t.insertedFrom = insertedFrom;
        return t;
    }

    /** Total length of the stack's items plus spacing. Approximates 1.0 per item. */
    public float getStackWidth() {
        return stack != null ? Math.max(1.0f, stack.stackSize * 0.5f) : 1.0f;
    }
}
