package com.simibubi.create.content.logistics.packagePort;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public class PackagePortPlacementPacket extends SimplePacketBase {

	private PackagePortTarget target;
	private BlockPos pos;

	public PackagePortPlacementPacket(PackagePortTarget target, BlockPos pos) {
		this.target = target;
		this.pos = pos;
	}

	public PackagePortPlacementPacket(FriendlyByteBuf buffer) {
		target = PackagePortTarget.read(buffer.readNbt());
		pos = buffer.readBlockPos();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeNbt(target.write());
		buffer.writeBlockPos(pos);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player == null)
				return;
			Level world = player.level();
			if (world == null || !world.isLoaded(pos))
				return;
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (!(blockEntity instanceof PackagePortBlockEntity ppbe))
				return;
			if (!target.canSupport(ppbe))
				return;
			
			Vec3 targetLocation = target.getExactTargetLocation(ppbe, world, pos);
			if (targetLocation == Vec3.ZERO || !targetLocation.closerThan(Vec3.atBottomCenterOf(pos),
				AllConfigs.server().logistics.packagePortRange.get() + 2))
				return;

			target.setup(ppbe, world, pos);
			ppbe.target = target;
			ppbe.notifyUpdate();
			ppbe.use(player);
		});
		return true;
	}

	public static class ClientBoundRequest extends SimplePacketBase {

		BlockPos pos;

		public ClientBoundRequest(BlockPos pos) {
			this.pos = pos;
		}

		public ClientBoundRequest(FriendlyByteBuf buffer) {
			this.pos = buffer.readBlockPos();
		}

		@Override
		public void write(FriendlyByteBuf buffer) {
			buffer.writeBlockPos(pos);
		}

		@Override
		public boolean handle(Context context) {
			context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
				() -> () -> PackagePortTargetSelectionHandler.flushSettings(pos)));
			return true;
		}

	}

}
