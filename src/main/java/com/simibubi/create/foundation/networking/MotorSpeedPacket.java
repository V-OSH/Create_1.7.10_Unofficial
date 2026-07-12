/*
 * Legacy network bridge adapted from Create 6.0.8's ValueSettingsPacket.
 * Create is Copyright (c) simibubi and contributors, licensed under the MIT License.
 */
package com.simibubi.create.foundation.networking;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.kinetics.motor.KineticMotorValueSettings;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class MotorSpeedPacket implements IMessage {

    private int x;
    private int y;
    private int z;
    private int row;
    private int value;

    public MotorSpeedPacket() {}

    public MotorSpeedPacket(int x, int y, int z, int row, int value) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.row = row;
        this.value = value;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        row = buffer.readByte();
        value = buffer.readUnsignedShort();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        buffer.writeByte(row);
        buffer.writeShort(value);
    }

    public void applyTo(CreativeMotorBlockEntity motor) {
        motor.setGeneratedSpeed(KineticMotorValueSettings.toSignedSpeed(row, value));
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public static class Handler implements IMessageHandler<MotorSpeedPacket, IMessage> {

        @Override
        public IMessage onMessage(MotorSpeedPacket message, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().playerEntity;
            World world = player.worldObj;
            if (player.getDistanceSq(message.x + 0.5, message.y + 0.5, message.z + 0.5) > 64) {
                return null;
            }
            if (world.getBlock(message.x, message.y, message.z) != AllBlocks.CREATIVE_MOTOR) {
                return null;
            }
            TileEntity tileEntity = world.getTileEntity(message.x, message.y, message.z);
            if (tileEntity instanceof CreativeMotorBlockEntity motor) {
                message.applyTo(motor);
            }
            return null;
        }
    }
}
