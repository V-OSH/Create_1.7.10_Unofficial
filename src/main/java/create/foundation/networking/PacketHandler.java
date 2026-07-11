package create.foundation.networking;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Wraps Forge 1.7.10's {@link SimpleNetworkWrapper} for Create's
 * {@link IPacket} system.
 */
public final class PacketHandler {

    public static final String CHANNEL = "create";

    public static PacketHandler INSTANCE;

    private final SimpleNetworkWrapper channel;
    private int nextDiscriminator;

    public PacketHandler() {
        INSTANCE = this;
        channel = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void registerPacket(Class<? extends IPacket> packetClass, Side handlerSide) {
        byte discriminator = (byte) nextDiscriminator++;
        channel.registerMessage(
                PacketDispatcher.class,
                (Class) packetClass,
                discriminator,
                handlerSide
        );
    }

    public <T extends IMessage> void registerMessage(
            Class<T> packetClass, IMessageHandler<T, ?> handler, Side handlerSide) {
        byte discriminator = (byte) nextDiscriminator++;
        channel.registerMessage(handler, packetClass, discriminator, handlerSide);
    }

    public void sendToServer(IMessage packet) {
        channel.sendToServer(packet);
    }

    public void sendToAll(IMessage packet) {
        channel.sendToAll(packet);
    }

    public void sendTo(IMessage packet, EntityPlayerMP player) {
        channel.sendTo(packet, player);
    }

    public void sendToAllAround(IMessage packet, NetworkRegistry.TargetPoint point) {
        channel.sendToAllAround(packet, point);
    }

    public void sendToDimension(IMessage packet, int dimensionId) {
        channel.sendToDimension(packet, dimensionId);
    }

    public static final class PacketDispatcher
            implements IMessageHandler<IPacket, IMessage> {

        @Override
        public IMessage onMessage(IPacket message, MessageContext ctx) {
            message.handle();
            return null;
        }
    }
}
