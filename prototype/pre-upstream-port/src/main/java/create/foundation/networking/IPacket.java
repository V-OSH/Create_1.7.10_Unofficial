package create.foundation.networking;

import cpw.mods.fml.common.network.simpleimpl.IMessage;

/**
 * Custom packet abstraction for Create 1.7.10.
 *
 * <p>Extends Forge's {@link IMessage} so packets work directly with
 * {@code SimpleNetworkWrapper}. Each packet knows how to encode/decode itself
 * and has a {@code handle()} method that executes on the target logical side.</p>
 *
 * <p>All concrete implementations must have a no-arg constructor for Forge's
 * reflective instantiation.</p>
 */
public interface IPacket extends IMessage {

    /** Execute the packet logic on the target logical side. */
    void handle();
}
