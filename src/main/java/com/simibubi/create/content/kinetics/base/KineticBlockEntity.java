/*
 * Adapted from Create 6.0.8's KineticBlockEntity for Minecraft Forge 1.7.10.
 * Upstream source: com/simibubi/create/content/kinetics/base/KineticBlockEntity.java
 * Create is Copyright (c) simibubi and contributors, licensed under the MIT License.
 */
package com.simibubi.create.content.kinetics.base;

import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.utility.legacy.kinetics.LegacyKineticWorldAdapter;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class KineticBlockEntity extends TileEntity {

    private float speed;
    private boolean networkInitialized;

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        if (this.speed == speed) {
            return;
        }
        this.speed = speed;
        markDirty();
        if (worldObj != null) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    @Override
    public void updateEntity() {
        if (!networkInitialized && worldObj != null && !worldObj.isRemote) {
            networkInitialized = true;
            LegacyKineticWorldAdapter.rebuildAt(worldObj, xCoord, yCoord, zCoord);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        speed = tag.getFloat("Speed");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setFloat("Speed", speed);
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
    }

    @Override
    public void onDataPacket(NetworkManager network, S35PacketUpdateTileEntity packet) {
        readFromNBT(packet.func_148857_g());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return worldObj != null && ICogWheel.isLargeCog(getBlockType())
            ? createRenderBoundingBox(xCoord, yCoord, zCoord, true)
            : super.getRenderBoundingBox();
    }

    static AxisAlignedBB createRenderBoundingBox(int x, int y, int z, boolean largeCogwheel) {
        int inflation = largeCogwheel ? 1 : 0;
        return AxisAlignedBB.getBoundingBox(x - inflation, y - inflation, z - inflation, x + 1 + inflation,
            y + 1 + inflation, z + 1 + inflation);
    }
}
