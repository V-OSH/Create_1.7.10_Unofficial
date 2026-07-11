package create.core.kinetic.belt;

import create.core.kinetic.KineticBlockType;
import create.core.kinetic.KineticTileEntity;
import create.core.kinetic.RotationPropagator;
import create.core.kinetic.belt.transport.BeltInventory;
import create.shim.MyBlockPos;
import create.shim.MyDirection;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Tile entity for a single belt segment in a multi-block chain.
 *
 * <p>Only the START segment (index 0) is the "controller" — it owns
 * the {@link BeltInventory} and is the only segment that participates
 * in the kinetic network. Non-controller segments look up the controller
 * for speed and inventory access.</p>
 *
 * <p>Each segment stores the controller position, its index within the
 * chain, and the total chain length. These are persisted to NBT for
 * chunk-load recovery.</p>
 */
public class BeltTileEntity extends KineticTileEntity {

    // --- Chain identity (persisted) ---

    /** Position of the START segment. Null if this IS the START. */
    protected MyBlockPos controller;

    /** 0-based position within the chain. 0 = START, beltLength-1 = END. */
    protected int index;

    /** Total number of segments in the chain. */
    protected int beltLength;

    /** This segment's role in the chain. */
    protected BeltPart beltPart = BeltPart.START;

    /** Slope type (HORIZONTAL only in iteration 1). */
    protected BeltSlope slope = BeltSlope.HORIZONTAL;

    /** Direction the belt chain travels. */
    protected ForgeDirection facing = ForgeDirection.NORTH;

    // --- Public accessors for BeltInventory (different package) ---

    /** Total number of segments in the chain. */
    public int getBeltLength() { return beltLength; }

    /** 0-based position within the chain. */
    public int getIndex() { return index; }

    // --- Inventory (controller only) ---

    /** Owns items. Only non-null when isController(). Lazy-initialized. */
    protected BeltInventory inventory;

    // --- Chain validation ---

    /** Set to true when this segment's chain is invalid and needs repair. */
    public boolean chainInvalid;

    // --- Identity ---

    public boolean isController() {
        return controller == null || controller.equals(getKineticPos());
    }

    public BeltTileEntity getControllerBE() {
        if (isController()) return this;
        if (controller == null || worldObj == null) return null;
        TileEntity te = worldObj.getTileEntity(
                controller.getX(), controller.getY(), controller.getZ());
        if (te instanceof BeltTileEntity) return (BeltTileEntity) te;
        return null;
    }

    public BeltInventory getInventory() {
        if (!isController()) {
            BeltTileEntity ctrl = getControllerBE();
            return ctrl != null ? ctrl.getInventory() : null;
        }
        if (inventory == null) {
            inventory = new BeltInventory(this);
        }
        return inventory;
    }

    // --- Speed ---

    /** Belt movement speed in blocks per tick. */
    public float getBeltMovementSpeed() {
        return getSpeed() / 480f;
    }

    /**
     * Direction-aware movement speed. Positive = toward END,
     * negative = toward START. Handles X-axis sign convention
     * (EAST = positive X = negative facing for belt direction).
     */
    public float getDirectionAwareBeltMovementSpeed() {
        float speed = getBeltMovementSpeed();
        // For EAST/WEST facings: offsetX is ±1, invert sign
        if (facing.offsetX != 0) {
            speed *= -1;
        }
        return speed;
    }

    public ForgeDirection getBeltFacing() {
        return facing;
    }

    public ForgeDirection getMovementFacing() {
        float speed = getBeltMovementSpeed();
        // Axis direction depends on facing and speed sign
        // For X-axis belts: positive speed → negative X (west)
        if (facing.offsetX != 0) {
            if (speed > 0) return facing.getOpposite();
            return facing;
        }
        // For Z-axis belts: positive speed → positive Z (south)
        if (speed > 0) return facing;
        return facing.getOpposite();
    }

    // --- IKineticTile overrides ---

    @Override
    public boolean hasShaftTowards(ForgeDirection face) {
        // MIDDLE segments never have shaft connections
        if (beltPart == BeltPart.MIDDLE) return false;
        // START and END: connection on faces along the rotation axis
        return MyDirection.Axis.fromForge(face) == rotationAxis;
    }

    @Override
    public float calculateStressApplied() {
        if (!isController()) return 0;
        // Full chain stress reported by the controller
        return beltLength * 1.0f;
    }

    // --- Tick ---

    @Override
    public void updateEntity() {
        super.updateEntity(); // handles kinetic network reconnection

        if (worldObj == null || worldObj.isRemote) return;

        // Validate chain integrity
        if (chainInvalid) {
            worldObj.setBlockToAir(xCoord, yCoord, zCoord);
            return;
        }

        // Only the controller ticks the inventory
        if (!isController()) return;

        getInventory().tick();
    }

    // --- NBT ---

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        boolean isCtrl = tag.getBoolean("IsController");
        if (isCtrl) {
            controller = null;
        } else if (tag.hasKey("Controller")) {
            controller = MyBlockPos.fromLong(tag.getLong("Controller"));
        }

        beltLength = tag.getInteger("Length");
        index = tag.getInteger("Index");
        beltPart = BeltPart.values()[tag.getInteger("BeltPart")];
        slope = BeltSlope.values()[tag.getInteger("BeltSlope")];
        facing = ForgeDirection.getOrientation(tag.getInteger("Facing"));

        if (isController() && tag.hasKey("Inventory")) {
            getInventory().read(tag.getCompoundTag("Inventory"));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);

        boolean isCtrl = isController();
        tag.setBoolean("IsController", isCtrl);
        if (!isCtrl && controller != null) {
            tag.setLong("Controller", controller.asLong());
        }
        tag.setInteger("Length", beltLength);
        tag.setInteger("Index", index);
        tag.setInteger("BeltPart", beltPart.ordinal());
        tag.setInteger("BeltSlope", slope.ordinal());
        tag.setInteger("Facing", facing.ordinal());

        if (isCtrl && inventory != null) {
            tag.setTag("Inventory", inventory.write());
        }
    }

    // --- Setters (used by BeltBlock during chain formation) ---

    public void setController(MyBlockPos controller) { this.controller = controller; }
    public void setIndex(int index) { this.index = index; }
    public void setBeltLength(int beltLength) { this.beltLength = beltLength; }
    public void setBeltPart(BeltPart part) { this.beltPart = part; }
    public void setBeltFacing(ForgeDirection facing) { this.facing = facing; }
    public MyBlockPos getController() { return controller; }
}
