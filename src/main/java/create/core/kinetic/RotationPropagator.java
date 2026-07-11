package create.core.kinetic;

import create.shim.MyBlockPos;
import create.shim.MyDirection;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * BFS-based rotation speed propagation through the kinetic network.
 *
 * <p>When a kinetic block is placed or removed, this class traverses the
 * connected graph, assigns speed/source/network to each reachable tile,
 * and recalculates aggregated stress and capacity.</p>
 *
 * <p>Speed modifiers between connected blocks depend on their types:</p>
 * <ul>
 *   <li>Shaft → Shaft (same axis): ×1</li>
 *   <li>Cogwheel → Cogwheel (perpendicular): ×−1</li>
 *   <li>Large Cogwheel → Small Cogwheel: ×−2 (doubles speed)</li>
 *   <li>Small Cogwheel → Large Cogwheel: ×−0.5 (halves speed)</li>
 * </ul>
 */
public final class RotationPropagator {

    private static final int MAX_NETWORK_SIZE = 512;

    private RotationPropagator() {}

    // --- Public entry points ---

    /**
     * Called when a kinetic tile entity is added to the world (block placed).
     */
    public static void handleAdded(World world, IKineticTile added) {
        if (added.isSource()) {
            createNetwork(world, added);
            propagateFrom(world, added, new HashSet<>());
        } else {
            findAndAttach(world, added);
        }
    }

    /**
     * Called when a kinetic tile entity is removed from the world (block broken).
     */
    public static void handleRemoved(World world, MyBlockPos removedPos, IKineticTile removed) {
        int dimId = removed.getDimensionId();
        Long oldNetworkId = removed.getNetworkId();
        boolean removedIsSource = removed.isSource();

        if (oldNetworkId != null) {
            KineticNetwork network = KineticNetworkManager.getNetwork(oldNetworkId, dimId);
            if (network != null) {
                network.remove(removed);
                if (network.isEmpty()) {
                    KineticNetworkManager.removeIfEmpty(oldNetworkId, dimId);
                }
            }
        }

        // Find neighbors that depended on the removed tile
        List<IKineticTile> orphaned = new ArrayList<>();
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if (!removed.isConnected(dir)) continue;
            MyBlockPos neighborPos = removedPos.offset(fromForge(dir));
            IKineticTile neighbor = getKineticTile(world, neighborPos);
            if (neighbor == null) continue;

            if (removedIsSource) {
                // Sources set their own sourcePosition to null (self-powered),
                // so no downstream tile lists the source as its sourcePosition.
                // Collect all directly-connected neighbors sharing the same network.
                if (neighbor.getNetworkId() != null
                        && neighbor.getNetworkId().equals(oldNetworkId)) {
                    orphaned.add(neighbor);
                }
            } else {
                // For non-source tiles, downstream tiles have sourcePosition pointing here
                if (neighbor.getSourcePosition()
                        .map(sp -> sp.equals(removedPos))
                        .orElse(false)) {
                    orphaned.add(neighbor);
                }
            }
        }

        // BFS clear orphaned nodes
        Set<MyBlockPos> cleared = new HashSet<>();
        for (IKineticTile orphan : orphaned) {
            clearNetwork(orphan, cleared);
        }

        // Re-propagate from any sources that remain in the affected area
        Set<MyBlockPos> repropagated = new HashSet<>();
        for (MyBlockPos pos : cleared) {
            IKineticTile te = getKineticTile(world, pos);
            if (te != null && te.isSource()) {
                createNetwork(world, te);
                propagateFrom(world, te, repropagated);
            }
        }

        // Any cleared TEs that weren't repowered try to find a neighbor source
        for (MyBlockPos pos : cleared) {
            if (repropagated.contains(pos)) continue;
            IKineticTile te = getKineticTile(world, pos);
            if (te != null && te.getNetworkId() == null) {
                findAndAttach(world, te);
            }
        }
    }

    // --- Speed modifier calculation ---

    /**
     * Compute the speed multiplier when {@code from} drives {@code to}
     * through the given face.
     */
    public static float getRotationSpeedModifier(IKineticTile from, IKineticTile to,
                                                  ForgeDirection face,
                                                  ForgeDirection oppositeFace) {
        boolean fromShaft = from.hasShaftTowards(face);
        boolean toShaft = to.hasShaftTowards(oppositeFace);

        if (fromShaft && toShaft) {
            // Direct shaft connection along the same axis — speed passes through
            return 1.0f;
        }

        KineticBlockType fromType = from.getKineticType();
        KineticBlockType toType = to.getKineticType();

        // Belt connections: always same-axis shaft connection, speed passes through 1:1
        if (fromType == KineticBlockType.BELT || toType == KineticBlockType.BELT) {
            return 1.0f;
        }

        // Large → Small: double speed, reverse direction
        if (fromType == KineticBlockType.LARGE_COGWHEEL
                && toType == KineticBlockType.SMALL_COGWHEEL) {
            return -2.0f;
        }

        // Small → Large: halve speed, reverse direction
        if (fromType == KineticBlockType.SMALL_COGWHEEL
                && toType == KineticBlockType.LARGE_COGWHEEL) {
            return -0.5f;
        }

        // All other cogwheel/cross connections: reverse direction, same speed
        return -1.0f;
    }

    // --- Internal propagation ---

    /**
     * Set up a new network centered on a generator.
     */
    private static void createNetwork(World world, IKineticTile source) {
        float genSpeed = source.getGeneratedSpeed();
        source.setSpeed(genSpeed);
        Long networkId = source.getKineticPos().asLong();
        source.setNetworkId(networkId);
        source.setSourcePosition(null);

        KineticNetwork network = KineticNetworkManager.getOrCreateNetwork(
                networkId, source.getDimensionId());
        network.add(source, genSpeed);
        network.updateFromSpeed(genSpeed);
    }

    /**
     * BFS from a source, assigning speed and network to all connected tiles.
     */
    private static void propagateFrom(World world, IKineticTile source,
                                      Set<MyBlockPos> visited) {
        Deque<IKineticTile> queue = new ArrayDeque<>();
        queue.add(source);
        visited.add(source.getKineticPos());

        int count = 0;
        while (!queue.isEmpty() && count < MAX_NETWORK_SIZE) {
            IKineticTile current = queue.poll();
            count++;

            float currentSpeed = current.getSpeed();
            Long networkId = current.getNetworkId();
            int dimId = current.getDimensionId();
            KineticNetwork network = KineticNetworkManager.getOrCreateNetwork(
                    networkId != null ? networkId : -1L, dimId);

            for (ForgeDirection face : ForgeDirection.VALID_DIRECTIONS) {
                if (!current.isConnected(face)) continue;

                MyBlockPos currentPos = current.getKineticPos();
                MyBlockPos neighborPos = offsetByForge(currentPos, face);
                IKineticTile neighbor = getKineticTile(world, neighborPos);
                if (neighbor == null) continue;
                if (visited.contains(neighborPos)) continue;

                ForgeDirection oppositeFace = face.getOpposite();
                if (!neighbor.isConnected(oppositeFace)) continue;

                // Skip if neighbor is a source with its own network
                if (neighbor.isSource() && neighbor.getGeneratedSpeed() != 0) {
                    continue;
                }

                float modifier = getRotationSpeedModifier(current, neighbor, face, oppositeFace);
                float newSpeed = currentSpeed * modifier;

                if (Math.abs(newSpeed) < 0.001f) continue;

                neighbor.setSpeed(newSpeed);
                neighbor.setSourcePosition(currentPos);
                neighbor.setNetworkId(networkId);

                network.add(neighbor, newSpeed);

                visited.add(neighborPos);
                queue.add(neighbor);
            }

            // Also check diagonal neighbors for cogwheel connections
            checkDiagonalNeighbors(world, current, currentSpeed, network, visited, queue);
        }

        // Final stress/capacity update at the current speed
        if (source.getNetworkId() != null) {
            KineticNetwork net = KineticNetworkManager.getNetwork(
                    source.getNetworkId(), source.getDimensionId());
            if (net != null) {
                net.updateFromSpeed(source.getSpeed());
            }
        }
    }

    /**
     * Check the 12 diagonal-offset positions for cogwheel connections.
     */
    private static void checkDiagonalNeighbors(World world, IKineticTile current,
                                                float currentSpeed,
                                                KineticNetwork network,
                                                Set<MyBlockPos> visited,
                                                Deque<IKineticTile> queue) {
        MyBlockPos pos = current.getKineticPos();
        int dimId = current.getDimensionId();
        Long networkId = current.getNetworkId();

        // All offsets where exactly two coords are ±1 and one is 0 (distSq == 2)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    int nonZero = (dx != 0 ? 1 : 0) + (dy != 0 ? 1 : 0) + (dz != 0 ? 1 : 0);
                    if (nonZero != 2) continue;

                    MyBlockPos neighborPos = pos.add(dx, dy, dz);
                    if (visited.contains(neighborPos)) continue;

                    IKineticTile neighbor = getKineticTile(world, neighborPos);
                    if (neighbor == null) continue;
                    if (neighbor.isSource() && neighbor.getGeneratedSpeed() != 0) continue;

                    // Both must be cogwheels for diagonal connection
                    KineticBlockType ct = current.getKineticType();
                    KineticBlockType nt = neighbor.getKineticType();

                    // Belts do not have diagonal connections — skip belt types
                    if (ct == KineticBlockType.BELT || nt == KineticBlockType.BELT) continue;

                    boolean currentIsCog = ct == KineticBlockType.SMALL_COGWHEEL
                            || ct == KineticBlockType.LARGE_COGWHEEL;
                    boolean neighborIsCog = nt == KineticBlockType.SMALL_COGWHEEL
                            || nt == KineticBlockType.LARGE_COGWHEEL;

                    if (!currentIsCog || !neighborIsCog) continue;

                    // Axes must be perpendicular
                    if (current.getRotationAxis() == neighbor.getRotationAxis()) continue;

                    // Determine modifier based on sizes
                    float modifier;
                    if (ct == KineticBlockType.LARGE_COGWHEEL
                            && nt == KineticBlockType.SMALL_COGWHEEL) {
                        modifier = -2.0f;
                    } else if (ct == KineticBlockType.SMALL_COGWHEEL
                            && nt == KineticBlockType.LARGE_COGWHEEL) {
                        modifier = -0.5f;
                    } else {
                        modifier = -1.0f;
                    }

                    float newSpeed = currentSpeed * modifier;
                    if (Math.abs(newSpeed) < 0.001f) continue;

                    neighbor.setSpeed(newSpeed);
                    neighbor.setSourcePosition(pos);
                    neighbor.setNetworkId(networkId);
                    network.add(neighbor, newSpeed);

                    visited.add(neighborPos);
                    queue.add(neighbor);
                }
            }
        }
    }

    /**
     * Try to find a powered neighbor and attach to its network.
     */
    private static void findAndAttach(World world, IKineticTile te) {
        MyBlockPos pos = te.getKineticPos();

        for (ForgeDirection face : ForgeDirection.VALID_DIRECTIONS) {
            if (!te.isConnected(face)) continue;

            MyBlockPos neighborPos = offsetByForge(pos, face);
            IKineticTile neighbor = getKineticTile(world, neighborPos);
            if (neighbor == null) continue;
            if (!neighbor.isConnected(face.getOpposite())) continue;
            if (neighbor.getSpeed() == 0 && !neighbor.isSource()) continue;

            float neighborSpeed = neighbor.isSource()
                    ? neighbor.getGeneratedSpeed()
                    : neighbor.getSpeed();
            if (Math.abs(neighborSpeed) < 0.001f) continue;

            float modifier = getRotationSpeedModifier(neighbor, te, face.getOpposite(), face);
            float newSpeed = neighborSpeed * modifier;
            if (Math.abs(newSpeed) < 0.001f) continue;

            te.setSpeed(newSpeed);
            te.setSourcePosition(neighborPos);

            Long networkId = neighbor.getNetworkId();
            if (networkId == null && neighbor.isSource()) {
                networkId = neighbor.getKineticPos().asLong();
            }
            te.setNetworkId(networkId);

            KineticNetwork network = KineticNetworkManager.getOrCreateNetwork(
                    networkId != null ? networkId : -1L, te.getDimensionId());
            network.add(te, newSpeed);
            network.updateFromSpeed(newSpeed);
            return;
        }

        // Also check diagonal neighbors
        checkDiagonalForAttachment(world, te, pos);
    }

    private static void checkDiagonalForAttachment(World world, IKineticTile te,
                                                    MyBlockPos pos) {
        KineticBlockType tt = te.getKineticType();
        if (tt != KineticBlockType.SMALL_COGWHEEL
                && tt != KineticBlockType.LARGE_COGWHEEL) {
            return;
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    int nonZero = (dx != 0 ? 1 : 0) + (dy != 0 ? 1 : 0) + (dz != 0 ? 1 : 0);
                    if (nonZero != 2) continue;

                    MyBlockPos neighborPos = pos.add(dx, dy, dz);
                    IKineticTile neighbor = getKineticTile(world, neighborPos);
                    if (neighbor == null) continue;
                    if (neighbor.getSpeed() == 0 && !neighbor.isSource()) continue;

                    KineticBlockType nt = neighbor.getKineticType();
                    boolean neighborIsCog = nt == KineticBlockType.SMALL_COGWHEEL
                            || nt == KineticBlockType.LARGE_COGWHEEL;
                    if (!neighborIsCog) continue;
                    if (te.getRotationAxis() == neighbor.getRotationAxis()) continue;

                    float neighborSpeed = neighbor.isSource()
                            ? neighbor.getGeneratedSpeed()
                            : neighbor.getSpeed();
                    if (Math.abs(neighborSpeed) < 0.001f) continue;

                    float modifier;
                    if (nt == KineticBlockType.LARGE_COGWHEEL
                            && tt == KineticBlockType.SMALL_COGWHEEL) {
                        modifier = -2.0f;
                    } else if (nt == KineticBlockType.SMALL_COGWHEEL
                            && tt == KineticBlockType.LARGE_COGWHEEL) {
                        modifier = -0.5f;
                    } else {
                        modifier = -1.0f;
                    }

                    float newSpeed = neighborSpeed * modifier;
                    if (Math.abs(newSpeed) < 0.001f) continue;

                    te.setSpeed(newSpeed);
                    te.setSourcePosition(neighborPos);

                    Long networkId = neighbor.getNetworkId();
                    if (networkId == null && neighbor.isSource()) {
                        networkId = neighbor.getKineticPos().asLong();
                    }
                    te.setNetworkId(networkId);

                    KineticNetwork network = KineticNetworkManager.getOrCreateNetwork(
                            networkId != null ? networkId : -1L, te.getDimensionId());
                    network.add(te, newSpeed);
                    network.updateFromSpeed(newSpeed);
                    return;
                }
            }
        }
    }

    /**
     * BFS clear: remove source and network from {@code te} and all tiles
     * downstream of it.
     */
    private static void clearNetwork(IKineticTile te, Set<MyBlockPos> cleared) {
        Deque<IKineticTile> queue = new ArrayDeque<>();
        queue.add(te);

        while (!queue.isEmpty()) {
            IKineticTile current = queue.poll();
            MyBlockPos currentPos = current.getKineticPos();
            if (!cleared.add(currentPos)) continue;

            // Remove from old network
            if (current.getNetworkId() != null) {
                KineticNetwork network = KineticNetworkManager.getNetwork(
                        current.getNetworkId(), current.getDimensionId());
                if (network != null) {
                    network.remove(current);
                    if (network.isEmpty()) {
                        KineticNetworkManager.removeIfEmpty(
                                current.getNetworkId(), current.getDimensionId());
                    }
                }
            }

            current.removeSource();
            current.setSpeed(0);
            current.setNetworkId(null);

            // Queue downstream neighbors (those that list 'current' as their source)
            for (ForgeDirection face : ForgeDirection.VALID_DIRECTIONS) {
                MyBlockPos neighborPos = offsetByForge(currentPos, face);
                IKineticTile neighbor = getKineticTileByPos(current, neighborPos);
                if (neighbor == null) continue;

                if (neighbor.getSourcePosition()
                        .map(sp -> sp.equals(currentPos))
                        .orElse(false)) {
                    queue.add(neighbor);
                }
            }
        }
    }

    // --- World access helpers ---

    /** Get a kinetic tile at the given position. */
    private static IKineticTile getKineticTile(World world, MyBlockPos pos) {
        TileEntity te = world.getTileEntity(pos.getX(), pos.getY(), pos.getZ());
        if (te instanceof IKineticTile) {
            return (IKineticTile) te;
        }
        return null;
    }

    /**
     * Get a kinetic tile at the given position, using another tile's world
     * context. Used in clearNetwork where we may not have a World reference
     * directly but can reach through the tile.
     */
    private static IKineticTile getKineticTileByPos(IKineticTile context, MyBlockPos pos) {
        // This requires the TE to hold a World reference, which TileEntity does in 1.7.10
        // KineticTileEntity extends TileEntity which has worldObj
        // For non-TileEntity IKineticTile implementors, this returns null
        if (context instanceof TileEntity) {
            World world = ((TileEntity) context).getWorldObj();
            if (world != null) {
                return getKineticTile(world, pos);
            }
        }
        return null;
    }

    // --- Coordinate helpers ---

    /** Convert a ForgeDirection to a MyDirection for offset calculations. */
    private static MyDirection fromForge(ForgeDirection dir) {
        return switch (dir) {
            case DOWN -> MyDirection.DOWN;
            case UP -> MyDirection.UP;
            case NORTH -> MyDirection.NORTH;
            case SOUTH -> MyDirection.SOUTH;
            case WEST -> MyDirection.WEST;
            case EAST -> MyDirection.EAST;
            default -> MyDirection.UP;
        };
    }

    /** Offset a MyBlockPos by one block in the given ForgeDirection. */
    private static MyBlockPos offsetByForge(MyBlockPos pos, ForgeDirection dir) {
        return pos.add(dir.offsetX, dir.offsetY, dir.offsetZ);
    }

    // --- Test support ---

    /**
     * Package-private overload for unit testing. Accepts a lookup function
     * instead of a World, so tests can provide a stub neighbor map.
     */
    public static void handleAdded(IKineticTile added,
                            java.util.function.Function<MyBlockPos, IKineticTile> lookup) {
        if (added.isSource()) {
            createNetwork(added, lookup);
            propagateFrom(added, lookup, new HashSet<>());
        } else {
            findAndAttach(added, lookup);
        }
    }

    public static void handleRemoved(MyBlockPos removedPos, IKineticTile removed,
                              java.util.function.Function<MyBlockPos, IKineticTile> lookup) {
        int dimId = removed.getDimensionId();
        Long oldNetworkId = removed.getNetworkId();
        boolean removedIsSource = removed.isSource();

        if (oldNetworkId != null) {
            KineticNetwork network = KineticNetworkManager.getNetwork(oldNetworkId, dimId);
            if (network != null) {
                network.remove(removed);
                if (network.isEmpty()) {
                    KineticNetworkManager.removeIfEmpty(oldNetworkId, dimId);
                }
            }
        }

        List<IKineticTile> orphaned = new ArrayList<>();
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if (!removed.isConnected(dir)) continue;
            MyBlockPos neighborPos = removedPos.offset(fromForge(dir));
            IKineticTile neighbor = lookup.apply(neighborPos);
            if (neighbor == null) continue;
            if (removedIsSource) {
                if (neighbor.getNetworkId() != null
                        && neighbor.getNetworkId().equals(oldNetworkId)) {
                    orphaned.add(neighbor);
                }
            } else {
                if (neighbor.getSourcePosition()
                        .map(sp -> sp.equals(removedPos))
                        .orElse(false)) {
                    orphaned.add(neighbor);
                }
            }
        }

        Set<MyBlockPos> cleared = new HashSet<>();
        for (IKineticTile orphan : orphaned) {
            clearNetworkStatic(orphan, cleared, lookup);
        }

        Set<MyBlockPos> repropagated = new HashSet<>();
        for (MyBlockPos pos : cleared) {
            IKineticTile te = lookup.apply(pos);
            if (te != null && te.isSource()) {
                createNetwork(te, lookup);
                propagateFrom(te, lookup, repropagated);
            }
        }

        for (MyBlockPos pos : cleared) {
            if (repropagated.contains(pos)) continue;
            IKineticTile te = lookup.apply(pos);
            if (te != null && te.getNetworkId() == null) {
                findAndAttach(te, lookup);
            }
        }
    }

    // --- Test-support internal methods (no World dependency) ---

    private static void createNetwork(IKineticTile source,
                                       java.util.function.Function<MyBlockPos, IKineticTile> lookup) {
        float genSpeed = source.getGeneratedSpeed();
        source.setSpeed(genSpeed);
        Long networkId = source.getKineticPos().asLong();
        source.setNetworkId(networkId);
        source.setSourcePosition(null);

        KineticNetwork network = KineticNetworkManager.getOrCreateNetwork(
                networkId, source.getDimensionId());
        network.add(source, genSpeed);
        network.updateFromSpeed(genSpeed);
    }

    private static void propagateFrom(IKineticTile source,
                                       java.util.function.Function<MyBlockPos, IKineticTile> lookup,
                                       Set<MyBlockPos> visited) {
        Deque<IKineticTile> queue = new ArrayDeque<>();
        queue.add(source);
        visited.add(source.getKineticPos());

        int count = 0;
        while (!queue.isEmpty() && count < MAX_NETWORK_SIZE) {
            IKineticTile current = queue.poll();
            count++;

            float currentSpeed = current.getSpeed();
            Long networkId = current.getNetworkId();
            int dimId = current.getDimensionId();
            KineticNetwork network = KineticNetworkManager.getOrCreateNetwork(
                    networkId != null ? networkId : -1L, dimId);

            for (ForgeDirection face : ForgeDirection.VALID_DIRECTIONS) {
                if (!current.isConnected(face)) continue;

                MyBlockPos currentPos = current.getKineticPos();
                MyBlockPos neighborPos = offsetByForge(currentPos, face);
                IKineticTile neighbor = lookup.apply(neighborPos);
                if (neighbor == null) continue;
                if (visited.contains(neighborPos)) continue;

                ForgeDirection oppositeFace = face.getOpposite();
                if (!neighbor.isConnected(oppositeFace)) continue;

                if (neighbor.isSource() && neighbor.getGeneratedSpeed() != 0) continue;

                float modifier = getRotationSpeedModifier(current, neighbor, face, oppositeFace);
                float newSpeed = currentSpeed * modifier;

                if (Math.abs(newSpeed) < 0.001f) continue;

                neighbor.setSpeed(newSpeed);
                neighbor.setSourcePosition(currentPos);
                neighbor.setNetworkId(networkId);

                network.add(neighbor, newSpeed);

                visited.add(neighborPos);
                queue.add(neighbor);
            }

            checkDiagonalNeighbors(lookup, current, currentSpeed, network, visited, queue);
        }

        if (source.getNetworkId() != null) {
            KineticNetwork net = KineticNetworkManager.getNetwork(
                    source.getNetworkId(), source.getDimensionId());
            if (net != null) {
                net.updateFromSpeed(source.getSpeed());
            }
        }
    }

    private static void checkDiagonalNeighbors(
            java.util.function.Function<MyBlockPos, IKineticTile> lookup,
            IKineticTile current, float currentSpeed,
            KineticNetwork network, Set<MyBlockPos> visited,
            Deque<IKineticTile> queue) {
        MyBlockPos pos = current.getKineticPos();
        Long networkId = current.getNetworkId();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    int nonZero = (dx != 0 ? 1 : 0) + (dy != 0 ? 1 : 0) + (dz != 0 ? 1 : 0);
                    if (nonZero != 2) continue;

                    MyBlockPos neighborPos = pos.add(dx, dy, dz);
                    if (visited.contains(neighborPos)) continue;

                    IKineticTile neighbor = lookup.apply(neighborPos);
                    if (neighbor == null) continue;
                    if (neighbor.isSource() && neighbor.getGeneratedSpeed() != 0) continue;

                    KineticBlockType ct = current.getKineticType();
                    KineticBlockType nt = neighbor.getKineticType();
                    boolean currentIsCog = ct == KineticBlockType.SMALL_COGWHEEL
                            || ct == KineticBlockType.LARGE_COGWHEEL;
                    boolean neighborIsCog = nt == KineticBlockType.SMALL_COGWHEEL
                            || nt == KineticBlockType.LARGE_COGWHEEL;
                    if (!currentIsCog || !neighborIsCog) continue;
                    if (current.getRotationAxis() == neighbor.getRotationAxis()) continue;

                    float modifier;
                    if (ct == KineticBlockType.LARGE_COGWHEEL
                            && nt == KineticBlockType.SMALL_COGWHEEL) {
                        modifier = -2.0f;
                    } else if (ct == KineticBlockType.SMALL_COGWHEEL
                            && nt == KineticBlockType.LARGE_COGWHEEL) {
                        modifier = -0.5f;
                    } else {
                        modifier = -1.0f;
                    }

                    float newSpeed = currentSpeed * modifier;
                    if (Math.abs(newSpeed) < 0.001f) continue;

                    neighbor.setSpeed(newSpeed);
                    neighbor.setSourcePosition(pos);
                    neighbor.setNetworkId(networkId);
                    network.add(neighbor, newSpeed);

                    visited.add(neighborPos);
                    queue.add(neighbor);
                }
            }
        }
    }

    private static void findAndAttach(IKineticTile te,
                                       java.util.function.Function<MyBlockPos, IKineticTile> lookup) {
        MyBlockPos pos = te.getKineticPos();

        for (ForgeDirection face : ForgeDirection.VALID_DIRECTIONS) {
            if (!te.isConnected(face)) continue;

            MyBlockPos neighborPos = offsetByForge(pos, face);
            IKineticTile neighbor = lookup.apply(neighborPos);
            if (neighbor == null) continue;
            if (!neighbor.isConnected(face.getOpposite())) continue;
            if (neighbor.getSpeed() == 0 && !neighbor.isSource()) continue;

            float neighborSpeed = neighbor.isSource()
                    ? neighbor.getGeneratedSpeed()
                    : neighbor.getSpeed();
            if (Math.abs(neighborSpeed) < 0.001f) continue;

            float modifier = getRotationSpeedModifier(neighbor, te, face.getOpposite(), face);
            float newSpeed = neighborSpeed * modifier;
            if (Math.abs(newSpeed) < 0.001f) continue;

            te.setSpeed(newSpeed);
            te.setSourcePosition(neighborPos);

            Long networkId = neighbor.getNetworkId();
            if (networkId == null && neighbor.isSource()) {
                networkId = neighbor.getKineticPos().asLong();
            }
            te.setNetworkId(networkId);

            KineticNetwork network = KineticNetworkManager.getOrCreateNetwork(
                    networkId != null ? networkId : -1L, te.getDimensionId());
            network.add(te, newSpeed);
            network.updateFromSpeed(newSpeed);
            return;
        }

        checkDiagonalForAttachment(te, pos, lookup);
    }

    private static void checkDiagonalForAttachment(IKineticTile te, MyBlockPos pos,
                                                    java.util.function.Function<MyBlockPos, IKineticTile> lookup) {
        KineticBlockType tt = te.getKineticType();
        if (tt != KineticBlockType.SMALL_COGWHEEL && tt != KineticBlockType.LARGE_COGWHEEL) {
            return;
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    int nonZero = (dx != 0 ? 1 : 0) + (dy != 0 ? 1 : 0) + (dz != 0 ? 1 : 0);
                    if (nonZero != 2) continue;

                    MyBlockPos neighborPos = pos.add(dx, dy, dz);
                    IKineticTile neighbor = lookup.apply(neighborPos);
                    if (neighbor == null) continue;
                    if (neighbor.getSpeed() == 0 && !neighbor.isSource()) continue;

                    KineticBlockType nt = neighbor.getKineticType();
                    boolean neighborIsCog = nt == KineticBlockType.SMALL_COGWHEEL
                            || nt == KineticBlockType.LARGE_COGWHEEL;
                    if (!neighborIsCog) continue;
                    if (te.getRotationAxis() == neighbor.getRotationAxis()) continue;

                    float neighborSpeed = neighbor.isSource()
                            ? neighbor.getGeneratedSpeed()
                            : neighbor.getSpeed();
                    if (Math.abs(neighborSpeed) < 0.001f) continue;

                    float modifier;
                    if (nt == KineticBlockType.LARGE_COGWHEEL && tt == KineticBlockType.SMALL_COGWHEEL) {
                        modifier = -2.0f;
                    } else if (nt == KineticBlockType.SMALL_COGWHEEL && tt == KineticBlockType.LARGE_COGWHEEL) {
                        modifier = -0.5f;
                    } else {
                        modifier = -1.0f;
                    }

                    float newSpeed = neighborSpeed * modifier;
                    if (Math.abs(newSpeed) < 0.001f) continue;

                    te.setSpeed(newSpeed);
                    te.setSourcePosition(neighborPos);

                    Long networkId = neighbor.getNetworkId();
                    if (networkId == null && neighbor.isSource()) {
                        networkId = neighbor.getKineticPos().asLong();
                    }
                    te.setNetworkId(networkId);

                    KineticNetwork network = KineticNetworkManager.getOrCreateNetwork(
                            networkId != null ? networkId : -1L, te.getDimensionId());
                    network.add(te, newSpeed);
                    network.updateFromSpeed(newSpeed);
                    return;
                }
            }
        }
    }

    private static void clearNetworkStatic(IKineticTile te, Set<MyBlockPos> cleared,
                                            java.util.function.Function<MyBlockPos, IKineticTile> lookup) {
        Deque<IKineticTile> queue = new ArrayDeque<>();
        queue.add(te);

        while (!queue.isEmpty()) {
            IKineticTile current = queue.poll();
            MyBlockPos currentPos = current.getKineticPos();
            if (!cleared.add(currentPos)) continue;

            if (current.getNetworkId() != null) {
                KineticNetwork network = KineticNetworkManager.getNetwork(
                        current.getNetworkId(), current.getDimensionId());
                if (network != null) {
                    network.remove(current);
                    if (network.isEmpty()) {
                        KineticNetworkManager.removeIfEmpty(
                                current.getNetworkId(), current.getDimensionId());
                    }
                }
            }

            current.removeSource();
            current.setSpeed(0);
            current.setNetworkId(null);

            for (ForgeDirection face : ForgeDirection.VALID_DIRECTIONS) {
                MyBlockPos neighborPos = offsetByForge(currentPos, face);
                IKineticTile neighbor = lookup.apply(neighborPos);
                if (neighbor == null) continue;

                if (neighbor.getSourcePosition()
                        .map(sp -> sp.equals(currentPos))
                        .orElse(false)) {
                    queue.add(neighbor);
                }
            }
        }
    }
}
