package com.simibubi.create.foundation.blockEntity.behaviour;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour.ValueSettings;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;

public class ValueSettingsPacket extends BlockEntityConfigurationPacket<SmartBlockEntity> {

	private int row;
	private int value;
	private InteractionHand interactHand;
	private Direction side;
	private boolean ctrlDown;
	private int behaviourIndex;
	private BlockHitResult hitResult;

	public ValueSettingsPacket(BlockPos pos, int row, int value, @Nullable InteractionHand interactHand,
		@Nullable BlockHitResult hitResult, Direction side, boolean ctrlDown, int behaviourIndex) {
		super(pos);
		this.row = row;
		this.value = value;
		this.interactHand = interactHand;
		this.hitResult = hitResult;
		this.side = side;
		this.ctrlDown = ctrlDown;
		this.behaviourIndex = behaviourIndex;
	}

	public ValueSettingsPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeVarInt(value);
		buffer.writeVarInt(row);
		buffer.writeBoolean(interactHand != null);
		if (interactHand != null) {
			buffer.writeBoolean(interactHand == InteractionHand.MAIN_HAND);
			buffer.writeBlockHitResult(hitResult);
		}
		buffer.writeVarInt(side.ordinal());
		buffer.writeBoolean(ctrlDown);
		buffer.writeVarInt(behaviourIndex);
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		value = buffer.readVarInt();
		row = buffer.readVarInt();
		if (buffer.readBoolean()) {
			interactHand = buffer.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
			hitResult = buffer.readBlockHitResult();
		}
		side = Direction.values()[buffer.readVarInt()];
		ctrlDown = buffer.readBoolean();
		behaviourIndex = buffer.readVarInt();
	}

	@Override
	protected void applySettings(ServerPlayer player, SmartBlockEntity be) {
		for (BlockEntityBehaviour behaviour : be.getAllBehaviours()) {
			if (!(behaviour instanceof ValueSettingsBehaviour valueSettingsBehaviour))
				continue;
			if (!valueSettingsBehaviour.acceptsValueSettings())
				continue;
			if (behaviourIndex != valueSettingsBehaviour.netId())
				continue;
			if (interactHand != null) {
				valueSettingsBehaviour.onShortInteract(player, interactHand, side, hitResult);
				return;
			}
			valueSettingsBehaviour.setValueSettings(player, new ValueSettings(row, value), ctrlDown);
			return;
		}
	}

	@Override
	protected void applySettings(SmartBlockEntity be) {}

}
