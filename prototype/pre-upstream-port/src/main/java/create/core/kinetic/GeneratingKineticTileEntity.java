package create.core.kinetic;

/**
 * Base class for tile entities that GENERATE rotational power.
 *
 * <p>Extends {@link KineticTileEntity} with the source lifecycle: a generating
 * tile IS its own network (identified by {@code getKineticPos().asLong()}).
 * When conditions change (water flow, right-click speed adjustment), the
 * tile detaches from its old network and re-creates itself as a fresh
 * source, propagating the new speed downstream.</p>
 *
 * <p>Subclasses must implement {@link #getGeneratedSpeed()} — the speed
 * they produce at the current moment (can change based on conditions).</p>
 */
public abstract class GeneratingKineticTileEntity extends KineticTileEntity {

    /**
     * Set to true when this source was overpowered and should re-claim
     * its network on the next tick.
     */
    public boolean reActivateSource;

    // --- Network identity ---

    /**
     * Each source's network ID is its own block position.
     * Matches the Create 6.0.8 {@code GeneratingKineticBlockEntity.createNetworkId()}.
     */
    public Long createNetworkId() {
        return getKineticPos().asLong();
    }

    /**
     * Subclasses return their current generated speed in RPM.
     * May return 0 if conditions for generation are not met.
     */
    @Override
    public abstract float getGeneratedSpeed();

    // --- Speed lifecycle ---

    /**
     * Called when external conditions change (water flow, right-click, etc.).
     * Detaches from the old network and re-creates as a fresh source with
     * the new speed.
     */
    public void updateGeneratedRotation() {
        if (worldObj == null || worldObj.isRemote) return;

        float newSpeed = getGeneratedSpeed();
        float prevSpeed = this.speed;

        if (prevSpeed != newSpeed) {
            applyNewSpeed(prevSpeed, newSpeed);
        }

        // Refresh stress/capacity in the network (even if speed didn't change,
        // capacity from this source may have changed)
        if (networkId != null && newSpeed != 0) {
            KineticNetwork network = KineticNetworkManager.getNetwork(
                    networkId, getDimensionId());
            if (network != null) {
                network.updateFromSpeed(newSpeed);
            }
        }

        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    /**
     * State machine for source speed transitions.
     *
     * <ul>
     *   <li>0 → non-zero: create a new network and propagate</li>
     *   <li>non-zero → 0: detach and dissolve the network</li>
     *   <li>non-zero → new non-zero (self-powered): re-broadcast with new speed</li>
     *   <li>non-zero → new non-zero (overpowered by stronger source): stay subordinate</li>
     * </ul>
     */
    protected void applyNewSpeed(float prevSpeed, float newSpeed) {
        if (newSpeed == 0) {
            if (hasSource()) {
                // Overpowered by another source; just stop contributing capacity.
                // The external source's network handles propagation.
                getOrCreateNetwork().updateFromSpeed(getSpeed());
                return;
            }
            // Shut down our own network entirely
            if (worldObj != null) {
                RotationPropagator.handleRemoved(worldObj, getKineticPos(), this);
            }
            setSpeed(0);
            setNetworkId(null);
            return;
        }

        if (prevSpeed == 0) {
            // Start from idle: create a fresh network
            setSpeed(newSpeed);
            setNetworkId(createNetworkId());
            setSourcePosition(null);
            if (worldObj != null) {
                RotationPropagator.handleAdded(worldObj, this);
            }
            return;
        }

        if (hasSource()) {
            // We are overpowered by another source.
            // Only reclaim if our new speed is strictly stronger (in absolute terms).
            if (Math.abs(prevSpeed) >= Math.abs(newSpeed)) return;
            // Become the dominant source
            if (worldObj != null) {
                RotationPropagator.handleRemoved(worldObj, getKineticPos(), this);
            }
            setSpeed(newSpeed);
            setSourcePosition(null);
            setNetworkId(createNetworkId());
            if (worldObj != null) {
                RotationPropagator.handleAdded(worldObj, this);
            }
            return;
        }

        // Speed change while self-powered: detach and re-broadcast
        if (worldObj != null) {
            RotationPropagator.handleRemoved(worldObj, getKineticPos(), this);
        }
        setSpeed(newSpeed);
        if (worldObj != null) {
            RotationPropagator.handleAdded(worldObj, this);
        }
    }

    // --- Helpers ---

    private boolean hasSource() {
        return sourcePosition != null;
    }

    private KineticNetwork getOrCreateNetwork() {
        return KineticNetworkManager.getOrCreateNetwork(
                networkId != null ? networkId : -1L, getDimensionId());
    }

    // --- Re-activation (overpower recovery) ---

    @Override
    public void removeSource() {
        if (hasSource() && isSource()) {
            reActivateSource = true;
        }
        super.removeSource();
        setNetworkId(null);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (reActivateSource && worldObj != null && !worldObj.isRemote) {
            updateGeneratedRotation();
            reActivateSource = false;
        }
    }
}
