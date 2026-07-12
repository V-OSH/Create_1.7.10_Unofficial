/*
 * Adapted from Create 6.0.8's KineticBlockEntity for Minecraft Forge 1.7.10.
 * Upstream source: com/simibubi/create/content/kinetics/base/KineticBlockEntity.java
 * Create is Copyright (c) simibubi and contributors, licensed under the MIT License.
 */
package com.simibubi.create.content.kinetics.base;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

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
            LegacyKineticNetwork.rebuildAt(worldObj, xCoord, yCoord, zCoord);
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
}
