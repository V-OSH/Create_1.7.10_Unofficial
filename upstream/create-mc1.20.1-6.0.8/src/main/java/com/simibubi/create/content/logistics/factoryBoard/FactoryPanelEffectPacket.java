package com.simibubi.create.content.logistics.factoryBoard;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent.Context;

public class FactoryPanelEffectPacket extends SimplePacketBase {

	private FactoryPanelPosition fromPos;
	private FactoryPanelPosition toPos;
	private boolean success;

	public FactoryPanelEffectPacket(FactoryPanelPosition fromPos, FactoryPanelPosition toPos, boolean success) {
		this.fromPos = fromPos;
		this.toPos = toPos;
		this.success = success;
	}

	public FactoryPanelEffectPacket(FriendlyByteBuf buffer) {
		fromPos = new FactoryPanelPosition(buffer.readBlockPos(), PanelSlot.values()[buffer.readVarInt()]);
		toPos = new FactoryPanelPosition(buffer.readBlockPos(), PanelSlot.values()[buffer.readVarInt()]);
		success = buffer.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(fromPos.pos());
		buffer.writeVarInt(fromPos.slot()
			.ordinal());
		buffer.writeBlockPos(toPos.pos());
		buffer.writeVarInt(toPos.slot()
			.ordinal());
		buffer.writeBoolean(success);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ClientLevel level = Minecraft.getInstance().level;
			BlockState blockState = level.getBlockState(fromPos.pos());
			if (!AllBlocks.FACTORY_GAUGE.has(blockState))
				return;
			FactoryPanelBehaviour panelBehaviour = FactoryPanelBehaviour.at(level, toPos);
			if (panelBehaviour != null) {
				panelBehaviour.bulb.setValue(1);
				FactoryPanelConnection connection = panelBehaviour.targetedBy.get(fromPos);
				if (connection != null)
					connection.success = success;
			}
		});
		return true;
	}
}
