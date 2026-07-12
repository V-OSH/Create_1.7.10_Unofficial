package create.core.machinery.fan;

import create.core.kinetic.KineticTileEntity;
import create.shim.MyBlockPos;
import create.shim.MyDirection;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Encased Fan tile entity — manages the air current simulation.
 * Scanning and entity processing is done inline (no separate AirCurrent class
 * for Phase 3a simplicity).
 */
public class EncasedFanTileEntity extends KineticTileEntity {

    private ForgeDirection facing = ForgeDirection.NORTH;
    private int maxDistance;
    private boolean updateAirFlow = true;
    private int airCurrentCooldown;
    private int entitySearchCooldown;
    private AxisAlignedBB airCurrentBounds;

    // Per-entity processing state stored in NBT on the ItemEntity
    private static final String PROCESSING_KEY = "CreateFanProcessing";
    private static final String PROCESSING_TIME_KEY = "CreateFanProcessingTime";
    private static final String PROCESSING_TYPE_KEY = "CreateFanProcessingType";

    // --- Accessors ---

    public ForgeDirection getFacing() {
        return facing;
    }

    public void updateFromMetadata(int meta) {
        if (meta >= 0 && meta < 6) {
            this.facing = ForgeDirection.getOrientation(meta);
            this.rotationAxis = MyDirection.Axis.fromForge(facing);
        }
    }

    public void markAirFlowDirty() {
        this.updateAirFlow = true;
    }

    /** Get the direction of airflow. Positive speed = push, negative = pull. */
    public ForgeDirection getAirFlowDirection() {
        float speed = getSpeed();
        if (speed == 0) return null;
        return speed > 0 ? facing : facing.getOpposite();
    }

    // --- Tick ---

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (worldObj == null) return;
        if (getSpeed() == 0) return;

        if (worldObj.isRemote) return;

        // Rebuild air current periodically
        if (airCurrentCooldown > 0) {
            airCurrentCooldown--;
        }
        if (updateAirFlow || airCurrentCooldown <= 0) {
            rebuildAirCurrent();
            updateAirFlow = false;
            airCurrentCooldown = 20;
        }

        // Search for entities every 5 ticks
        if (entitySearchCooldown > 0) {
            entitySearchCooldown--;
        }
        if (entitySearchCooldown <= 0) {
            entitySearchCooldown = 5;
            processAirCurrent();
        }
    }

    // --- Air current rebuild ---

    private void rebuildAirCurrent() {
        maxDistance = computeMaxDistance();
        airCurrentBounds = computeBounds();
    }

    private int computeMaxDistance() {
        float speed = Math.abs(getSpeed());
        // Lerp between 3 and 20 based on speed (max at 256 RPM)
        float factor = Math.min(speed / 256f, 1.0f);
        return Math.max(3, (int) (3 + factor * 17));
    }

    private AxisAlignedBB computeBounds() {
        ForgeDirection flow = getAirFlowDirection();
        if (flow == null) return null;

        int dx = flow.offsetX;
        int dy = flow.offsetY;
        int dz = flow.offsetZ;

        // Start at the fan's output face
        int startX = xCoord + facing.offsetX;
        int startY = yCoord + facing.offsetY;
        int startZ = zCoord + facing.offsetZ;

        // End at max distance (or first solid block)
        int endX = xCoord + dx * (maxDistance + 1);
        int endY = yCoord + dy * (maxDistance + 1);
        int endZ = zCoord + dz * (maxDistance + 1);

        // Expand perpendicular to flow for entity capture (0.5 block width)
        double minX, minY, minZ, maxX, maxY, maxZ;

        if (dx != 0) {
            minX = Math.min(startX, endX);
            maxX = Math.max(startX, endX) + 1;
            minY = yCoord + 0.25;
            maxY = yCoord + 0.75;
            minZ = zCoord + 0.25;
            maxZ = zCoord + 0.75;
        } else if (dy != 0) {
            minX = xCoord + 0.25;
            maxX = xCoord + 0.75;
            minY = Math.min(startY, endY);
            maxY = Math.max(startY, endY) + 1;
            minZ = zCoord + 0.25;
            maxZ = zCoord + 0.75;
        } else {
            minX = xCoord + 0.25;
            maxX = xCoord + 0.75;
            minY = yCoord + 0.25;
            maxY = yCoord + 0.75;
            minZ = Math.min(startZ, endZ);
            maxZ = Math.max(startZ, endZ) + 1;
        }

        return AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    // --- Air current processing ---

    @SuppressWarnings("unchecked")
    private void processAirCurrent() {
        if (airCurrentBounds == null || worldObj == null) return;

        ForgeDirection flow = getAirFlowDirection();
        if (flow == null) return;

        float speed = Math.abs(getSpeed());
        double strength = Math.min(speed / 256f, 1.0f);

        // Find entities in the air current bounds
        List<Entity> entities = worldObj.getEntitiesWithinAABB(
                Entity.class, airCurrentBounds);

        for (Entity entity : entities) {
            if (entity instanceof EntityPlayer) continue; // don't push players
            if (entity.isDead) continue;

            // Compute entity's position along the flow
            double dist = getDistanceAlongFlow(entity);
            if (dist < 0 || dist > maxDistance) continue;

            // Push/pull
            double force = strength * 0.05;
            entity.motionX += flow.offsetX * force;
            entity.motionY += flow.offsetY * force;
            entity.motionZ += flow.offsetZ * force;

            // Cap velocity
            double maxVel = 0.5;
            if (Math.abs(entity.motionX) > maxVel) entity.motionX = Math.signum(entity.motionX) * maxVel;
            if (Math.abs(entity.motionY) > maxVel) entity.motionY = Math.signum(entity.motionY) * maxVel;
            if (Math.abs(entity.motionZ) > maxVel) entity.motionZ = Math.signum(entity.motionZ) * maxVel;

            // Find processing type at this distance
            int blockDist = (int) Math.round(dist);
            FanProcessingType type = getProcessingTypeAt(blockDist);
            if (type == null) continue;

            // Process ItemEntity
            if (entity instanceof EntityItem) {
                processItemEntity((EntityItem) entity, type);
            } else {
                type.affectEntity(entity);
            }
        }
    }

    private double getDistanceAlongFlow(Entity entity) {
        ForgeDirection flow = getAirFlowDirection();
        if (flow == null) return -1;

        double dx = entity.posX - (xCoord + 0.5 + facing.offsetX * 0.5);
        double dy = entity.posY - (yCoord + 0.5 + facing.offsetY * 0.5);
        double dz = entity.posZ - (zCoord + 0.5 + facing.offsetZ * 0.5);

        return dx * flow.offsetX + dy * flow.offsetY + dz * flow.offsetZ;
    }

    private FanProcessingType getProcessingTypeAt(int dist) {
        World world = worldObj;
        if (world == null) return null;

        // Check blocks at this distance from the fan output face
        int checkX = xCoord + facing.offsetX * (dist + 1);
        int checkY = yCoord + facing.offsetY * (dist + 1);
        int checkZ = zCoord + facing.offsetZ * (dist + 1);

        // Check the block itself and the block below (for floor-mounted catalysts)
        FanProcessingType type = AllFanProcessingTypes.getAt(world, checkX, checkY, checkZ);
        if (type != null) return type;

        // Check one block below (catalysts might be at floor level)
        return AllFanProcessingTypes.getAt(world, checkX, checkY - 1, checkZ);
    }

    private void processItemEntity(EntityItem entity, FanProcessingType type) {
        ItemStack stack = entity.getEntityItem();
        if (stack == null) return;

        NBTTagCompound data = entity.getEntityData();
        int previousType = data.getInteger(PROCESSING_TYPE_KEY);
        int currentType = type.getPriority();

        // If the processing type changed, reset progress
        if (previousType != currentType) {
            data.setInteger(PROCESSING_TIME_KEY, 0);
            data.setInteger(PROCESSING_TYPE_KEY, currentType);
        }

        int time = data.getInteger(PROCESSING_TIME_KEY);
        time++;
        data.setInteger(PROCESSING_TIME_KEY, time);

        // Processing takes 100 ticks (scaled by stack size)
        int requiredTime = 100 * stack.stackSize;
        if (time >= requiredTime) {
            List<ItemStack> results = type.process(stack);
            data.setInteger(PROCESSING_TIME_KEY, 0);

            if (results != null && !results.isEmpty()) {
                // Replace the item entity with processed outputs
                for (int i = 0; i < results.size(); i++) {
                    ItemStack result = results.get(i);
                    if (i == 0) {
                        // Replace existing entity's item
                        entity.setEntityItemStack(result.copy());
                    } else {
                        // Spawn additional entities
                        EntityItem newEntity = new EntityItem(worldObj,
                                entity.posX, entity.posY, entity.posZ, result.copy());
                        newEntity.delayBeforeCanPickup = 10;
                        worldObj.spawnEntityInWorld(newEntity);
                    }
                }
                if (results.isEmpty()) {
                    entity.setDead();
                }
            }
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
        maxDistance = tag.getInteger("maxDistance");
        updateAirFlow = true;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("facing", facing.ordinal());
        tag.setInteger("maxDistance", maxDistance);
    }
}
