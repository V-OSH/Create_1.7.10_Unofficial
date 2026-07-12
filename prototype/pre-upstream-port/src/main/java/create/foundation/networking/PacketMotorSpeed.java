package create.foundation.networking;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import create.core.kinetic.source.CreativeMotorTileEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Client→Server packet: set the Creative Motor's configured speed.
 */
public class PacketMotorSpeed implements IMessage {

    private int x, y, z;
    private int speed;

    public PacketMotorSpeed() {}

    public PacketMotorSpeed(int x, int y, int z, int speed) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.speed = speed;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        speed = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(speed);
    }

    public static class Handler implements IMessageHandler<PacketMotorSpeed, IMessage> {
        @Override
        public IMessage onMessage(PacketMotorSpeed msg, MessageContext ctx) {
            World world = ctx.getServerHandler().playerEntity.worldObj;
            TileEntity te = world.getTileEntity(msg.x, msg.y, msg.z);
            if (te instanceof CreativeMotorTileEntity) {
                CreativeMotorTileEntity motor = (CreativeMotorTileEntity) te;
                // Set speed directly instead of cycling
                motor.setConfiguredSpeed(msg.speed);
                motor.updateGeneratedRotation();
                motor.markDirty();
            }
            return null;
        }
    }
}
