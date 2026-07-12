package create.core.kinetic.belt;

import create.core.kinetic.KineticBlock;
import create.core.kinetic.belt.transport.BeltInventory;
import create.core.kinetic.belt.transport.TransportedItemStack;
import create.foundation.AllItems;
import create.shim.MyBlockPos;
import create.shim.MyDirection;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * A single belt segment, forming a horizontal multi-block chain.
 *
 * <p>Metadata encoding:
 * bits 0-1: facing (0=NORTH, 1=SOUTH, 2=WEST, 3=EAST)
 * bits 2-3: beltPart (0=START, 1=MIDDLE, 2=END)</p>
 *
 * <p>Chain formation happens on placement via {@link #tryFormChain}.
 * Breaking any segment destroys the entire chain (simplified from
 * the more sophisticated chain-splitting in official Create).</p>
 */
public class BeltBlock extends KineticBlock {

    // --- Metadata ---

    private static final int FACING_MASK = 0x03;
    private static final int PART_SHIFT = 2;
    private static final int PART_MASK = 0x03;

    public static ForgeDirection getFacingFromMeta(int meta) {
        int idx = meta & FACING_MASK;
        switch (idx) {
            case 0: return ForgeDirection.NORTH;
            case 1: return ForgeDirection.SOUTH;
            case 2: return ForgeDirection.WEST;
            case 3: return ForgeDirection.EAST;
            default: return ForgeDirection.NORTH;
        }
    }

    public static int facingToMeta(ForgeDirection dir) {
        switch (dir) {
            case NORTH: return 0;
            case SOUTH: return 1;
            case WEST:  return 2;
            case EAST:  return 3;
            default: return 0;
        }
    }

    public static BeltPart getPartFromMeta(int meta) {
        int idx = (meta >> PART_SHIFT) & PART_MASK;
        if (idx >= 0 && idx < BeltPart.values().length) {
            return BeltPart.values()[idx];
        }
        return BeltPart.START;
    }

    public static int partToMeta(BeltPart part, int facingMeta) {
        return facingMeta | (part.ordinal() << PART_SHIFT);
    }

    @Override
    public Class<? extends TileEntity> getTileEntityClass() {
        return BeltTileEntity.class;
    }

    public BeltBlock() {
        setBlockName("create:belt");
        setBlockTextureName("create:belt");
        setHarvestLevel("axe", 0);
        setHarvestLevel("pickaxe", 0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        super.registerBlockIcons(reg);
        registerTexture(reg, "belt");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return iconMap.get("belt");
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        setBlockBounds(0, 6 / 16f, 0, 1, 10 / 16f, 1);
    }

    @Override
    public void setBlockBoundsForItemRender() {
        setBlockBounds(0, 6 / 16f, 0, 1, 10 / 16f, 1);
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        BeltTileEntity te = new BeltTileEntity();
        te.setKineticType(create.core.kinetic.KineticBlockType.BELT);
        ForgeDirection facing = getFacingFromMeta(metadata);
        te.setBeltFacing(facing);
        te.setRotationAxis(beltFacingToAxis(facing));
        te.setBeltPart(getPartFromMeta(metadata));
        return te;
    }

    // --- Rotation axis ---

    /** Facing N/S → rotation axis X; facing E/W → rotation axis Z. */
    public static MyDirection.Axis beltFacingToAxis(ForgeDirection facing) {
        switch (facing) {
            case NORTH: case SOUTH: return MyDirection.Axis.X;
            case EAST:  case WEST:  return MyDirection.Axis.Z;
            default: return MyDirection.Axis.X;
        }
    }

    // --- Placement ---

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z,
                                 EntityLivingBase placer, ItemStack stack) {
        // Determine facing from player's horizontal look direction
        int facing = net.minecraft.util.MathHelper
                .floor_double((double) (placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        // facing: 0=south, 1=west, 2=north, 3=east
        // The belt travels in the direction the player is looking
        ForgeDirection dir;
        switch (facing) {
            case 0: dir = ForgeDirection.SOUTH; break;
            case 1: dir = ForgeDirection.WEST;  break;
            case 2: dir = ForgeDirection.NORTH; break;
            case 3: dir = ForgeDirection.EAST;  break;
            default: dir = ForgeDirection.NORTH;
        }
        world.setBlockMetadataWithNotify(x, y, z,
                partToMeta(BeltPart.START, facingToMeta(dir)), 2);

        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof BeltTileEntity) {
            BeltTileEntity bte = (BeltTileEntity) te;
            bte.setBeltFacing(dir);
            bte.setRotationAxis(beltFacingToAxis(dir));
            bte.setBeltPart(BeltPart.START);
            bte.setController(null); // self is controller (for now)
            bte.setIndex(0);
            bte.setBeltLength(1);
            bte.chainInvalid = false;
            tryFormChain(world, x, y, z);
            bte.updateSpeed = true;
        }
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof BeltTileEntity) {
                BeltTileEntity bte = (BeltTileEntity) te;
                int meta = world.getBlockMetadata(x, y, z);
                bte.setBeltFacing(getFacingFromMeta(meta));
                bte.setRotationAxis(beltFacingToAxis(getFacingFromMeta(meta)));
            }
        }
        // KineticBlock.onBlockAdded handles kinetic network join
        super.onBlockAdded(world, x, y, z);
    }

    // --- Destruction ---

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof BeltTileEntity) {
                BeltTileEntity broken = (BeltTileEntity) te;

                // Eject all items from the controller
                BeltTileEntity ctrl = broken.getControllerBE();
                if (ctrl == null) ctrl = broken;
                if (ctrl.getInventory() != null) {
                    ctrl.getInventory().ejectAll();
                }

                // Walk chain forward from controller and destroy all segments
                destroyChain(world, ctrl, broken.getBeltFacing());
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    /**
     * Destroy every belt segment in the chain starting from the controller.
     */
    private static void destroyChain(World world, BeltTileEntity controller,
                                      ForgeDirection facing) {
        // Walk to START first
        BeltTileEntity start;
        if (controller != null && controller.isController()) {
            start = controller;
        } else if (controller != null && controller.getControllerBE() != null) {
            start = controller.getControllerBE();
        } else {
            return; // can't find controller, individual segments will self-destruct
        }

        if (start == null) return;
        int x = start.xCoord, y = start.yCoord, z = start.zCoord;

        // Walk forward along the chain
        int max = 20; // safety limit
        MyBlockPos current = new MyBlockPos(x, y, z);
        for (int i = 0; i < max; i++) {
            if (!world.blockExists(current.getX(), current.getY(), current.getZ())) break;
            Block block = world.getBlock(current.getX(), current.getY(), current.getZ());
            if (!(block instanceof BeltBlock)) break;

            ItemStack drop = new ItemStack(AllItems.BELT_CONNECTOR, 1, 0);
            world.spawnEntityInWorld(new EntityItem(world,
                    current.getX() + 0.5, current.getY() + 0.5, current.getZ() + 0.5,
                    drop));

            world.setBlockToAir(current.getX(), current.getY(), current.getZ());
            current = current.add(facing.offsetX, facing.offsetY, facing.offsetZ);
        }
    }

    // --- Neighbor change ---

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        super.onNeighborBlockChange(world, x, y, z, neighbor);
        // Neighbor changes handled by super (kinetic connection re-scan)
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        if (world.isRemote || !(entity instanceof EntityItem) || entity.isDead) return;

        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof BeltTileEntity)) return;

        BeltTileEntity segment = (BeltTileEntity) te;
        BeltTileEntity controller = segment.getControllerBE();
        if (controller == null) return;

        BeltInventory inventory = controller.getInventory();
        if (inventory == null) return;

        EntityItem entityItem = (EntityItem) entity;
        ItemStack stack = entityItem.getEntityItem();
        if (stack == null || stack.stackSize <= 0) return;

        float position = BeltHelper.getBeltPositionForSegment(
                segment.getIndex(),
                controller.getBeltFacing(),
                entity.posX - x,
                entity.posZ - z);
        position = Math.max(0, Math.min(position, controller.getBeltLength()));
        if (!inventory.canInsertAt(position)) return;

        TransportedItemStack transported = new TransportedItemStack();
        transported.stack = stack.copy();
        transported.beltPosition = position;
        transported.prevBeltPosition = position;
        transported.insertedAt = segment.getIndex();
        transported.insertedFrom = ForgeDirection.UP.ordinal();
        inventory.addItem(transported);

        entityItem.setDead();
    }

    // --- Chain formation ---

    /**
     * Try to merge the newly-placed belt at (x,y,z) into an adjacent chain.
     */
    public static void tryFormChain(World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof BeltTileEntity)) return;

        BeltTileEntity placed = (BeltTileEntity) te;
        ForgeDirection facing = placed.getBeltFacing();

        // Step 1: Look backward (opposite of facing) for an existing chain
        // If found, the placed belt extends it forward (becomes the new END)
        int backX = x - facing.offsetX;
        int backY = y;
        int backZ = z - facing.offsetZ;

        if (world.blockExists(backX, backY, backZ)) {
            TileEntity backTe = world.getTileEntity(backX, backY, backZ);
            if (backTe instanceof BeltTileEntity) {
                BeltTileEntity backBelt = (BeltTileEntity) backTe;
                if (backBelt.getBeltFacing() == facing) {
                    // Found backward belt with matching facing — extend its chain
                    BeltTileEntity ctrl = backBelt.getControllerBE();
                    if (ctrl == null) ctrl = backBelt;

                    // Walk to the current END of the chain
                    MyBlockPos endPos = ctrl.getKineticPos();
                    for (int i = 0; i < ctrl.beltLength - 1; i++) {
                        endPos = endPos.add(facing.offsetX, 0, facing.offsetZ);
                    }

                    // Extend chain: update all segments
                    int newLength = ctrl.beltLength + 1;
                    updateChain(world, ctrl.getKineticPos(), facing, newLength);

                    // Force network reconnection on controller
                    ctrl.updateSpeed = true;
                    return;
                }
            }
        }

        // Step 2: Look forward (along facing) for an existing chain
        // If found, the placed belt becomes the new START
        int fwdX = x + facing.offsetX;
        int fwdY = y;
        int fwdZ = z + facing.offsetZ;

        if (world.blockExists(fwdX, fwdY, fwdZ)) {
            TileEntity fwdTe = world.getTileEntity(fwdX, fwdY, fwdZ);
            if (fwdTe instanceof BeltTileEntity) {
                BeltTileEntity fwdBelt = (BeltTileEntity) fwdTe;
                if (fwdBelt.getBeltFacing() == facing) {
                    // Found forward belt — placed belt becomes new START
                    BeltTileEntity oldCtrl = fwdBelt.getControllerBE();
                    if (oldCtrl == null) oldCtrl = fwdBelt;

                    int newLength = oldCtrl.beltLength + 1;

                    // The placed belt is the new controller/START
                    placed.setController(null);
                    placed.setIndex(0);
                    placed.setBeltLength(newLength);
                    placed.setBeltPart(BeltPart.START);
                    world.setBlockMetadataWithNotify(x, y, z,
                            partToMeta(BeltPart.START, facingToMeta(facing)), 2);

                    // Re-index downstream segments (+1)
                    MyBlockPos current = new MyBlockPos(fwdX, fwdY, fwdZ);
                    for (int i = 1; i < newLength; i++) {
                        if (!world.blockExists(current.getX(), current.getY(),
                                current.getZ())) break;
                        TileEntity segTe = world.getTileEntity(
                                current.getX(), current.getY(), current.getZ());
                        if (segTe instanceof BeltTileEntity) {
                            BeltTileEntity seg = (BeltTileEntity) segTe;
                            seg.setController(placed.getKineticPos());
                            seg.setIndex(i);
                            seg.setBeltLength(newLength);
                            if (i == newLength - 1) {
                                seg.setBeltPart(BeltPart.END);
                            } else {
                                seg.setBeltPart(BeltPart.MIDDLE);
                            }
                            seg.markDirty();
                            world.setBlockMetadataWithNotify(
                                    current.getX(), current.getY(), current.getZ(),
                                    partToMeta(seg.beltPart, facingToMeta(facing)), 2);
                        }
                        current = current.add(facing.offsetX, 0, facing.offsetZ);
                    }

                    // Force network reconnection on the new controller
                    placed.updateSpeed = true;
                    return;
                }
            }
        }

        // Step 3: No adjacent belts found. Keep this as a valid standalone
        // segment so players can place belts naturally before extending them.
        placed.setController(null);
        placed.setIndex(0);
        placed.setBeltLength(1);
        placed.setBeltPart(BeltPart.START);
        placed.chainInvalid = false;
    }

    /**
     * Update all segments of a chain after extension.
     */
    private static void updateChain(World world, MyBlockPos startPos,
                                     ForgeDirection facing, int newLength) {
        MyBlockPos current = startPos;
        for (int i = 0; i < newLength; i++) {
            if (!world.blockExists(current.getX(), current.getY(), current.getZ())) break;

            TileEntity te = world.getTileEntity(
                    current.getX(), current.getY(), current.getZ());
            if (te instanceof BeltTileEntity) {
                BeltTileEntity seg = (BeltTileEntity) te;
                seg.setController(i == 0 ? null : startPos);
                seg.setIndex(i);
                seg.setBeltLength(newLength);
                if (i == 0) {
                    seg.setBeltPart(BeltPart.START);
                } else if (i == newLength - 1) {
                    seg.setBeltPart(BeltPart.END);
                } else {
                    seg.setBeltPart(BeltPart.MIDDLE);
                }
                seg.chainInvalid = false;
                seg.markDirty();
                world.setBlockMetadataWithNotify(
                        current.getX(), current.getY(), current.getZ(),
                        partToMeta(seg.beltPart,
                                facingToMeta(seg.getBeltFacing())), 2);
            }
            current = current.add(facing.offsetX, 0, facing.offsetZ);
        }
    }

    // --- Chain traversal ---

    /**
     * Walk forward from controller along facing, collecting all belt positions.
     */
    public static List<MyBlockPos> getBeltChain(World world, MyBlockPos start,
                                                  ForgeDirection facing) {
        List<MyBlockPos> chain = new ArrayList<>();
        MyBlockPos current = start;
        int max = 20;
        for (int i = 0; i < max; i++) {
            if (!world.blockExists(current.getX(), current.getY(), current.getZ())) break;
            Block block = world.getBlock(current.getX(), current.getY(), current.getZ());
            if (!(block instanceof BeltBlock)) break;
            if (getFacingFromMeta(world.getBlockMetadata(
                    current.getX(), current.getY(), current.getZ())) != facing) break;
            chain.add(current);
            current = current.add(facing.offsetX, 0, facing.offsetZ);
        }
        return chain;
    }

    // --- hasTileEntity ---

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }
}
