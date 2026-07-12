package create.core.kinetic;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Base class for all kinetic blocks. Handles TileEntity creation and
 * kinetic network lifecycle hooks (place, break, neighbor change).
 */
public abstract class KineticBlock extends Block {

    protected KineticBlock() {
        super(Material.rock);
        setHardness(3.0F);
        setResistance(10.0F);
        setStepSound(soundTypeWood);
    }

    // --- Rendering ---

    @SideOnly(Side.CLIENT)
    protected final Map<String, IIcon> iconMap = new HashMap<>();

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        // Ensure blockIcon is still set from the texture name set via setBlockTextureName
        super.registerBlockIcons(reg);
        // Subclass calls registerTexture() in its override
    }

    protected void registerTexture(IIconRegister reg, String name) {
        iconMap.put(name, reg.registerIcon("create:" + name));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        // Default: return first registered icon
        if (iconMap.isEmpty()) return null;
        return iconMap.values().iterator().next();
    }

    @Override
    public int getRenderType() {
        return create.foundation.render.KineticRenderer.RENDER_ID;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return AxisAlignedBB.getBoundingBox(
                x + minX, y + minY, z + minZ,
                x + maxX, y + maxY, z + maxZ);
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    /** Returns the TileEntity class for registration wiring. */
    public abstract Class<? extends TileEntity> getTileEntityClass();

    // --- Placement ---

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        if (world.isRemote) return;

        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof IKineticTile)) return;
        IKineticTile kinetic = (IKineticTile) te;

        // Auto-detect connections to adjacent kinetic blocks
        autoConnect(world, x, y, z, kinetic);

        // Join or create the kinetic network
        RotationPropagator.handleAdded(world, kinetic);
    }

    // --- Removal ---

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof IKineticTile) {
                IKineticTile kinetic = (IKineticTile) te;
                RotationPropagator.handleRemoved(
                        world, new create.shim.MyBlockPos(x, y, z), kinetic);

                // Clear connections on neighbors
                clearNeighborConnections(world, x, y, z, kinetic);
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    // --- Neighbor change ---

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        if (world.isRemote) return;

        // When a neighbor changes, re-validate our connections and network
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof IKineticTile) {
            IKineticTile kinetic = (IKineticTile) te;

            // Re-scan connections
            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                int nx = x + dir.offsetX;
                int ny = y + dir.offsetY;
                int nz = z + dir.offsetZ;
                TileEntity neighborTe = world.getTileEntity(nx, ny, nz);

                boolean shouldConnect = neighborTe instanceof IKineticTile
                        && canConnectTo(kinetic, (IKineticTile) neighborTe, dir);
                boolean wasConnected = kinetic.isConnected(dir);

                if (shouldConnect != wasConnected) {
                    kinetic.setConnected(dir, shouldConnect);
                    // Also update the neighbor's side
                    if (shouldConnect) {
                        ((IKineticTile) neighborTe).setConnected(dir.getOpposite(), true);
                    }
                }
            }

            // Trigger network revalidation
            KineticTileEntity kte = (te instanceof KineticTileEntity)
                    ? (KineticTileEntity) te : null;
            if (kte != null) {
                kte.updateSpeed = true;
            }
        }
    }

    // --- Connection helpers ---

    /**
     * Scan all 6 neighbors and set up bidirectional connections.
     */
    private static void autoConnect(World world, int x, int y, int z,
                                     IKineticTile kinetic) {
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            int nx = x + dir.offsetX;
            int ny = y + dir.offsetY;
            int nz = z + dir.offsetZ;
            TileEntity neighborTe = world.getTileEntity(nx, ny, nz);
            if (!(neighborTe instanceof IKineticTile)) continue;

            IKineticTile neighbor = (IKineticTile) neighborTe;
            if (canConnectTo(kinetic, neighbor, dir)) {
                kinetic.setConnected(dir, true);
                neighbor.setConnected(dir.getOpposite(), true);
            }
        }
    }

    /**
     * Clear our connections from neighbors who we were connected to.
     */
    private static void clearNeighborConnections(World world, int x, int y, int z,
                                                  IKineticTile kinetic) {
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if (!kinetic.isConnected(dir)) continue;
            int nx = x + dir.offsetX;
            int ny = y + dir.offsetY;
            int nz = z + dir.offsetZ;
            TileEntity neighborTe = world.getTileEntity(nx, ny, nz);
            if (neighborTe instanceof IKineticTile) {
                ((IKineticTile) neighborTe).setConnected(dir.getOpposite(), false);
            }
        }
    }

    /**
     * Check whether two kinetic tiles can connect through the given face.
     *
     * <p>Shafts connect inline (same axis), cogwheels connect on any
     * perpendicular face. Two shafts on different axes do not connect.</p>
     *
     * <p>Sources (motors, water wheels) only connect via their shaft faces
     * — they do not connect on every face like cogwheels.</p>
     *
     * <p>Belts connect only to shafts on their perpendicular rotation axis,
     * and only via START/END segments (MIDDLE has no shaft connection).</p>
     */
    protected static boolean canConnectTo(IKineticTile from, IKineticTile to,
                                           ForgeDirection face) {
        // Belt connections: belts only connect to shafts on their rotation axis
        if (from.getKineticType() == KineticBlockType.BELT
                || to.getKineticType() == KineticBlockType.BELT) {
            IKineticTile belt = (from.getKineticType() == KineticBlockType.BELT) ? from : to;
            IKineticTile other = (belt == from) ? to : from;
            ForgeDirection beltFace = (belt == from) ? face : face.getOpposite();

            // Belts only connect to shafts
            if (other.getKineticType() != KineticBlockType.SHAFT) return false;

            // Rotation axes must match
            if (belt.getRotationAxis() != other.getRotationAxis()) return false;

            // Belt face must be along the rotation axis
            if (create.shim.MyDirection.Axis.fromForge(beltFace) != belt.getRotationAxis())
                return false;

            // Only START and END segments have shaft connection
            return belt.hasShaftTowards(beltFace);
        }

        // Source blocks: connect only via their shaft faces
        if (from.getKineticType() == KineticBlockType.SOURCE) {
            return from.hasShaftTowards(face);
        }
        if (to.getKineticType() == KineticBlockType.SOURCE) {
            return to.hasShaftTowards(face.getOpposite());
        }

        // Shaft-to-shaft: must share the same rotation axis
        if (from.getKineticType() == KineticBlockType.SHAFT
                && to.getKineticType() == KineticBlockType.SHAFT) {
            if (from.getRotationAxis() != to.getRotationAxis()) {
                return false;
            }
            // The face must be along the shared axis
            return face.ordinal() == axisDirectionOrdinal(from.getRotationAxis())
                    || face.ordinal() == axisOppositeOrdinal(from.getRotationAxis());
        }

        // Cogwheel ↔ anything: always connect (perpendicular coupling)
        return true;
    }

    private static int axisDirectionOrdinal(create.shim.MyDirection.Axis axis) {
        return switch (axis) {
            case Y -> ForgeDirection.UP.ordinal();
            case Z -> ForgeDirection.SOUTH.ordinal();
            case X -> ForgeDirection.EAST.ordinal();
        };
    }

    private static int axisOppositeOrdinal(create.shim.MyDirection.Axis axis) {
        return switch (axis) {
            case Y -> ForgeDirection.DOWN.ordinal();
            case Z -> ForgeDirection.NORTH.ordinal();
            case X -> ForgeDirection.WEST.ordinal();
        };
    }
}
