package com.simibubi.create.content.kinetics.chainConveyor;

import java.util.Collection;
import java.util.UUID;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.simibubi.create.foundation.render.PlayerSkyhookRenderer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ClientboundChainConveyorRidingPacket extends SimplePacketBase {

	private final Collection<UUID> uuids;

	public ClientboundChainConveyorRidingPacket(Collection<UUID> uuids) {
		this.uuids = uuids;
	}

	public ClientboundChainConveyorRidingPacket(FriendlyByteBuf buffer) {
		this.uuids = buffer.readList(FriendlyByteBuf::readUUID);
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeCollection(uuids, FriendlyByteBuf::writeUUID);
	}

	@Override
	public boolean handle(NetworkEvent.Context context) {
		context.enqueueWork(() -> {
			PlayerSkyhookRenderer.updatePlayerList(this.uuids);
		});
		return true;
	}
}
