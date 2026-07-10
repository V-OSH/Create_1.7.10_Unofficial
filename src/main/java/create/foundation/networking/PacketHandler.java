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

    private final SimpleNetworkWrapper channel;
    private int nextDiscriminator;

    public PacketHandler() {
        channel = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);
    }

    /**
     * Register a packet type.
     *
     * @param handlerSide the side where {@link IPacket#handle()} executes —
     *                    {@link Side#CLIENT} for server-to-client packets,
     *                    {@link Side#SERVER} for client-to-server packets
     */
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

    public void sendToServer(IPacket packet) {
        channel.sendToServer(packet);
    }

    public void sendToAll(IPacket packet) {
        channel.sendToAll(packet);
    }

    public void sendTo(IPacket packet, EntityPlayerMP player) {
        channel.sendTo(packet, player);
    }

    public void sendToAllAround(IPacket packet, NetworkRegistry.TargetPoint point) {
        channel.sendToAllAround(packet, point);
    }

    public void sendToDimension(IPacket packet, int dimensionId) {
        channel.sendToDimension(packet, dimensionId);
    }

    /**
     * Generic dispatcher that calls {@link IPacket#handle()} on the message.
     */
    public static final class PacketDispatcher
            implements IMessageHandler<IPacket, IMessage> {

        @Override
        public IMessage onMessage(IPacket message, MessageContext ctx) {
            message.handle();
            return null;
        }
    }
}
