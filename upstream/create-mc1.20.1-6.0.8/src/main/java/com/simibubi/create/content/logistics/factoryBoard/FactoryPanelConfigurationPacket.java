package com.simibubi.create.content.logistics.factoryBoard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class FactoryPanelConfigurationPacket extends BlockEntityConfigurationPacket<FactoryPanelBlockEntity> {

	private PanelSlot slot;
	private String address;
	private Map<FactoryPanelPosition, Integer> inputAmounts;
	private List<ItemStack> craftingArrangement;
	private int outputAmount;
	private int promiseClearingInterval;
	private FactoryPanelPosition removeConnection;
	private boolean clearPromises;
	private boolean reset;
	private boolean redstoneReset;

	public FactoryPanelConfigurationPacket(FactoryPanelPosition position, String address,
		Map<FactoryPanelPosition, Integer> inputAmounts, List<ItemStack> craftingArrangement, int outputAmount,
		int promiseClearingInterval, @Nullable FactoryPanelPosition removeConnection, boolean clearPromises,
		boolean reset, boolean sendRedstoneReset) {
		super(position.pos());
		this.address = address;
		this.inputAmounts = inputAmounts;
		this.craftingArrangement = craftingArrangement;
		this.outputAmount = outputAmount;
		this.promiseClearingInterval = promiseClearingInterval;
		this.removeConnection = removeConnection;
		this.clearPromises = clearPromises;
		this.reset = reset;
		this.redstoneReset = sendRedstoneReset;
		this.slot = position.slot();
	}

	public FactoryPanelConfigurationPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeVarInt(slot.ordinal());
		buffer.writeUtf(address);
		buffer.writeVarInt(inputAmounts.size());
		for (Entry<FactoryPanelPosition, Integer> entry : inputAmounts.entrySet()) {
			entry.getKey()
				.send(buffer);
			buffer.writeVarInt(entry.getValue());
		}
		buffer.writeVarInt(craftingArrangement.size());
		craftingArrangement.forEach(buffer::writeItem);
		buffer.writeVarInt(outputAmount);
		buffer.writeVarInt(promiseClearingInterval);
		buffer.writeBoolean(removeConnection != null);
		if (removeConnection != null)
			removeConnection.send(buffer);
		buffer.writeBoolean(clearPromises);
		buffer.writeBoolean(reset);
		buffer.writeBoolean(redstoneReset);
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		slot = PanelSlot.values()[buffer.readVarInt()];
		address = buffer.readUtf();
		inputAmounts = new HashMap<>();
		int entries = buffer.readVarInt();
		for (int i = 0; i < entries; i++)
			inputAmounts.put(FactoryPanelPosition.receive(buffer), buffer.readVarInt());
		int craftEntries = buffer.readVarInt();
		craftingArrangement = new ArrayList<>();
		for (int i = 0; i < craftEntries; i++)
			craftingArrangement.add(buffer.readItem());
		outputAmount = buffer.readVarInt();
		promiseClearingInterval = buffer.readVarInt();
		if (buffer.readBoolean())
			removeConnection = FactoryPanelPosition.receive(buffer);
		clearPromises = buffer.readBoolean();
		reset = buffer.readBoolean();
		redstoneReset = buffer.readBoolean();
	}

	@Override
	protected void applySettings(FactoryPanelBlockEntity be) {
		FactoryPanelBehaviour behaviour = be.panels.get(slot);
		if (behaviour == null)
			return;

		behaviour.recipeAddress = reset ? "" : address;
		behaviour.recipeOutput = reset ? 1 : outputAmount;
		behaviour.promiseClearingInterval = reset ? -1 : promiseClearingInterval;
		behaviour.activeCraftingArrangement = reset ? List.of() : craftingArrangement;

		if (reset) {
			behaviour.forceClearPromises = true;
			behaviour.disconnectAll();
			behaviour.setFilter(ItemStack.EMPTY);
			behaviour.count = 0;
			be.redraw = true;
			be.notifyUpdate();
			return;
		}
		
		if (redstoneReset) {
			behaviour.disconnectAllLinks();
			be.notifyUpdate();
			return;
		}
		
		for (Entry<FactoryPanelPosition, Integer> entry : inputAmounts.entrySet()) {
			FactoryPanelPosition key = entry.getKey();
			FactoryPanelConnection connection = behaviour.targetedBy.get(key);
			if (connection != null)
				connection.amount = entry.getValue();
		}

		if (removeConnection != null) {
			behaviour.targetedBy.remove(removeConnection);
			FactoryPanelBehaviour source = FactoryPanelBehaviour.at(be.getLevel(), removeConnection);
			if (source != null) {
				source.targeting.remove(behaviour.getPanelPosition());
				source.blockEntity.sendData();
			}
		}

		if (clearPromises)
			behaviour.forceClearPromises = true;

		be.notifyUpdate();
	}

}
