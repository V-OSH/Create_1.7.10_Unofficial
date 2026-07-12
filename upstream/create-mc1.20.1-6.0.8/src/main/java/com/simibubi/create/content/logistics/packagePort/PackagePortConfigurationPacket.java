package com.simibubi.create.content.logistics.packagePort;

import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class PackagePortConfigurationPacket extends BlockEntityConfigurationPacket<PackagePortBlockEntity> {

	private String newFilter;
	private boolean acceptPackages;

	public PackagePortConfigurationPacket(BlockPos pos, String newFilter, boolean acceptPackages) {
		super(pos);
		this.newFilter = newFilter;
		this.acceptPackages = acceptPackages;
	}

	public PackagePortConfigurationPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeBoolean(acceptPackages);
		buffer.writeUtf(newFilter);
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		acceptPackages = buffer.readBoolean();
		newFilter = buffer.readUtf();
	}

	@Override
	protected void applySettings(PackagePortBlockEntity be) {
		if (be.addressFilter.equals(newFilter) && be.acceptsPackages == acceptPackages)
			return;
		be.addressFilter = newFilter;
		be.acceptsPackages = acceptPackages;
		be.filterChanged();
		be.notifyUpdate();
	}

}
