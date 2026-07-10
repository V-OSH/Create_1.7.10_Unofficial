package create.core.kinetic;

import create.shim.MyBlockPos;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds the aggregated state of a single kinetic network.
 *
 * <p>Each network is identified by the {@link MyBlockPos#asLong()} of its
 * generating source. Speed is uniform across the network; stress and capacity
 * are aggregated from all members.</p>
 *
 * <p>Members are stored by position (not TE reference) so the network
 * survives chunk unloads without dangling references.</p>
 */
public class KineticNetwork {

    private final Long id;

    /** Generator positions → base capacity at 1 RPM. */
    private final Map<MyBlockPos, Float> sources = new HashMap<>();

    /** Consumer/relay positions → base stress impact at 1 RPM. */
    private final Map<MyBlockPos, Float> members = new HashMap<>();

    private float currentCapacity;
    private float currentStress;
    private int unloadedMembers;
    private float unloadedCapacity;
    private float unloadedStress;

    public KineticNetwork(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public int getSize() {
        return members.size() + unloadedMembers;
    }

    public float getCurrentCapacity() {
        return currentCapacity;
    }

    public float getCurrentStress() {
        return currentStress;
    }

    public int getUnloadedMembers() {
        return unloadedMembers;
    }

    /** True if the network is overstressed (stress exceeds capacity). */
    public boolean isOverStressed() {
        return currentCapacity > 0 && currentStress > currentCapacity;
    }

    // --- Add / Remove ---

    /**
     * Add a tile to this network. If it is a source, its capacity is
     * recorded; otherwise its stress impact is recorded as a consumer.
     *
     * @param te        the kinetic tile
     * @param speed     current network speed (absolute RPM)
     */
    public void add(IKineticTile te, float speed) {
        MyBlockPos pos = te.getKineticPos();
        float absSpeed = Math.abs(speed);

        float stressApplied = te.calculateStressApplied();
        if (stressApplied > 0) {
            members.put(pos, stressApplied);
            currentStress += stressApplied * absSpeed;
        }

        float capacityAdded = te.calculateAddedStressCapacity();
        if (capacityAdded > 0) {
            sources.put(pos, capacityAdded);
            currentCapacity += capacityAdded * absSpeed;
        }
    }

    /**
     * Remove a tile from this network.
     */
    public void remove(IKineticTile te) {
        MyBlockPos pos = te.getKineticPos();
        float absSpeed = Math.abs(te.getSpeed());

        Float removedStress = members.remove(pos);
        if (removedStress != null) {
            currentStress -= removedStress * absSpeed;
            if (currentStress < 0) currentStress = 0;
        }

        Float removedCapacity = sources.remove(pos);
        if (removedCapacity != null) {
            currentCapacity -= removedCapacity * absSpeed;
            if (currentCapacity < 0) currentCapacity = 0;
        }
    }

    /**
     * Add a tile during chunk load without triggering stress recalc.
     * Used when the tile's NBT stored previous stress/capacity values.
     */
    public void addSilently(IKineticTile te, float lastCapacity, float lastStress) {
        MyBlockPos pos = te.getKineticPos();
        if (te.calculateAddedStressCapacity() > 0) {
            sources.put(pos, te.calculateAddedStressCapacity());
        }
        if (te.calculateStressApplied() > 0) {
            members.put(pos, te.calculateStressApplied());
        }
        unloadedCapacity += lastCapacity;
        unloadedStress += lastStress;
        unloadedMembers++;
    }

    /** Recalculate effective capacity from all registered sources at current speed. */
    public float calculateCapacity(float networkSpeed) {
        float absSpeed = Math.abs(networkSpeed);
        float total = unloadedCapacity;
        for (float baseCapacity : sources.values()) {
            total += baseCapacity * absSpeed;
        }
        return total;
    }

    /** Recalculate effective stress from all registered members at current speed. */
    public float calculateStress(float networkSpeed) {
        float absSpeed = Math.abs(networkSpeed);
        float total = unloadedStress;
        for (float baseStress : members.values()) {
            total += baseStress * absSpeed;
        }
        return total;
    }

    /** Update capacity and stress based on the given network speed. */
    public void updateFromSpeed(float networkSpeed) {
        currentCapacity = calculateCapacity(networkSpeed);
        currentStress = calculateStress(networkSpeed);
    }

    /** Returns true if this network has no members and no sources. */
    public boolean isEmpty() {
        return members.isEmpty() && sources.isEmpty() && unloadedMembers == 0;
    }
}
