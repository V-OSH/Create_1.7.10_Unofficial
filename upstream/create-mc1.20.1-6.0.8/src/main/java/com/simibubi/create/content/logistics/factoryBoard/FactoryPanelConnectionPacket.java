package com.simibubi.create.content.logistics.factoryBoard;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class FactoryPanelConnectionPacket extends BlockEntityConfigurationPacket<FactoryPanelBlockEntity> {

	private FactoryPanelPosition fromPos;
	private FactoryPanelPosition toPos;
	private boolean relocate;

	public FactoryPanelConnectionPacket(FactoryPanelPosition fromPos, FactoryPanelPosition toPos, boolean relocate) {
		super(toPos.pos());
		this.fromPos = fromPos;
		this.toPos = toPos;
		this.relocate = relocate;
	}

	public FactoryPanelConnectionPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(fromPos.pos());
		buffer.writeVarInt(fromPos.slot()
			.ordinal());
		buffer.writeBlockPos(toPos.pos());
		buffer.writeVarInt(toPos.slot()
			.ordinal());
		buffer.writeBoolean(relocate);
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		fromPos = new FactoryPanelPosition(buffer.readBlockPos(), PanelSlot.values()[buffer.readVarInt()]);
		toPos = new FactoryPanelPosition(buffer.readBlockPos(), PanelSlot.values()[buffer.readVarInt()]);
		relocate = buffer.readBoolean();
	}

	@Override
	protected void applySettings(ServerPlayer player, FactoryPanelBlockEntity be) {
		FactoryPanelBehaviour behaviour = FactoryPanelBehaviour.at(be.getLevel(), toPos);
		if (behaviour != null)
			if (relocate)
				behaviour.moveTo(fromPos, player);
			else
				behaviour.addConnection(fromPos);
	}

	@Override
	protected void applySettings(FactoryPanelBlockEntity be) {}

	@Override
	protected int maxRange() {
		return super.maxRange() * 2;
	}

}
